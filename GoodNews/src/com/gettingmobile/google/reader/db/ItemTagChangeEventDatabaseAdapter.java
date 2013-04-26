package com.gettingmobile.google.reader.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Bundle;
import com.gettingmobile.google.reader.ElementId;
import com.gettingmobile.google.reader.ItemTagChangeEvent;
import com.gettingmobile.google.reader.TagChangeOperation;

import java.util.List;

import static com.gettingmobile.google.reader.db.ItemTagChangeEventTable.*;
import static com.gettingmobile.google.reader.db.Table.INVALID_ID;

public class ItemTagChangeEventDatabaseAdapter extends AbstractDatabaseAdapter<ItemTagChangeEvent> {
    public static final ItemTagChangeEventDatabaseAdapter INSTANCE = new ItemTagChangeEventDatabaseAdapter();

	public static final class Group {
		public final ElementId tag;
		public final TagChangeOperation operation;
		
		Group(String tag, int operationOrdinal) {
			this.tag = new ElementId(tag);
			this.operation = TagChangeOperation.values()[operationOrdinal];
		}
	}
	
	public ItemTagChangeEventDatabaseAdapter() {
		super(TABLE_NAME);
	}

	public ItemTagChangeEvent readFirst(SQLiteDatabase db) {
		return readFirst(db.query(TABLE_NAME, null, null, null, null, null, "id", "1"));
	}
	
	public ItemTagChangeEvent readNext(SQLiteDatabase db, long prevId) {
		return readFirst(db.query(TABLE_NAME, null, "id > " + prevId, null, null, null, "id", "1"));
	}
	
	public List<ItemTagChangeEvent> readAll(SQLiteDatabase db) {
		return readList(db.query(TABLE_NAME, null, null, null, null, null, "id"));
	}
	
	public Group readFirstGroup(SQLiteDatabase db) {
		final Cursor c = db.query(TABLE_NAME, 
				new String[] { TAG_ID, OPERATION }, null, null, TAG_ID + ", " + OPERATION, null, ID);
		try {
			return c.moveToFirst() ? new Group(c.getString(0), c.getInt(1)) : null;
		} finally {
			c.close();
		}
	}
	
	public int readGroupCount(SQLiteDatabase db, int limit) {
		final Cursor c = db.query(TABLE_NAME, 
				new String[] { TAG_ID, OPERATION, "COUNT(itemId)" }, null, null, TAG_ID + ", " + OPERATION, null, ID);
		try {
            if (limit <= 0) {
			    return c.getCount();
            } else {
                int count = 0;
                while (c.moveToNext()) {
                    final int groupItemCount = c.getInt(2);
                    count += 1 + (groupItemCount / limit);
                }
                return count;
            }
		} finally {
			c.close();
		}
	}
    
    public int readGroupCount(SQLiteDatabase db) {
        return readGroupCount(db, 0);
    }
	
	public boolean readHasChanges(SQLiteDatabase db) {
		return readGroupCount(db) > 0;
	}
	
	public List<ItemTagChangeEvent> readByTagAndOperation(SQLiteDatabase db, ElementId tag, TagChangeOperation operation, int limit) {
		return readList(db.query(TABLE_NAME, null, "tagId=? AND operation=?",
				new String[] { tag.getId(), Integer.toString(operation.ordinal()) }, null, null, ID, 
                limit <= 0 ? null : Integer.toString(limit)));
	}
    
    public List<ItemTagChangeEvent> readByTagAndOperation(SQLiteDatabase db, ElementId tag, TagChangeOperation operation) {
        return readByTagAndOperation(db, tag, operation, 0);
    }

	protected long getId(SQLiteDatabase db, ItemTagChangeEvent event) {
		final Cursor c = db.query(
				TABLE_NAME, new String[] { ID }, "feedId=? AND itemId=? AND tagId=?", 
				new String[] { event.getFeedId().getId(), event.getItemId().getId(), event.getTagId().getId()}, 
				null, null, null);
		try {
			return c.moveToFirst() ? c.getLong(0) : INVALID_ID;
		} finally {
			c.close();
		}
	}

	@Override
	public long write(SQLiteDatabase db, ItemTagChangeEvent event) {
		final long existingId = getId(db, event);
		if (existingId != INVALID_ID) {
			final ContentValues v = new ContentValues();
			v.put(OPERATION, event.getOperation().ordinal());
			db.update(TABLE_NAME, v, "id=?", new String[] { Long.toString(existingId) });
            return existingId;
		} else {
			return super.write(db, event);
		}
	}

    public SQLiteStatement compileInsertStatement(SQLiteDatabase db) {
        return db.compileStatement(
                "INSERT INTO " + TABLE_NAME + " (feedId, itemId, operation, tagId) VALUES (?, ?, ?, ?)");
    }

    public void insert(SQLiteStatement statement, ItemTagChangeEvent event) {
        statement.bindString(1, event.getFeedId().getId());
        statement.bindString(2, event.getItemId().getId());
        statement.bindLong(3, event.isAddOperation() ? 1 : 0);
        statement.bindString(4, event.getTagId().getId());
        statement.executeInsert();
    }
	
	public void delete(SQLiteDatabase db, ElementId itemId, ElementId tag, TagChangeOperation op) {
		db.delete(TABLE_NAME, 
				"itemId=? AND tagId=? AND operation=?", 
				new String[] { itemId.getId(), tag.getId(), Integer.toString(op.ordinal()) });
	}

	@Override
	protected void setRowValues(SQLiteDatabase db, ContentValues columns, ItemTagChangeEvent event, Bundle parameters) {
		columns.put(FEED_ID, event.getFeedId().getId());
		columns.put(ITEM_ID, event.getItemId().getId());
		columns.put(OPERATION, event.getOperation().ordinal());
		columns.put(TAG_ID, event.getTagId().getId());
	}

    @Override
    protected void attachRowId(ItemTagChangeEvent entity, long id) {
        entity.setId(id);
    }

    @Override
	protected ItemTagChangeEvent create() {
		return new ItemTagChangeEvent();
	}

	@Override
    public ItemTagChangeEvent readCurrent(Cursor c) {
		final ItemTagChangeEvent event = super.readCurrent(c);
		
		event.setId(getRowKey(c));
		event.setFeedId(new ElementId(c.getString(c.getColumnIndex(FEED_ID))));
		event.setItemId(new ElementId(c.getString(c.getColumnIndex(ITEM_ID))));
		event.setOperation(TagChangeOperation.values()[c.getInt(c.getColumnIndex(OPERATION))]);
		event.setTagId(new ElementId(c.getString(c.getColumnIndex(TAG_ID))));
		
		return event;
	}

	@Override
	public long getRowKey(Cursor c) {
		return getRowKey(c, ID);
	}	
}
