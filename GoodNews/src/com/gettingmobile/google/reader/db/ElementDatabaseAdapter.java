package com.gettingmobile.google.reader.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import com.gettingmobile.google.reader.Element;
import com.gettingmobile.google.reader.ElementId;

import java.util.HashSet;
import java.util.Set;

public abstract class ElementDatabaseAdapter<T extends Element> extends AbstractDatabaseAdapter<T> {
	public ElementDatabaseAdapter(String tableName) {
		super(tableName);
	}

    public Set<ElementId> readAllIds(SQLiteDatabase db) {
        final Cursor c = db.query(getTableName(), new String[] { ElementTable.ID }, null, null, null, null, null);
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

	@Override
    public long getRowKey(Cursor c) {
		return getRowKey(c, ElementTable.KEY);
	}

	@Override
	protected void setRowValues(SQLiteDatabase db, ContentValues columns, T entity, Bundle parameters) {
		columns.put(ElementTable.ID, entity.getId().getId());
		columns.put(ElementTable.TITLE, entity.getTitle());
	}

    @Override
    protected void attachRowId(T entity, long id) {
        entity.setKey(id);
    }

    @Override
    public T readCurrent(Cursor c) {
		final T entity = super.readCurrent(c);
		
		entity.setKey(getRowKey(c));
		entity.setId(new ElementId(c.getString(c.getColumnIndex(ElementTable.ID))));
		entity.setTitle(c.getString(c.getColumnIndex(ElementTable.TITLE)));
		
		return entity;
	}
}
