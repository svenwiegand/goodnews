package com.gettingmobile.google.reader.db;

import android.database.sqlite.SQLiteDatabase;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public abstract class Table {
	public static long INVALID_ID = -1;
	private final String tableName;
	
	
	public Table(String tableName) {
		this.tableName = tableName;
	}
	
	public String getTableName() {
		return tableName;
	}
	
	protected abstract void defineColumns(Map<String, String> columns);
	
	protected void postCommands(SQLiteDatabase db) {
		// do nothing by default
	}
	
	public void upgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // do nothing by default
	}
	
	public void create(SQLiteDatabase db) {
		drop(db);
		final Map<String, String> columns = new LinkedHashMap<String, String>();
		defineColumns(columns);
		final StringBuilder createStmt = new StringBuilder("CREATE TABLE " + getTableName() + " (\n");
		for (final Iterator<Entry<String, String>> it = columns.entrySet().iterator(); it.hasNext(); ) {
			final Entry<String, String> column = it.next();
			createStmt.append(column.getKey()).append(' ').append(column.getValue());
			createStmt.append(it.hasNext() ? ",\n" : "\n");
		}
		createStmt.append(')');
		db.execSQL(createStmt.toString());
		postCommands(db);
		init(db);
	}

    public void drop(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + getTableName());
    }
	
	public void delete(SQLiteDatabase db) {
		db.delete(tableName, null, null);
	}
	
	public void clean(SQLiteDatabase db) {
		delete(db);
		init(db);
	}
	
	public void init(SQLiteDatabase db) {
		// do nothing by default
	}
}
