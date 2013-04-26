package com.gettingmobile.google.reader.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import com.gettingmobile.google.reader.ElementId;
import com.gettingmobile.google.reader.Feed;
import com.gettingmobile.google.reader.Tag;

import java.util.*;

public class FeedDatabaseAdapter extends SortedElementDatabaseAdapter<Feed> {
	private static final String TAG_ID = "tagId";
	private final TagDatabaseAdapter tagAdapter = new TagDatabaseAdapter();
	
	public FeedDatabaseAdapter() {
		super(FeedTable.TABLE_NAME);
	}

    public EntityCursor<Feed> cursorAll(SQLiteDatabase db) {
        return new SimpleEntityCursor<Feed>(new FeedCursorAdapter(), db.rawQuery(
                "SELECT f.*, COUNT(i._id) unreadCount " +
                        "FROM feed f " +
                        "LEFT OUTER JOIN item i ON i.feedId=f.id AND i.read=0 " +
                        "GROUP BY f.title COLLATE NOCASE, f._id", null));
    }

	/**
	 * Reads all feeds from the database. The returned feeds will not contain any tags.
	 * @param db the database to read from.
	 * @return all feeds from the database. May be empty but never null.
	 */
	public List<Feed> readAll(SQLiteDatabase db) {
		return readList(cursorAll(db));
	}

    public EntityCursor<Feed> cursorAllUnread(SQLiteDatabase db) {
        return new SimpleEntityCursor<Feed>(new FeedCursorAdapter(), db.rawQuery(
                "SELECT f.*, COUNT(i._id) unreadCount " +
                        "FROM feed f " +
                        "LEFT OUTER JOIN item i ON i.feedId=f.id AND i.read=0 " +
                        "GROUP BY f.title COLLATE NOCASE, f._id HAVING unreadCount>0", null));
    }

	/**
	 * Reads all feeds containing unread items from the database. The returned feeds will not contain any tags.
	 * @param db the database to read from.
	 * @return all feeds from the database. May be empty but never null.
	 */
	public List<Feed> readAllUnread(SQLiteDatabase db) {
		return readList(cursorAllUnread(db));
	}
    
    public EntityCursor<Feed> cursorByTag(SQLiteDatabase db, long tagKey, boolean sortByDragAndDropOrder) {
        final String orderBy = sortByDragAndDropOrder ? "ft.sortOrder, lower(f.title)" : "lower(f.title)";
        return new SimpleEntityCursor<Feed>(new FeedCursorAdapter(), db.rawQuery(
                "SELECT f.*, COUNT(i._id) unreadCount " +
                        "FROM feed f " +
                        "INNER JOIN feedTag ft ON ft.feedKey = f._id AND ft.tagKey = " + Long.toString(tagKey) + " " +
                        "LEFT OUTER JOIN item i ON i.feedId=f.id AND i.read=0 " +
                        "GROUP BY f._id " +
                        "ORDER BY " + orderBy, null));
    }

	/**
	 * Reads all feeds with the specified tag from the database. The returned feeds will not contain any tags.
	 * @param db the database to read from.
	 * @param tagKey the key of the tag to read the feeds for.
     * @param sortByDragAndDropOrder whether to sort according to greader's drag and drop order or alphabetically.
	 * @return the specified feeds from the database. May be empty but never null.
	 */	
	public List<Feed> readByTag(SQLiteDatabase db, long tagKey, boolean sortByDragAndDropOrder) {
        return readList(cursorByTag(db, tagKey, sortByDragAndDropOrder));
	}

    public List<Feed> readByTag(SQLiteDatabase db, long tagKey) {
        return readByTag(db, tagKey, true);
    }

    public EntityCursor<Feed> cursorUnreadByTag(SQLiteDatabase db, long tagKey, boolean sortByDragAndDropOrder) {
        final String orderBy = sortByDragAndDropOrder ? "ft.sortOrder, lower(f.title)" : "lower(f.title)";
        return new SimpleEntityCursor<Feed>(new FeedCursorAdapter(), db.rawQuery(
                "SELECT f.*, COUNT(i._id) unreadCount " +
                        "FROM feed f " +
                        "INNER JOIN feedTag ft ON ft.feedKey = f._id AND ft.tagKey = " + Long.toString(tagKey) + " " +
                        "LEFT OUTER JOIN item i ON i.feedId=f.id AND i.read=0 " +
                        "GROUP BY f._id " +
                        "HAVING unreadCount>0 " +
                        "ORDER BY " + orderBy, null));
    }

	/**
	 * Reads all feeds with the specified tag containing unread items from the database. The returned feeds will not 
	 * contain any tags.
	 * @param db the database to read from.
	 * @param tagKey the key of the tag to read the feeds for.
     * @param sortByDragAndDropOrder whether to sort according to greader's drag and drop order or alphabetically.
	 * @return the specified feeds from the database. May be empty but never null.
	 */	
	public List<Feed> readUnreadByTag(SQLiteDatabase db, long tagKey, boolean sortByDragAndDropOrder) {
        return readList(cursorUnreadByTag(db, tagKey, sortByDragAndDropOrder));
	}

    public List<Feed> readUnreadByTag(SQLiteDatabase db, long tagKey) {
        return readUnreadByTag(db, tagKey, true);
    }

    public List<Feed> readWithoutTag(SQLiteDatabase db) {
        return readList(db.rawQuery(
//                "SELECT f.*, COUNT(ft.tagKey) tagCount, COUNT(i._id) unreadCount " +
//                "FROM feed f " +
//                "LEFT OUTER JOIN feedTag ft ON ft.feedKey=f._id " +
//                "LEFT OUTER JOIN item i ON i.feedId=f.id AND i.read=0 " +
//                "GROUP BY f._id HAVING tagCount=0", null));
                "SELECT f.*, (SELECT COUNT(*) FROM item i WHERE i.feedId=f.id AND i.read=0) unreadCount " +
                "FROM feed f " +
                "WHERE (SELECT COUNT(*) FROM feedTag ft WHERE ft.feedKey=f._id)=0 " +
                "ORDER BY lower(f.title)", null));
    }

    public List<Feed> readUnreadWithoutTag(SQLiteDatabase db) {
        return readList(db.rawQuery(
//                "SELECT f.*, COUNT(ft.tagKey) tagCount, COUNT(i._id) unreadCount " +
//                "FROM feed f " +
//                "LEFT OUTER JOIN feedTag ft ON ft.feedKey=f._id " +
//                "LEFT OUTER JOIN item i ON i.feedId=f.id AND i.read=0 " +
//                "GROUP BY f._id HAVING tagCount=0 AND unreadCount>0", null));
                "SELECT f.*, (SELECT COUNT(*) FROM item i WHERE i.feedId=f.id AND i.read=0) unreadCount " +
                "FROM feed f " +
                "WHERE (SELECT COUNT(*) FROM feedTag ft WHERE ft.feedKey=f._id)=0 AND unreadCount>0 " +
                "ORDER BY lower(f.title)", null));
    }

	public Map<String, String> readTitles(SQLiteDatabase db) {
		final Cursor c = db.query(
				FeedTable.TABLE_NAME, new String[] { FeedTable.ID, FeedTable.TITLE }, null, null, null, null, null);
		try {
			final Map<String, String> map = new HashMap<String, String>();
			if (c.moveToFirst()) do {
				map.put(c.getString(0), c.getString(1));
			} while (c.moveToNext());
			return map;
		} finally {
			c.close();
		}
	}
	
	public String readTitle(SQLiteDatabase db, ElementId feedId) {
		final Cursor c = db.query(
				FeedTable.TABLE_NAME, new String[] { FeedTable.TITLE }, 
				FeedTable.ID + "=?", new String[] { feedId.getId() }, null, null, null);
		try {
			return c.moveToFirst() ? c.getString(0) : null;
		} finally {
			c.close();
		}
	}
	
	public Set<ElementId> readFeedTags(SQLiteDatabase db, ElementId feedId) {
		final Cursor c = db.rawQuery(
				"SELECT t.id " +
				"FROM feed f " +
				"INNER JOIN feedTag ft ON ft.feedKey = f._id " +
				"INNER JOIN tag t ON t._id = ft.tagKey " +
				"WHERE f.id = ? ", new String[] { feedId.getId() });
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

	@Override
	protected void setRowValues(SQLiteDatabase db, ContentValues columns, Feed f, Bundle parameters) {
		super.setRowValues(db, columns, f, parameters);
		columns.put(FeedTable.HTML_URL, f.getHtmlUrl());
        columns.put(FeedTable.ROOT_SORT_ORDER, f.getRootSortOrder());
	}

	@Override
	protected void writeJoins(SQLiteDatabase db, Feed entity, long rowId) throws SQLException {
		super.writeJoins(db, entity, rowId);
		
		db.delete(FeedTagTable.TABLE_NAME, FeedTagTable.FEED_KEY + "=?", new String[]{Long.toString(rowId)});
		for (ElementId tagId : entity.getTagIds()) {
			if (Tag.isUsed(tagId)) {
				db.execSQL(
						"INSERT INTO " + FeedTagTable.TABLE_NAME + " (feedKey, tagKey, sortOrder) " +
						"VALUES (?, (SELECT t._id FROM tag t WHERE t.id=?), ?)",
						new String[] { Long.toString(rowId), tagId.getId(), Integer.toString(entity.getSortOrder(tagId)) });
			}
		}
	}

    @Override
    public void writeList(SQLiteDatabase db, Collection<Feed> entities) throws SQLException {
        super.writeList(db, entities);
        tagAdapter.updateFeedFolderFlags(db);
    }

    public void rewrite(SQLiteDatabase db, Collection<Feed> feeds) {
        new FeedTagTable().clean(db);
        new FeedTable().clean(db);
        super.writeList(db, feeds);
        tagAdapter.updateFeedFolderFlags(db);
    }

	@Override
	protected Feed create() {
		return new Feed();
	}

	@Override
    public Feed readCurrent(Cursor c) {
		final Feed feed = super.readCurrent(c);
		
		feed.setHtmlUrl(c.getString(c.getColumnIndex(FeedTable.HTML_URL)));
        feed.setRootSortOrder(c.getInt(c.getColumnIndex(FeedTable.ROOT_SORT_ORDER)));

		final int tagIdCol = c.getColumnIndex(TAG_ID);
		if (tagIdCol >= 0) {
			final String tagId = c.getString(tagIdCol);
			if (tagId != null) {			
				feed.getTagIds().add(new ElementId(c.getString(tagIdCol)));
			}
		}
		
		return feed;
	}

	@Override
    public void readJoinCurrent(Feed current, Cursor c) {
		super.readJoinCurrent(current, c);

        final ElementId tagId = new ElementId(c.getString(c.getColumnIndex(TAG_ID)));
		current.getTagIds().add(tagId);
        current.setSortOrder(tagId, c.getInt(c.getColumnIndex(FeedTagTable.SORT_ORDER)));
	}
}
