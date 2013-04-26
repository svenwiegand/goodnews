package com.gettingmobile.google.reader.db;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.Collection;
import java.util.List;

public interface DatabaseAdapter<T> {
	void delete(SQLiteDatabase db) throws SQLException;
	long write(SQLiteDatabase db, T entity) throws SQLException;
	void writeList(SQLiteDatabase db, Collection<T> entities) throws SQLException;
	T readFirst(Cursor c);
	List<T> readList(Cursor c);
    T readCurrent(Cursor c);
    long getRowKey(Cursor c);
}
