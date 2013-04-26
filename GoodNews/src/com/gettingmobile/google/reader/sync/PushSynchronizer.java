package com.gettingmobile.google.reader.sync;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import com.gettingmobile.google.reader.ElementId;
import com.gettingmobile.google.reader.ItemState;
import com.gettingmobile.google.reader.ItemTagChangeEvent;
import com.gettingmobile.google.reader.TagChangeOperation;
import com.gettingmobile.google.reader.db.ItemDatabaseAdapter;
import com.gettingmobile.google.reader.db.ItemTagChangeDatabaseAdapter;
import com.gettingmobile.google.reader.db.ItemTagChangeEventDatabaseAdapter;
import com.gettingmobile.google.reader.db.ItemTagChangeEventDatabaseAdapter.Group;
import com.gettingmobile.google.reader.rest.ChangeItemTagRequest;
import com.gettingmobile.google.reader.rest.GetEditTokenRequest;
import com.gettingmobile.rest.RequestCallback;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class PushSynchronizer extends AbstractSynchronizer {
    private final ItemDatabaseAdapter itemAdapter;
	private final ItemTagChangeEventDatabaseAdapter itemTagChangeEventAdapter;
	
	public PushSynchronizer(SyncContext context) {
		super(context);
		
		/*
		 * create database adapters
		 */
        itemAdapter = new ItemDatabaseAdapter();
		itemTagChangeEventAdapter = new ItemTagChangeEventDatabaseAdapter();
	}

    public boolean isPushRequired() {
        return ItemTagChangeDatabaseAdapter.hasGlobalChanges(getDbHelper().getDatabase());
    }

    @Override
    public int forecastMaxProgress() {
        return
                1 + // requesting edit token
                1;    // generating read state events
    }

    @Override
	protected void doSync(SyncCallbackHelper callback) throws URISyntaxException, SyncException {
        /*
         * generate change events for read state
         */
        createReadStateChangeEvents(callback);
        callback.incrementProgress();

        /*
         * request edit token if required
         */
        requestEditTokenIfRequired(callback);
        
        /*
         * send the change events to google reader
         */
        boolean send = true;
        while (send) {
            throwCancelledIfApplicable();
            send = sendNextItemTagChangeRequest(callback);
        }

        settings.updateLastSuccessfulPushTimestamp();
	}

	protected void requestEditTokenIfRequired(final SyncCallbackHelper callback) throws URISyntaxException, SyncException {
        if (authenticator.hasValidEditToken()) {
            callback.incrementProgress();
        } else {
            sendRequest(new GetEditTokenRequest(authenticator),
                    new RequestCallback<GetEditTokenRequest, String>() {
                        @Override
                        public void onRequestProcessed(GetEditTokenRequest request, String result, Throwable error) {
                            authenticator.setEditToken(result);
                            callback.incrementProgress();
                        }
            });
        }
	}
    
    private void insertReadStateChangePart(
            SQLiteStatement statement, ItemDatabaseAdapter.ReadStateChange r, boolean invert, ItemState state) {
        itemTagChangeEventAdapter.insert(statement, new ItemTagChangeEvent(
                r.itemId, r.feedId, invert ? !r.read : r.read, state.getId()));
    }
    
    protected void createReadStateChangeEvents(SyncCallbackHelper callback) {
        final SQLiteDatabase db = getDbHelper().getDatabase();
        db.beginTransaction();
        try {
            final SQLiteStatement s = itemTagChangeEventAdapter.compileInsertStatement(db);
            final List<ItemDatabaseAdapter.ReadStateChange> readStateChanges = itemAdapter.readReadStateChanges(db);
            for (ItemDatabaseAdapter.ReadStateChange r : readStateChanges) {
                insertReadStateChangePart(s, r, false, ItemState.READ);
                insertReadStateChangePart(s, r, true, ItemState.KEPT_UNREAD);
                insertReadStateChangePart(s, r, true, ItemState.TRACKING_KEPT_UNREAD);
            }
            itemAdapter.updateSyncedRead(db);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        callback.addMaxProgress(itemTagChangeEventAdapter.readGroupCount(getDbHelper().getDatabase(), ChangeItemTagRequest.MAX_ITEMS));
        callback.incrementProgress();
    }

	protected boolean sendNextItemTagChangeRequest(SyncCallbackHelper callback) throws URISyntaxException, SyncException {
		/*
		 * retrieve next items to be adjusted
		 */
		final SQLiteDatabase db = getDbHelper().getDatabase();
		final Group g = itemTagChangeEventAdapter.readFirstGroup(db);
		if (g == null) {
			return false;
		}
		
		final List<ItemTagChangeEvent> events =
			itemTagChangeEventAdapter.readByTagAndOperation(db, g.tag, g.operation, ChangeItemTagRequest.MAX_ITEMS);
		final List<ElementId> feedIds = new ArrayList<ElementId>(events.size());
		final List<ElementId> itemIds = new ArrayList<ElementId>(events.size());
		for (ItemTagChangeEvent e : events) {
			feedIds.add(e.getFeedId());
			itemIds.add(e.getItemId());
		}
		
		/*
		 * build and send request 
		 */
        final boolean add = TagChangeOperation.ADD == g.operation;
        sendRequest(new ChangeItemTagRequest(
                authenticator, feedIds, itemIds, add ? g.tag : null, add ? null : g.tag));
        cleanUpItemTagChangeEvents(itemIds, g.operation, g.tag);
        callback.incrementProgress();
        return true;
	}

    protected void cleanUpItemTagChangeEvents(Collection<ElementId> itemIds, TagChangeOperation operation, ElementId tag) {
        final SQLiteDatabase db = getDbHelper().getDatabase();
        db.beginTransaction();
        try {
            for (ElementId itemId : itemIds) {
                itemTagChangeEventAdapter.delete(db, itemId, tag, operation);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }
}
