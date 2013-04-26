package com.gettingmobile.google.reader.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import com.gettingmobile.google.reader.SortedElement;

public abstract class SortedElementDatabaseAdapter<T extends SortedElement> extends ElementDatabaseAdapter<T> {
	protected static final String colUnreadCount = "unreadCount";
	
	public SortedElementDatabaseAdapter(String tableName) {
		super(tableName);
	}

	@Override
	protected void setRowValues(SQLiteDatabase db, ContentValues columns, T entity, Bundle parameters) {
		super.setRowValues(db, columns, entity, parameters);
		columns.put(SortedElementTable.SORT_ID, entity.getSortId());
	}

	@Override
    public T readCurrent(Cursor c) {
		final T entity = super.readCurrent(c);
		
		entity.setSortId(c.getString(c.getColumnIndex(SortedElementTable.SORT_ID)));
		final int unreadCountCol = c.getColumnIndex(colUnreadCount);
		entity.setUnreadCount(unreadCountCol > -1 ? c.getInt(unreadCountCol) : 0);
		
		return entity;
	}
}
