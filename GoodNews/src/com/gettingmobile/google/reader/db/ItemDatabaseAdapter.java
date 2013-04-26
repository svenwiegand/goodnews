package com.gettingmobile.google.reader.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Bundle;
import com.gettingmobile.google.reader.*;
import com.gettingmobile.util.StringUtils;

import java.util.*;

import static com.gettingmobile.google.reader.db.ItemTable.*;

public class ItemDatabaseAdapter extends ElementDatabaseAdapter<Item> {
    private static final String TAG_ID = "tagId";
    private static final String SELECT_COLUMNS = "i._id _id, i.id id, i.title title, i.refId refId, i.feedId feedId, " +
            "i.feedTitle feedTitle, i.timestamp timestamp, i.read read, " +
            "i.alternateHref alternateHref, i.alternateMimeType alternateMimeType, i.author author, " +
            "i.hasSummary hasSummary, i.hasContent hasContent, i.isExternalContent isExternalContent, i.hasImages hasImages, " +
            "i.teaser teaser, t.id tagId ";

	public ItemDatabaseAdapter() {
        super(TABLE_NAME);
	}

    protected String buildKeyList(long[] keys) {
        final Long[] k = new Long[keys.length];
        for (int i = 0; i < keys.length; ++i) {
            k[i] = keys[i];
        }
        return StringUtils.explode(Arrays.asList(k), ",");
    }

    public long readItemKeyById(SQLiteDatabase db, ElementId itemId) {
        final Cursor c = db.query(
                TABLE_NAME, new String[] { KEY }, "id=?", new String[] { itemId.getId() }, null, null, null);
        try {
            return c.moveToFirst() ? c.getLong(0) : INVALID_ID;
        } finally {
            c.close();
        }
    }

    public ItemCursor cursorByKeys(SQLiteDatabase db, long[] keys, boolean groupByFeeds, SortOrder order) {
        if (keys == null || keys.length == 0)
            return new EmptyItemCursor();

        final String feedOrder = groupByFeeds ? "feedTitle COLLATE NOCASE, i.feedId, " : "";
        final Cursor indexCursor = db.rawQuery(
                "SELECT f._id, i._id, COUNT(it.itemKey), i.read " +
                        "FROM item i " +
                        "LEFT JOIN feed f ON f.id=i.feedId " +
                        "LEFT JOIN itemTag it ON it.itemKey=i._id " +
                        "WHERE i._id IN (" + buildKeyList(keys) + ") " +
                        "GROUP BY " + (groupByFeeds ? "f._id, " : "") + "i._id " +
                        "ORDER BY " + feedOrder + "i.timestamp " + order.getSql() + ", i._id",
                null);
        return createItemCursor(groupByFeeds, indexCursor, db.rawQuery(
                "SELECT " + SELECT_COLUMNS +
                        "FROM item i " +
                        "LEFT JOIN itemTag it ON it.itemKey=i._id " +
                        "LEFT JOIN tag t ON t._id=it.tagKey " +
                        "WHERE i._id IN (" + buildKeyList(keys) + ") " +
                        "ORDER BY " + feedOrder + "i.timestamp " + order.getSql() + ", i._id",
                null));
    }

	public List<Item> readByKeys(SQLiteDatabase db, long[] keys, boolean groupByFeeds, SortOrder order) {
        return readList(cursorByKeys(db, keys, groupByFeeds, order));
	}

    public ItemCursor cursorByKeysForFolder(SQLiteDatabase db, long[] keys, long tagKey,
                                            boolean groupByFeeds, boolean feedDragAndDropOrder, SortOrder order) {
        if (keys == null || keys.length == 0)
            return new EmptyItemCursor();

        final String feedGrouping = groupByFeeds ? "f._id, " : "";
        final String feedOrder = groupByFeeds ?
                (feedDragAndDropOrder ? "ft.sortOrder, feedTitle COLLATE NOCASE, i.feedId, " : "feedTitle COLLATE NOCASE, i.feedId, ") : "";
        final Cursor indexCursor = db.rawQuery(
                "SELECT f._id, i._id, COUNT(it.itemKey), i.read " +
                        "FROM item i " +
                        "LEFT JOIN feed f ON f.id=i.feedId " +
                        "LEFT JOIN feedTag ft ON ft.feedKey=f._id " +
                        "LEFT JOIN itemTag it ON it.itemKey=i._id " +
                        "WHERE " +
                        "  ft.tagKey=? AND " +
                        "  i._id IN (" + buildKeyList(keys) + ") " +
                        "GROUP BY " + feedGrouping + "i._id " +
                        "ORDER BY " + feedOrder + "i.timestamp " + order.getSql() + ", i._id",
                new String[] { Long.toString(tagKey) });
        return createItemCursor(groupByFeeds, indexCursor, db.rawQuery(
                "SELECT " + SELECT_COLUMNS +
                        "FROM item i " +
                        "LEFT JOIN feed f ON f.id=i.feedId " +
                        "LEFT JOIN feedTag ft ON ft.feedKey=f._id " +
                        "LEFT JOIN itemTag it ON it.itemKey=i._id " +
                        "LEFT JOIN tag t ON t._id=it.tagKey " +
                        "WHERE " +
                        "  ft.tagKey=? AND " +
                        "  i._id IN (" + buildKeyList(keys) + ") " +
                        "ORDER BY " + feedOrder + "i.timestamp " + order.getSql() + ", i._id",
                new String[] { Long.toString(tagKey) }));
    }

    public Item readByKey(SQLiteDatabase db, long key) {
        final List<Item> items = readList(db.rawQuery(
                "SELECT " + SELECT_COLUMNS +
                "FROM item i " +
                "LEFT JOIN itemTag it ON it.itemKey=i._id " +
                "LEFT JOIN tag t ON t._id=it.tagKey " +
                "WHERE i._id=? " +
                "ORDER BY i._id",
                new String[] { Long.toString(key) }));
        return !items.isEmpty() ? items.get(0) : null;
    }

    public Item readFullByKey(SQLiteDatabase db, long key) {
        final List<Item> items = readList(db.rawQuery(
                "SELECT i.summary summary, i.content content, " + SELECT_COLUMNS +
                "FROM item i " +
                "LEFT JOIN itemTag it ON it.itemKey=i._id " +
                "LEFT JOIN tag t ON t._id=it.tagKey AND t.isFeedFolder=0 " +
                "WHERE i._id=? " +
                "ORDER BY i._id",
                new String[] { Long.toString(key) }));
        return !items.isEmpty() ? items.get(0) : null;
    }

    public Item readById(SQLiteDatabase db, ElementId itemId) {
        final List<Item> items = readList(db.rawQuery(
                "SELECT " + SELECT_COLUMNS +
                        "FROM item i " +
                        "LEFT JOIN itemTag it ON it.itemKey=i._id " +
                        "LEFT JOIN tag t ON t._id=it.tagKey " +
                        "WHERE i.id=? " +
                        "ORDER BY i._id",
                new String[]{itemId.getId()}));
        return !items.isEmpty() ? items.get(0) : null;
    }

    public ItemCursor cursorByLabel(SQLiteDatabase db, long tagKey, boolean groupByFeeds, boolean excludeRead, SortOrder order) {
        final String tagKeyString = Long.toString(tagKey);
        final String excludeSelection = excludeRead ? "AND i.read=0 " : "";
        final String feedGrouping = groupByFeeds ? "f._id, " : "";
        final String feedOrder = groupByFeeds ? "feedTitle COLLATE NOCASE, i.feedId, " : "";
        final Cursor indexCursor = db.rawQuery(
                "SELECT f._id, i._id, COUNT(it.itemKey), i.read " +
                        "FROM item i " +
                        "LEFT JOIN feed f ON f.id=i.feedId " +
                        "LEFT JOIN itemTag it ON it.itemKey=i._id " +
                        "WHERE i._id IN (SELECT it2.itemKey FROM itemTag it2 WHERE it2.tagKey=?) " + excludeSelection +
                        "GROUP BY " + feedGrouping + "i._id " +
                        "ORDER BY " + feedOrder + "i.timestamp " + order.getSql() + ", i._id",
                new String[] { tagKeyString });
        return createItemCursor(groupByFeeds, indexCursor, db.rawQuery(
                "SELECT " + SELECT_COLUMNS +
                        "FROM item i " +
                        "LEFT JOIN feed f ON f.id=i.feedId " +
                        "LEFT JOIN itemTag it ON it.itemKey=i._id " +
                        "LEFT JOIN tag t ON t._id=it.tagKey " +
                        "WHERE i._id IN (SELECT it2.itemKey FROM itemTag it2 WHERE it2.tagKey=?) " + excludeSelection +
                        "ORDER BY " + feedOrder + "i.timestamp " + order.getSql() + ", i._id",
                new String[]{tagKeyString}));
    }

    public List<Item> readByLabel(SQLiteDatabase db, long tagKey, boolean groupByFeeds, boolean excludeRead, SortOrder order) {
        return readList(cursorByLabel(db, tagKey, groupByFeeds, excludeRead, order));
    }

    public ItemCursor cursorByFolder(SQLiteDatabase db, long tagKey, boolean groupByFeeds, boolean feedDragAndDropOrder, boolean excludeRead, SortOrder order) {
        final String tagKeyString = Long.toString(tagKey);
        final String excludeSelection = excludeRead ? "AND i.read=0 " : "";
        final String feedGrouping = groupByFeeds ? "f._id, " : "";
        final String feedOrder = groupByFeeds ?
                (feedDragAndDropOrder ? "ft.sortOrder, feedTitle COLLATE NOCASE, i.feedId, " : "feedTitle COLLATE NOCASE, i.feedId, ") : "";
        final Cursor indexCursor = db.rawQuery(
                "SELECT f._id, i._id, COUNT(it.itemKey), i.read " +
                        "FROM item i " +
                        "LEFT JOIN feed f ON f.id=i.feedId LEFT JOIN feedTag ft ON ft.feedKey=f._id AND ft.tagKey=? " +
                        "LEFT JOIN itemTag it ON it.itemKey=i._id " +
                        "WHERE ft.tagKey=? " + excludeSelection +
                        "GROUP BY " + feedGrouping + "i._id " +
                        "ORDER BY " + feedOrder + "i.timestamp " + order.getSql() + ", i._id",
                new String[] { tagKeyString, tagKeyString });
        return createItemCursor(groupByFeeds, indexCursor, db.rawQuery(
                "SELECT " + SELECT_COLUMNS +
                        "FROM item i " +
                        "LEFT JOIN feed f ON f.id=i.feedId LEFT JOIN feedTag ft ON ft.feedKey=f._id AND ft.tagKey=? " +
                        "LEFT JOIN itemTag it ON it.itemKey=i._id " +
                        "LEFT JOIN tag t ON t._id=it.tagKey " +
                        "WHERE ft.tagKey=? " + excludeSelection +
                        "ORDER BY " + feedOrder + "i.timestamp " + order.getSql() + ", i._id",
                new String[]{ tagKeyString, tagKeyString }));
    }

    public List<Item> readByFolder(SQLiteDatabase db, long tagKey, boolean groupByFeeds, boolean feedDragAndDropOrder, boolean excludeRead, SortOrder order) {
        return readList(cursorByFolder(db, tagKey, groupByFeeds, feedDragAndDropOrder, excludeRead, order));
    }

    public ItemCursor cursorByFeed(SQLiteDatabase db, ElementId feedId, boolean excludeRead, SortOrder order) {
        final String excludeSelection = excludeRead ? "AND i.read=0 " : "";
        final Cursor indexCursor = db.rawQuery(
                "SELECT 0, i._id, COUNT(it.itemKey), i.read " +
                        "FROM item i " +
                        "LEFT JOIN itemTag it ON it.itemKey=i._id " +
                        "WHERE i.feedId=? " + excludeSelection +
                        "GROUP BY i._id " +
                        "ORDER BY i.timestamp " + order.getSql() + ", i._id",
                new String[]{feedId.getId()});
        return createItemCursor(false, indexCursor, db.rawQuery(
                "SELECT " + SELECT_COLUMNS +
                        "FROM item i " +
                        "LEFT JOIN itemTag it ON it.itemKey=i._id " +
                        "LEFT JOIN tag t ON t._id=it.tagKey " +
                        "WHERE i.feedId=? " + excludeSelection +
                        "ORDER BY i.timestamp " + order.getSql() + ", i._id",
                new String[]{feedId.getId()}));
    }

    public List<Item> readByFeed(SQLiteDatabase db, ElementId feedId, boolean excludeRead, SortOrder order) {
        return readList(cursorByFeed(db, feedId, excludeRead, order));
    }

    public ItemCursor cursorAll(SQLiteDatabase db, boolean excludeRead, SortOrder order) {
        final String excludeSelection = excludeRead ? "WHERE i.read=0 " : "";
        final Cursor indexCursor = db.rawQuery(
                "SELECT 0, i._id, COUNT(it.itemKey), i.read " +
                        "FROM item i " +
                        "LEFT OUTER JOIN itemTag it ON it.itemKey=i._id " +
                        excludeSelection +
                        "GROUP BY i._id " +
                        "ORDER BY i.timestamp " + order.getSql() + ", i._id", null);
        return createItemCursor(false, indexCursor, db.rawQuery(
                "SELECT " + SELECT_COLUMNS +
                        "FROM item i " +
                        "LEFT OUTER JOIN itemTag it ON it.itemKey=i._id " +
                        "LEFT JOIN tag t ON t._id=it.tagKey AND t.isFeedFolder=0 " +
                        excludeSelection +
                        "ORDER BY i.timestamp " + order.getSql() + ", i._id",
                null));
    }

	public List<Item> readAll(SQLiteDatabase db, boolean excludeRead, SortOrder order) {
        return readList(cursorAll(db, excludeRead, order));
	}

    public ItemCursor cursorAll(SQLiteDatabase db, boolean groupByFeeds, boolean excludeRead, SortOrder order) {
        if (!groupByFeeds)
            return cursorAll(db, excludeRead, order);

        final String excludeSelection = excludeRead ? "WHERE i.read=0 " : "";
        final Cursor indexCursor = db.rawQuery(
                "SELECT f._id, i._id, COUNT(it.itemKey), i.read " +
                        "FROM item i " +
                        "LEFT JOIN feed f ON f.id=i.feedId " +
                        "LEFT OUTER JOIN itemTag it ON it.itemKey=i._id " +
                        excludeSelection +
                        "GROUP BY f._id, i._id " +
                        "ORDER BY f.title COLLATE NOCASE, i.feedId, i.timestamp " + order.getSql() + ", i._id",
                null);
        return createItemCursor(groupByFeeds, indexCursor, db.rawQuery(
                "SELECT " + SELECT_COLUMNS +
                        "FROM item i " +
                        "LEFT OUTER JOIN itemTag it ON it.itemKey=i._id " +
                        "LEFT JOIN tag t ON t._id=it.tagKey AND t.isFeedFolder=0 " +
                        excludeSelection +
                        "ORDER BY feedTitle COLLATE NOCASE, i.feedId, i.timestamp " + order.getSql() + ", i._id",
                null));
    }

    public List<Item> readAll(SQLiteDatabase db, boolean groupByFeeds, boolean excludeRead, SortOrder order) {
        return readList(cursorAll(db, groupByFeeds, excludeRead, order));
    }

    public Set<ElementId> readAllIds(SQLiteDatabase db) {
        final Cursor c = db.query(TABLE_NAME, new String[] { ID }, null, null, null, null, null);
        try {
            final Set<ElementId> ids = new HashSet<ElementId>(c.getCount());
            if (c.moveToFirst()) {
                do {
                    ids.add(new ElementId(c.getString(0)));
                } while (c.moveToNext());
            }
            return ids;
        } finally {
            c.close();
        }
    }

    public Set<ElementId> readExistingIds(SQLiteDatabase db, Collection<ElementId> ids) {
        final Set<ElementId> existingIds = new HashSet<ElementId>();
        if (!ids.isEmpty()) {
            final Cursor c = db.rawQuery(
                    "SELECT DISTINCT id FROM item WHERE id IN (" + StringUtils.explode(ids, ",", "'") + ")", null);
            try {
                if (c.moveToFirst()) {
                    do {
                        existingIds.add(new ElementId(c.getString(0)));
                    } while (c.moveToNext());
                }
            } finally {
                c.close();
            }
        }
        return existingIds;
    }
    
    /*
     * teaser related stuff
     */
    
    public ItemCursor cursorActiveWithContentWithoutTags(SQLiteDatabase db) {
        final Cursor indexCursor = db.rawQuery(
                "SELECT f._id, i._id, COUNT(it.itemKey), i.read " +
                        "FROM item i " +
                        "LEFT JOIN feed f ON f.id=i.feedId " +
                        "LEFT OUTER JOIN itemTag it ON it.itemKey=i._id " +
                        "WHERE it.tagKey IS NOT NULL OR i.read=0 " +
                        "GROUP BY i._id " +
                        "ORDER BY i.feedId, i.timestamp DESC, i._id",
                null);
        return createItemCursor(false, indexCursor, db.rawQuery(
                "SELECT i.content, i.summary, " + SELECT_COLUMNS +
                        "FROM item i " +
                        "LEFT JOIN feed f ON f.id=i.feedId " +
                        "LEFT OUTER JOIN itemTag it ON it.itemKey=i._id " +
                        "LEFT OUTER JOIN tag t ON t._id=it.tagKey " +
                        "WHERE it.tagKey IS NOT NULL OR i.read=0 " +
                        "ORDER BY i.feedId, i.timestamp DESC, i._id",
                null));
    }

    public ItemCursor cursorActiveWithContentWithoutTags(SQLiteDatabase db, ElementId feedId) {
        final Cursor indexCursor = db.rawQuery(
                "SELECT f._id, i._id, COUNT(it.itemKey), i.read " +
                        "FROM item i " +
                        "LEFT JOIN feed f ON f.id=i.feedId " +
                        "LEFT OUTER JOIN itemTag it ON it.itemKey=i._id " +
                        "WHERE f.id=? AND (it.tagKey IS NOT NULL OR i.read=0) " +
                        "GROUP BY i._id " +
                        "ORDER BY i.feedId, i.timestamp DESC, i._id",
                new String[] { feedId.getId() });
        return createItemCursor(false, indexCursor, db.rawQuery(
                "SELECT i.content, i.summary, " + SELECT_COLUMNS +
                        "FROM item i " +
                        "LEFT JOIN feed f ON f.id=i.feedId " +
                        "LEFT OUTER JOIN itemTag it ON it.itemKey=i._id " +
                        "LEFT OUTER JOIN tag t ON t._id=it.tagKey " +
                        "WHERE f.id=? AND (it.tagKey IS NOT NULL OR i.read=0) " +
                        "ORDER BY i.feedId, i.timestamp DESC, i._id",
                new String[] { feedId.getId() }));
    }
    
    public void updateTeaser(SQLiteDatabase db, long key, String teaser) {
        db.execSQL("UPDATE item SET teaser=? WHERE _id=?",
                new Object[] { teaser, key });
    }

    /*
     * signature check
     */

    public boolean doesItemSignatureExist(SQLiteDatabase db, Item item) {
        final Cursor c = db.rawQuery(
                "SELECT COUNT(*) FROM item i WHERE i.feedId=? AND i.timestamp=? AND i.title=?",
                new String[] {
                        item.getFeedId().getId(),
                        item.getTimestamp() != null ? Long.toString(item.getTimestamp().getTime()) : "0",
                        item.getTitle()});
        try {
            return c.moveToFirst() && c.getInt(0) > 0;
        } finally {
            c.close();
        }
    }

    /*
     * unread count
     */

	public int readUnreadCountByLabel(SQLiteDatabase db, ElementId tagId) {
		final Cursor c = db.rawQuery(
				"SELECT COUNT(*) " +
				"FROM item i " +
				"INNER JOIN itemTag it ON it.itemKey=i._id " +
				"WHERE it.tagKey=(SELECT t._id FROM tag t WHERE t.id=?) AND i.read=0",
				new String[] { tagId.getId() });
		try {
			return c.moveToFirst() ? c.getInt(0) : 0;
		} finally {
			c.close();
		}
	}

	public int readUnreadCount(SQLiteDatabase db) {
		final Cursor c = db.rawQuery(
				"SELECT COUNT(*) FROM " + TABLE_NAME +
					" WHERE " + READ + "=0", null);
		try {
			return c.moveToFirst() ? c.getInt(0) : 0;
		} finally {
			c.close();
		}
	}
    
    /*
     * statistics
     */
    
    public int readActiveCount(SQLiteDatabase db) {
        final Cursor c = db.rawQuery(
                "select count(distinct i._id) from item i left join itemTag it on it.itemKey=i._id where read=0 or it.tagKey is not null",
                null);
        try {
            return c.moveToFirst() ? c.getInt(0) : 0;
        } finally {
            c.close();
        }
    }

    public int readTotalCount(SQLiteDatabase db) {
        final Cursor c = db.rawQuery(
                "select count(*) from item i",
                null);
        try {
            return c.moveToFirst() ? c.getInt(0) : 0;
        } finally {
            c.close();
        }
    }

    /*
     * tag handling
     */

    public Set<ElementId> readItemTags(SQLiteDatabase db, long itemKey) {
        final Cursor c = db.rawQuery(
                "SELECT t.id " +
                "FROM itemTag it " +
                "INNER JOIN tag t ON t._id=it.tagKey " +
                "WHERE it.itemKey=?"
                , new String[] { Long.toString(itemKey) });
        try {
            final Set<ElementId> tags = new HashSet<ElementId>();
            if (c.moveToFirst()) do {
                tags.add(new ElementId(c.getString(0)));
            } while (c.moveToNext());
            return tags;
        } finally {
            c.close();
        }
    }

    protected void deleteItemTags(SQLiteDatabase db, long itemKey) {
        db.delete(ItemTagTable.TABLE_NAME, "itemKey=?", new String[]{Long.toString(itemKey)});
    }

    public void addItemTag(SQLiteDatabase db, long itemKey, ElementId tag) {
        db.execSQL(
                "INSERT OR IGNORE INTO " + ItemTagTable.TABLE_NAME + " (itemKey, tagKey) " +
                        "SELECT ?, t._id FROM tag t WHERE t.id=? AND t.isFeedFolder=0",
                new Object[]{itemKey, tag.getId()});
    }

    public void setItemTags(SQLiteDatabase db, long itemKey, Set<ElementId> tags) {
        deleteItemTags(db, itemKey);
        for (ElementId tagId : tags) {
            if (Tag.isUsed(tagId)) {
                addItemTag(db, itemKey, tagId);
            }
        }
    }

    public void addItemTagsFromItemTagChangeEvents(SQLiteDatabase db) {
        db.execSQL("INSERT INTO " + ItemTagTable.TABLE_NAME + " (itemKey, tagKey) " +
                "SELECT i._id, t._id FROM itemTagChangeEvent e " +
                "INNER JOIN item i ON i.id=e.itemId " +
                "INNER JOIN tag t ON t.id=e.tagId " +
                "WHERE e.operation=1");
    }

    public void removeItemTag(SQLiteDatabase db, long itemKey, ElementId tag) {
        db.delete(ItemTagTable.TABLE_NAME, "itemKey=? AND tagKey=(SELECT t._id FROM tag t WHERE t.id=?)",
                new String[]{Long.toString(itemKey), tag.getId()});
    }

    public void markItemRead(SQLiteDatabase db, long itemKey, boolean read) {
        db.execSQL("UPDATE item SET read=? WHERE _id=?", new String[] { read ? "1" : "0", Long.toString(itemKey) });
    }

    public void markItemsRead(SQLiteDatabase db, long[] itemKeys, boolean read) {
        db.execSQL("UPDATE item SET read=? WHERE _id IN (" + buildKeyList(itemKeys) + ")",
                new String[] { read ? "1" : "0" });
    }

    public void markAllRead(SQLiteDatabase db) {
        db.execSQL("UPDATE item SET read=1");
    }
    
    public void markReadByLabel(SQLiteDatabase db, ElementId tagId) {
        db.execSQL("UPDATE item SET read=1 WHERE _id IN " +
                "(SELECT it.itemKey FROM itemTag it WHERE it.tagKey=(SELECT t._id FROM tag t WHERE t.id=?))",
                new Object[] {tagId.getId()});
    }

    public void markReadByFolder(SQLiteDatabase db, ElementId tagId) {
        db.execSQL("UPDATE item SET read=1 WHERE _id IN " +
                "(SELECT i._id FROM item i " +
                "INNER JOIN feed f ON f.id=i.feedId " +
                "INNER JOIN feedTag ft ON ft.feedKey=f._id " +
                "INNER JOIN tag t ON t._id=ft.tagKey AND t.id=?)",
                new Object[] {tagId.getId()});
    }

    public void markReadByFeed(SQLiteDatabase db, ElementId feedId) {
        db.execSQL("UPDATE item SET read=1 WHERE feedId=?", new Object[] { feedId.getId() });
    }
    
    public SQLiteStatement compileMarkItemReadStatement(SQLiteDatabase db) {
        return db.compileStatement("UPDATE item SET read=? WHERE _id=?");
    }
    
    public void markItemRead(SQLiteStatement statement, long itemKey, boolean read) {
        statement.bindLong(1, read ? 1 : 0);
        statement.bindLong(2, itemKey);
        statement.execute();
    }
    
    public List<ReadStateChange> readReadStateChanges(SQLiteDatabase db) {
        final Cursor c = db.rawQuery("SELECT id, feedId, read FROM item WHERE read <> syncedRead", null);
        try {
            final List<ReadStateChange> ids = new ArrayList<ReadStateChange>(c.getCount());
            if (c.moveToFirst()) do {
                ids.add(new ReadStateChange(c.getString(0), c.getString(1), c.getInt(2) > 0));
            } while (c.moveToNext());
            return ids;            
        } finally {
            c.close();
        }        
    }

    public void updateSyncedRead(SQLiteDatabase db) {
        db.execSQL("UPDATE item SET syncedRead=1 WHERE read=1");
        db.execSQL("UPDATE item SET syncedRead=0 WHERE read=0");
    }
    
    public boolean hasUpdatedReadStates(SQLiteDatabase db) {
        final Cursor c = db.rawQuery("SELECT COUNT(*) FROM item WHERE read <> syncedRead", null);
        try {
            return c.moveToFirst() && c.getInt(0) > 0;
        } finally {
            c.close();
        }
    }

    public void deleteReadUnreferencedItems(SQLiteDatabase db, long minTimestamp) {
        db.execSQL("DELETE FROM item WHERE read<>0 AND insertTimestamp<=? AND _id NOT IN " +
                "(SELECT itemKey FROM itemTag it)",
                new String[] {Long.toString(minTimestamp)});
    }

    public void deleteReadUnreferencedItems(SQLiteDatabase db) {
        db.execSQL("DELETE FROM item WHERE read<>0 AND _id NOT IN " +
                "(SELECT itemKey FROM itemTag it INNER JOIN tag t ON it.tagKey=t._id AND t.isFeedFolder=0)");
    }

    public void deleteUnreadUnreferencedItems(SQLiteDatabase db, long minTimestamp) {
        db.execSQL("DELETE FROM item WHERE read=0 AND insertTimestamp<=? AND _id NOT IN " +
                "(SELECT itemKey FROM itemTag it)",
                new String[] {Long.toString(minTimestamp)});
    }

    public void updateFeedTitles(SQLiteDatabase db) {
        db.execSQL("UPDATE item SET feedTitle=(SELECT f.title FROM feed f WHERE f.id=item.feedId) " +
                "WHERE (SELECT f.title FROM feed f WHERE f.id=item.feedId) IS NOT NULL");
    }

    public void deleteItem(SQLiteDatabase db, Item item) {
        db.execSQL("DELETE FROM itemTag WHERE itemKey=?", new Long[]{item.getKey()});
        db.execSQL("DELETE FROM item WHERE _id=?", new Long[]{item.getKey()});
    }

    public void blacklistItem(SQLiteDatabase db, Item item) {
        deleteItem(db, item);
        new ItemBlacklistDatabaseAdapter().blacklistItem(db, item.getId());
    }

    /*
     * database mapping
     */
    
    private ItemCursor createItemCursor(boolean group, Cursor indexCursor, Cursor c) {
        return new StandardItemCursor(group, indexCursor, c);
    }
    

	@Override
	protected Item create() {
		return new Item();
	}

    public long write(SQLiteDatabase db, Item entity, boolean ignoreSummaryAndContent) throws SQLException {
        final Bundle parameters = new Bundle();
        parameters.putBoolean("ignoreSummaryAndContent", ignoreSummaryAndContent);
        return super.write(db, entity, parameters);
    }

    @Override
    protected void setRowValues(SQLiteDatabase db, ContentValues columns, Item item, Bundle parameters) {
        super.setRowValues(db, columns, item, parameters);
        columns.put(REF_ID, item.getId().getItemReferenceId());
        columns.put(FEED_ID, item.getFeedId().getId());
        columns.put(FEED_TITLE, item.getFeedTitle());
        columns.put(TIMESTAMP, item.getTimestamp() != null ? item.getTimestamp().getTime() : 0);
        columns.put(INSERT_TIMESTAMP, new Date().getTime());
        columns.put(READ, item.isRead());
        columns.put(SYNCED_READ, item.isRead());
        if (item.getAlternate() != null) {
            columns.put(ALTERNATE_HREF, item.getAlternate().getHref());
            columns.put(ALTERNATE_MIME_TYPE, item.getAlternate().getMimeType());
        }
        columns.put(AUTHOR, item.getAuthor());
        columns.put(HAS_SUMMARY, item.hasSummary());
        columns.put(HAS_CONTENT, item.hasContent());
        columns.put(TEASER, item.getTeaser());
        columns.put(IS_EXTERNAL_CONTENT, item.isExternalContent());
        columns.put(HAS_IMAGES, item.hasImages());
        
        final boolean ignoreSummaryAndContent = parameters.getBoolean("ignoreSummaryAndContent", false);
        if (!ignoreSummaryAndContent) {
            columns.put(SUMMARY, item.canStoreSummaryInDb() ? item.getSummary() : null);
            columns.put(CONTENT, item.canStoreContentInDb() ? item.getContent() : null);
        }
    }

    @Override
    public Item readCurrent(Cursor c) {
        final Item item = super.readCurrent(c);

        item.setFeedId(new ElementId(c.getString(c.getColumnIndex(FEED_ID))));
        item.setFeedTitle(c.getString(c.getColumnIndex(FEED_TITLE)));
        item.setRead(c.getInt(c.getColumnIndex(READ)) != 0);

        final int tagIdCol = c.getColumnIndex(TAG_ID);
        if (tagIdCol > -1) {
            final String tagId = c.getString(tagIdCol);
            if (tagId != null) {
                item.getTagIds().add(new ElementId(tagId));
            }
        }

        final long timestamp = c.getLong(c.getColumnIndex(TIMESTAMP));
        item.setTimestamp(timestamp > 0 ? new Date(timestamp) : null);

        final String alternateHref = c.getString(c.getColumnIndex(ALTERNATE_HREF));
        if (alternateHref != null) {
            final Resource alternate = new Resource();
            alternate.setHref(alternateHref);
            alternate.setMimeType(c.getString(c.getColumnIndex(ALTERNATE_MIME_TYPE)));
            item.setAlternate(alternate);
        }

        item.setAuthor(c.getString(c.getColumnIndex(AUTHOR)));

        final int summaryColumnIndex = c.getColumnIndex(SUMMARY);
        if (summaryColumnIndex >= 0) {
            item.setSummary(c.getString(summaryColumnIndex));
        }
        final int contentColumnIndex = c.getColumnIndex(CONTENT);
        if (contentColumnIndex >= 0) {
            item.setContent(c.getString(contentColumnIndex));
        }
        item.setHasSummary(c.getInt(c.getColumnIndex(HAS_SUMMARY)) > 0);
        item.setHasContent(c.getInt(c.getColumnIndex(HAS_CONTENT)) > 0);
        item.setIsExternalContent(c.getInt(c.getColumnIndex(IS_EXTERNAL_CONTENT)) > 0);
        item.setHasImages(c.getInt(c.getColumnIndex(HAS_IMAGES)) > 0);

        final int teaserColumnIndex = c.getColumnIndex(TEASER);
        if (teaserColumnIndex >= 0) {
            item.setTeaser(c.getString(teaserColumnIndex));
        }

        return item;
    }

    @Override
    public void readJoinCurrent(Item current, Cursor c) {
        super.readJoinCurrent(current, c);

        final String tagId = c.getString(c.getColumnIndex(TAG_ID));
        if (tagId != null) {
            current.getTagIds().add(new ElementId(tagId));
        }
    }

    @Override
    protected void writeJoins(SQLiteDatabase db, Item entity, long rowId) throws SQLException {
        super.writeJoins(db, entity, rowId);
        setItemTags(db, rowId, entity.getTagIds());
    }
    
    /*
     * internal classes
     */
    
    public static class ReadStateChange {
        public final ElementId itemId;
        public final ElementId feedId;
        public final boolean read;
        
        ReadStateChange(String itemId, String feedId, boolean read) {
            this.itemId = new ElementId(itemId);
            this.feedId = new ElementId(feedId);
            this.read = read;
        }
    }
}
