package com.gettingmobile.google.reader.sync;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.gettingmobile.android.content.LowDeviceStorageDetector;
import com.gettingmobile.goodnews.sync.CleanupService;
import com.gettingmobile.google.reader.*;
import com.gettingmobile.google.reader.db.*;
import com.gettingmobile.google.reader.rest.*;
import com.gettingmobile.io.IOUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

public final class PullSynchronizer extends AbstractSynchronizer {
    private static final int REFERENCE_PROGRESS_UPDATE_STEP = 500;
	private final TagDatabaseAdapter tagAdapter;
	private final FeedDatabaseAdapter feedAdapter;
	private final ItemDatabaseAdapter itemAdapter;
    private final ItemReferenceDatabaseAdapter itemReferenceAdapter;
    private final ItemRequestSpecificationDatabaseAdapter itemRequestSpecificationAdapter;

	public PullSynchronizer(SyncContext context) {
		super(context);
		
		/*
		 * create database adapters
		 */
		tagAdapter = new TagDatabaseAdapter();
		feedAdapter = new FeedDatabaseAdapter();
		itemAdapter = new ItemDatabaseAdapter();
        itemReferenceAdapter = new ItemReferenceDatabaseAdapter();
        itemRequestSpecificationAdapter = new ItemRequestSpecificationDatabaseAdapter();
	}

    @Override
    public int forecastMaxProgress() {
        return settings.getMaxUnreadKeeping() / REFERENCE_PROGRESS_UPDATE_STEP + 1 + // query read list item references
                1 + // query sort order
                1 + // query tags
                1 + // query feeds
                1 + // query tag item references
                1;  // post processing
    }

    @Override
    protected void throwCancelledIfApplicable() throws SyncException {
        super.throwCancelledIfApplicable();

        if (settings.cancelSyncOnLowDeviceStorage() && LowDeviceStorageDetector.isDeviceStorageLow(context.getContext()))
            throw new SyncException(SyncException.ErrorCode.DEVICE_STORAGE_LOW);
    }

    @Override
	protected void doSync(SyncCallbackHelper callback) throws URISyntaxException, SyncException {
        /*
         * init database
         */
        final SQLiteDatabase db = getDbHelper().getDatabase();
        final TmpItemReferenceTable tmpItemReferenceTable = new TmpItemReferenceTable();
        db.beginTransaction();
        try {
            tmpItemReferenceTable.create(db);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        /*
         * fetch read list item references to be able to determine progress
         */
        fetchAndProcessUnreadItemIds(callback);
        throwCancelledIfApplicable();

        /*
         * fetch sort order
         */
        final StreamContentOrder sortOrder = fetchSortOrder(callback);
        throwCancelledIfApplicable();

        /*
         * fetch tags
         */
        fetchAndProcessTagList(callback, sortOrder);
        throwCancelledIfApplicable();

        /*
         * fetch feeds
         */
        fetchAndProcessSubscriptions(callback, sortOrder);
        throwCancelledIfApplicable();

        /*
         * fetch tag item references
         */
        fetchAndProcessTagItemReferences(callback);
        throwCancelledIfApplicable();

        /*
         * fetch old item references (if there have been requested any by the user)
         */
        final List<ItemRequestSpecification> oldItemRequests = fetchOldItemReferences(callback);
        throwCancelledIfApplicable();

        /*
         * fetch unknown items
         */
        final int newUnreadItemCount = fetchAndProcessUnknownItems(callback);
        callback.setNewUnreadCount(newUnreadItemCount);

        /*
         * clean up
         */
        db.beginTransaction();
        try {
            tmpItemReferenceTable.drop(db);
            cleanUpOldItemRequests(db, oldItemRequests);

            final Calendar minReadTime = getPastTimestamp(settings.getDaysToCache());
            Log.d(LOG_TAG, "Deleting all read, untagged articles older than " + minReadTime.getTimeInMillis());
            itemAdapter.deleteReadUnreferencedItems(db, minReadTime.getTimeInMillis());

            final Calendar minUnreadTime = getPastTimestamp(settings.getDaysToCleanupUnread());
            Log.d(LOG_TAG, "Deleting all unread, untagged articles older than " + minUnreadTime.getTimeInMillis());
            itemAdapter.deleteUnreadUnreferencedItems(db, minUnreadTime.getTimeInMillis());

            itemAdapter.updateFeedTitles(db);

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        settings.updateLastSuccessfulPullTimestamp();
        callback.incrementProgress();
        callback.setUnreadCount(itemAdapter.readUnreadCount(db));

        /*
         * we should try to do a vacuum and analyze to dramatically increase performance
         */
        getDbHelper().tryVacuum();
        getDbHelper().analyze();

        /*
         * cleanup no longer required files
         */
        CleanupService.start(context.getContext());
	}

    /*
     * the several syncing steps
     */

    private void fetchAndProcessUnreadItemIds(SyncCallbackHelper callback) throws URISyntaxException, SyncException {
        final SQLiteDatabase db = getDbHelper().getDatabase();

        /*
         * log unread items, update item read marks and keep newest elements according to sync settings
         */
        final List<ElementId> tagIds = new ArrayList<ElementId>(1);
        tagIds.add(ItemState.READING_LIST.getId());
        final ItemReferenceStream refs = sendRequest(new GetItemReferencesRequest(
                authenticator, tagIds, ItemState.READ, settings.getMaxUnreadKeeping(), 0, true));
        int outstandingProgressIncrements = settings.getMaxUnreadKeeping() / REFERENCE_PROGRESS_UPDATE_STEP;
        try {
            db.beginTransaction();
            try {
                /*
                 * write references to database
                 */
                for (int count = 0; refs.hasNext(); ++count) {
                    throwCancelledIfApplicable();
                    try {
                        itemReferenceAdapter.write(db, refs.next());
                    } catch (JsonStreamException ex) {
                        if (!ex.isRecoverable())
                            throw ex;

                        Log.e(LOG_TAG, "Ignored error processing unread item reference.", ex);
                        callback.incrementSkipCount();
                    }
                    if (count % REFERENCE_PROGRESS_UPDATE_STEP == 0) {
                        callback.incrementProgress();
                        --outstandingProgressIncrements;
                    }
                }

                /*
                 * update existing item read states
                 */
                itemReferenceAdapter.updateItemReadMarks(db);

                /*
                 * delete references for known items
                 */
                itemReferenceAdapter.deleteKnown(db);

                /*
                 * delete items older than we should sync
                 */
                final Calendar minUnreadTimestamp = settings.getMinUnreadTimestamp();
                Log.i(LOG_TAG, "minUnreadTimestamp=" + minUnreadTimestamp.getTimeInMillis() + " (" + minUnreadTimestamp + ")");
                itemReferenceAdapter.deleteOlder(db, minUnreadTimestamp);

                final Calendar maxUnreadTimestamp = itemReferenceAdapter.getMaxTimestamp(db);
                Log.i(LOG_TAG, "maxUnreadTimestamp=" + maxUnreadTimestamp.getTimeInMillis() + " (" + maxUnreadTimestamp + ")");
                settings.setMinUnreadTimestamp(maxUnreadTimestamp);

                /*
                 * delete items older than time to keep unread
                 */
                final Calendar minUnreadToCleanupTimestamp = getPastTimestamp(settings.getDaysToCleanupUnread());
                Log.i(LOG_TAG, "minUnreadToCleanupTimestamp=" + minUnreadToCleanupTimestamp.getTimeInMillis() + " (" + minUnreadToCleanupTimestamp + ")");
                itemReferenceAdapter.deleteOlder(db, minUnreadToCleanupTimestamp);

                /*
                 * delete references to be ignored
                 */
                final Set<ElementId> streamIds = itemReferenceAdapter.readTagIds(db);
                for (Iterator<ElementId> it = streamIds.iterator(); it.hasNext(); ) {
                    if (!settings.shouldIgnoreUnread(it.next())) {
                        it.remove();
                    }
                }
                itemReferenceAdapter.deleteByTagIds(db, streamIds);

                /*
                 * restrict number of references for unknown, unread items to the max number configured by the user
                 */
                itemReferenceAdapter.keepNewest(db, settings.getMaxUnreadSync());

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        } finally {
            IOUtils.closeQuietly(refs);
        }
        callback.incrementProgress(outstandingProgressIncrements);

        /*
         * update progress
         */
        final int unreadCount = itemReferenceAdapter.readCount(db);
        Log.d(LOG_TAG, "number new unread items: " + unreadCount);
        callback.addMaxProgress(calculateNumberOfRequests(unreadCount));
        callback.incrementProgress();
    }

    private StreamContentOrder fetchSortOrder(SyncCallbackHelper callback) throws URISyntaxException {
        final StreamContentOrder sortOrder = sendRequest(new GetSortOrderRequest(authenticator));
        callback.incrementProgress();
        return sortOrder;
    }

    private void fetchAndProcessTagList(SyncCallbackHelper callback, StreamContentOrder sortOrder) throws URISyntaxException {
        final List<Tag> tags = sendRequest(new GetTagListRequest(authenticator));
        tags.add(new Tag(settings.getLabelReadListId(), false));
        final SQLiteDatabase db = getDbHelper().getDatabase();

        /*
         * apply sort order and determine which tags to insert, which to update and which to delete
         */
        final Map<String, Integer> sortIdOrder = sortOrder.getSortIdOrder(ItemState.ROOT.getId());
        final Set<ElementId> toBeDeleted = tagAdapter.readAllIds(db);
        final List<Tag> toBeUpdated = new ArrayList<Tag>();
        final List<Tag> toBeInserted = new ArrayList<Tag>();
        for (Tag tag : tags) {
            final Integer s = sortIdOrder != null ? sortIdOrder.get(tag.getSortId()) : null;
            if (s != null) {
                tag.setRootSortOrder(s);
            }
            if (toBeDeleted.remove(tag.getId())) {
                toBeUpdated.add(tag);
            } else {
                toBeInserted.add(tag);
            }
        }

        /*
         * write tags to database
         */
        db.beginTransaction();
        try {
            /*
             * update
             */
            for (final Tag tag : toBeUpdated) {
                tagAdapter.updateById(db, tag);
            }

            /*
             * insert
             */
            for (final Tag tag : toBeInserted) {
                tagAdapter.write(db, tag);
            }

            /*
             * delete
             */
            tagAdapter.deleteById(db, toBeDeleted);

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        callback.incrementProgress();
    }

    private void fetchAndProcessSubscriptions(SyncCallbackHelper callback, StreamContentOrder sortOrder) throws URISyntaxException {
        final List<Feed> feeds = sendRequest(new GetSubscriptionsRequest(authenticator));

        /*
         * apply sort order
         */
        for (Feed f : feeds) {
            f.setSortOrder(sortOrder);
        }

        /*
         * store feeds
         */
        final SQLiteDatabase db = getDbHelper().getDatabase();
        db.beginTransaction();
        try {
            /*
             * update
             */
            feedAdapter.rewrite(db, feeds);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        callback.incrementProgress();
    }

    private void fetchAndProcessTagItemReferences(SyncCallbackHelper callback) throws URISyntaxException, SyncException {
        /*
         * build the list of tags to be fetched
         */
        final List<ElementId> tagIds = new ArrayList<ElementId>();
        tagIds.add(ItemState.STARRED.getId());
        for (Tag label : tagAdapter.readUserLabels(getDbHelper().getDatabase())) {
            if (!label.isFeedFolder() && settings.shouldSyncTag(label.getId())) {
                tagIds.add(label.getId());
            }
        }

        /*
         * fetch item reference for all the requests
         */
        final SQLiteDatabase db = getDbHelper().getDatabase();
        final int newUnreadItemCount = itemReferenceAdapter.readCount(db);
        while (!tagIds.isEmpty()) {
            throwCancelledIfApplicable();
            writeItemReferenceList(callback, sendRequest(
                    new GetItemReferencesRequest(authenticator, tagIds, settings.getMaxTaggedSync())));
        }

        /*
         * update tags of existing items and delete the references to known items
         */
        db.beginTransaction();
        try {
            // remove all item tags and reassign based on received references
            itemReferenceAdapter.updateItemTags(db);

            // process item tag change events for changes made by the user during the sync
            itemAdapter.addItemTagsFromItemTagChangeEvents(db);

            // delete references to known items, so that we do not fetch them again
            itemReferenceAdapter.deleteKnown(db);

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        callback.addMaxProgress(calculateNumberOfRequests(itemReferenceAdapter.readCount(db) - newUnreadItemCount));
        callback.incrementProgress();
    }

    private List<ItemRequestSpecification> fetchOldItemReferences(SyncCallbackHelper callback) throws URISyntaxException {
        final SQLiteDatabase db = getDbHelper().getDatabase();

        /*
         * read specifications for items to fetch
         */
        final List<ItemRequestSpecification> specs = itemRequestSpecificationAdapter.readAll(db);
        if (!specs.isEmpty()) {
            final int newUnknownItemCount = itemReferenceAdapter.readCount(db);
            callback.addMaxProgress(specs.size());

            /*
             * fetch item references for each spec
             */
            final List<ElementId> streamIds = new ArrayList<ElementId>(1);
            for (ItemRequestSpecification spec : specs) {
                throwCancelledIfApplicable();
                Log.d(LOG_TAG, "fetching old items for stream " + spec.getStreamId() +
                        " - maxAge=" + spec.getMaxAgeInDays() + "; maxCount=" + spec.getMaxItemCount());

                streamIds.clear();
                streamIds.add(spec.getStreamId());
                writeItemReferenceList(callback, sendRequest(new GetItemReferencesRequest(
                        authenticator, streamIds, null, spec.getMaxItemCount(), spec.getStartTime() / 1000, true)));
                callback.incrementProgress();
            }

            /*
             * post processing
             */
            db.beginTransaction();
            try {
                // delete references to known items, so that we do not fetch them again
                itemReferenceAdapter.deleteKnown(db);

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }

            final int fetchedItems = itemReferenceAdapter.readCount(db) - newUnknownItemCount;
            Log.d(LOG_TAG, "number of fetched old items: " + fetchedItems);
            callback.addMaxProgress(calculateNumberOfRequests(fetchedItems));
        }
        return specs;
    }
    
    private void cleanUpOldItemRequests(SQLiteDatabase db, List<ItemRequestSpecification> specs) {
        for (ItemRequestSpecification spec : specs) {
            itemRequestSpecificationAdapter.delete(db, spec.getStreamId());
        }
    }

    private int fetchAndProcessUnknownItems(SyncCallbackHelper callback) throws URISyntaxException {
        final SQLiteDatabase db = getDbHelper().getDatabase();
        itemReferenceAdapter.deleteBlacklisted(db);
        final List<ItemReference> refs = itemReferenceAdapter.readAll(db);

        final int pageSize = GetItemsByTagRequest.getPageSize();
        int unreadCount = 0;
        for (int i = 0; i < refs.size(); i+= pageSize) {
            throwCancelledIfApplicable();
            final ItemStream items = sendRequest(new GetItemsByReferenceRequest(
                    authenticator, getItemReferenceIdsPage(refs, i, pageSize)));
            try {
                unreadCount+= writeItemList(callback, items, getItemReferencePageMap(refs, i, pageSize));
            } finally {
                IOUtils.closeQuietly(items);
            }
            callback.incrementProgress();
        }
        return unreadCount;
    }

    /*
     * helpers
     */

	private int calculateNumberOfRequests(int itemCount) {
		final int pageSize = GetItemsByTagRequest.getPageSize();
		return itemCount / pageSize + (itemCount % pageSize > 0 ? 1 : 0);
	}

    private static long[] getItemReferenceIdsPage(List<ItemReference> refs, int start, int count) {
        final int size = Math.min(count, refs.size() - start);
        final long[] page = new long[size];
        for (int i = start; i < start + size; ++i) {
            page[i - start] = refs.get(i).getId();
        }
        return page;
    }

    private static Map<Long, ElementId> getItemReferencePageMap(List<ItemReference> refs, int start, int count) {
        final Map<Long, ElementId> map = new HashMap<Long, ElementId>();
        final int end = Math.min(start + count, refs.size());
        for (int i = start; i < end; ++i) {
            final ItemReference ref = refs.get(i);
            if (ref.getDirectStreamIds() != null) {
                map.put(ref.getId(), ref.getDirectStreamIds().get(0));
            }
        }
        return map;
    }

    private void writeItemReferenceList(SyncCallbackHelper callback, ItemReferenceStream refs) {
        final SQLiteDatabase db = getDbHelper().getDatabase();
        try {
            db.beginTransaction();
            try {
                while (refs.hasNext()) {
                    try {
                        itemReferenceAdapter.write(db, refs.next());
                    } catch (JsonStreamException ex) {
                        if (!ex.isRecoverable())
                            throw ex;

                        Log.e(LOG_TAG, "Ignored error processing item reference.", ex);
                        callback.incrementSkipCount();
                    }
                }
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        } finally {
            IOUtils.closeQuietly(refs);
        }

    }

    private boolean itemExistsAndIsNotTagged(SQLiteDatabase db, Item item) {
        return item.getTagIds().isEmpty() && itemAdapter.doesItemSignatureExist(db, item);
    }

    private void writeItemTagChangeEvent(SQLiteDatabase db, Item item, boolean add, ItemState state) {
        ItemTagChangeEventDatabaseAdapter.INSTANCE.write(db, new ItemTagChangeEvent(item, add, state.getId()));
    }

    private void scheduleMarkItemReadInGoogleReaderIfUnread(SQLiteDatabase db, Item item) {
        if (!item.isRead()) {
            writeItemTagChangeEvent(db, item, true, ItemState.READ);
            writeItemTagChangeEvent(db, item, false, ItemState.KEPT_UNREAD);
            writeItemTagChangeEvent(db, item, false, ItemState.TRACKING_KEPT_UNREAD);
        }
    }

    private int writeItemList(SyncCallbackHelper callback, ItemStream items, Map<Long, ElementId> mapRefToOrigin) throws SyncException {
        final ItemTagChangeDatabaseAdapter itemTagChangeAdapter = new ItemTagChangeDatabaseAdapter();
        final SQLiteDatabase db = getDbHelper().getDatabase();
        db.beginTransaction();
        try {
            final ElementId readListId = settings.getLabelReadListId();

            int unreadCount = 0;
            while (items.hasNext()) {
                try {
                    final Item item = items.next();

                    /*
                     * ensure that we use a known feed id instead of the article's real feed id
                     * (important e.g. for Google's "What's popular" meta feed)
                     */
                    final ElementId originId = mapRefToOrigin.get(item.getId().getItemReferenceId());
                    if (originId != null && originId.getType() == ElementType.FEED) {
                        item.setFeedId(originId);
                    }

                    if (settings.shouldFixDuplicateItems() && itemExistsAndIsNotTagged(db, item)) {
                        Log.i(LOG_TAG, "Ignoring duplicate item: " + item.getFeedId() + ": " + item.getTitle());
                        scheduleMarkItemReadInGoogleReaderIfUnread(db, item);
                    } else {
                        item.processContentTreatment(
                                settings.getFeedSummaryTreatment(item.getFeedId()),
                                settings.getFeedContentTreatment(item.getFeedId()));
                        item.createTeaser(
                                settings.getFeedTeaserSource(item.getFeedId()),
                                settings.getFeedTeaserStartChar(item.getFeedId()),
                                null);
                        if (!item.isRead()) {
                            if (settings.autoListFeedArticles(item.getFeedId())) {
                                itemTagChangeAdapter.addItemTag(item, readListId);
                                itemTagChangeAdapter.commitChanges(db);
                            }
                        }
                        itemAdapter.write(db, item, settings.storeContentInFiles());
                        try {
                            item.saveIfApplicable(settings.storeContentInFiles(), settings.getContentStorageProvider());
                        } catch (IOException ex) {
                            Log.e(LOG_TAG, "Failed to write item.", ex);
                        }

                        if (!item.isRead()) {
                            ++unreadCount;
                        }
                    }
                } catch (JsonStreamException ex) {
                    if (!ex.isRecoverable())
                        throw ex;

                    Log.e(LOG_TAG, "Ignored error processing item.", ex);
                    callback.incrementSkipCount();
                }
            }
            db.setTransactionSuccessful();
            return unreadCount;
        } finally {
            db.endTransaction();
        }
    }

    private static Calendar getPastTimestamp(int daysInPast) {
        final Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_YEAR, -1 * daysInPast);
        return c;
    }
}
