package com.gettingmobile.google.reader.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import com.gettingmobile.google.reader.ElementId;
import com.gettingmobile.google.reader.ElementType;
import com.gettingmobile.google.reader.Tag;
import com.gettingmobile.util.StringUtils;

import java.util.Collection;
import java.util.List;

import static com.gettingmobile.google.reader.db.TagTable.*;

public class TagDatabaseAdapter extends SortedElementDatabaseAdapter<Tag> {
	public TagDatabaseAdapter() {
		super(TagTable.TABLE_NAME);
	}

	public List<Tag> readAll(SQLiteDatabase db) {
		return readList(db.rawQuery(
				"SELECT t.*, " +
                "  (SELECT COUNT(DISTINCT i._id) " +
                "    FROM item i " +
                "    LEFT JOIN feed f ON f.id=i.feedId " +
                "    LEFT JOIN feedTag ft ON ft.feedKey=f._id " +
                "    LEFT JOIN itemTag it ON it.itemKey=i._id " +
                "    WHERE (it.tagKey=t._id OR ft.tagKey=t._id) AND i.read=0) unreadCount " +
				"FROM tag t " +
				"ORDER BY t.sortOrder, lower(t.title)", null));
	}
	
	public List<Tag> readUserLabels(SQLiteDatabase db) {
        return readList(db.rawQuery(
                "SELECT t.*, COUNT(i._id) unreadCount " +
                "FROM tag t " +
                "LEFT OUTER JOIN feedTag ft ON ft.tagKey=t._id " +
                "LEFT OUTER JOIN feed f ON f._id=ft.feedKey " +
                "LEFT OUTER JOIN itemTag it ON it.tagKey=t._id " +
                "LEFT OUTER JOIN item i ON (i._id=it.itemKey OR i.feedId=f.id) AND i.read=0 " +
                "WHERE t.isUserLabel<>0 " +
                "GROUP BY t._id", null));
	}
	
	public List<Tag> readUnreadUserLabels(SQLiteDatabase db) {
		return readList(db.rawQuery(
				"SELECT t.*, COUNT(i._id) unreadCount " +
				"FROM tag t " +
                "LEFT OUTER JOIN feedTag ft ON ft.tagKey=t._id " +
                "LEFT OUTER JOIN feed f ON f._id=ft.feedKey " +
                "LEFT OUTER JOIN itemTag it ON it.tagKey=t._id " +
                "LEFT OUTER JOIN item i ON (i._id=it.itemKey OR i.feedId=f.id) AND i.read=0 " +
				"WHERE t.isUserLabel<>0 " +
                "GROUP BY t._id HAVING (unreadCount>0 OR t.isFeedFolder=0)", null));
	}
	
	public List<Tag> readUserLabelsIgnoreFolders(SQLiteDatabase db) {
		return readList(db.query(
				TABLE_NAME, null, "isUserLabel<>0 AND isFeedFolder=0" , null, null, null, TITLE));
	}
	
	public Tag readById(SQLiteDatabase db, ElementId id) {
		return readFirst(db.query(TABLE_NAME, null, "id=?", new String[] { id.getId() }, null, null, null));
	}

    public long readKeyById(SQLiteDatabase db, ElementId id) {
        final Cursor c = db.query(TABLE_NAME, new String[] {"_id"}, "id=?", new String[] { id.getId() }, null, null, null);
        try {
            return c.moveToFirst() ? c.getLong(0) : 0;
        } finally {
            c.close();
        }
    }

	@Override
    public Tag readCurrent(Cursor c) {
		final Tag label = super.readCurrent(c);
		
		label.setFeedFolder(c.getInt(c.getColumnIndex(IS_FEED_FOLDER)) != 0);
        label.setRootSortOrder(c.getInt(c.getColumnIndex(SORT_ORDER)));

		return label;
	}

    public void updateById(SQLiteDatabase db, Tag tag) {
        db.update(TABLE_NAME, createRowValues(db, tag, null), "id=?", new String[] {tag.getId().getId()});
    }

	public void updateFeedFolderFlags(SQLiteDatabase db) {
		db.execSQL(
				"UPDATE " + TABLE_NAME + " " +
				"SET isFeedFolder=(SELECT COUNT(*) FROM feedTag ft WHERE ft.tagKey=_id)");
	}

    public void deleteById(SQLiteDatabase db, Collection<ElementId> ids) {
        final StringBuilder idList = new StringBuilder();
        for (ElementId id : ids) {
            if (ElementType.LABEL.equals(id.getType())) {
                if (idList.length() > 0) {
                    idList.append(',');
                }
                idList.append('\'').append(id.getId()).append('\'');
            }
        }

        if (idList.length() > 0) {
            db.execSQL(
                    "DELETE FROM itemTag " +
                    "WHERE tagKey IN (SELECT t._id FROM tag t WHERE t.id IN (" +
                            StringUtils.explode(ids, ",", "'") + "))");
            db.delete(TABLE_NAME, "id IN (" + idList.toString() + ")", null);
        }
    }

	@Override
	protected void setRowValues(SQLiteDatabase db, ContentValues columns, Tag entity, Bundle parameters) {
		super.setRowValues(db, columns, entity, parameters);
		columns.put(IS_USER_LABEL, entity.isUserLabel());
        columns.put(IS_FEED_FOLDER, entity.isFeedFolder());
        columns.put(SORT_ORDER, entity.getRootSortOrder());
	}

	@Override
	protected Tag create() {
		return new Tag();
	}	
}
