package com.gettingmobile.google.reader.db;

import android.database.sqlite.SQLiteDatabase;

import java.util.Map;

public class FeedTable extends SortedElementTable {
	public static final String TABLE_NAME = "feed";
	public static final String HTML_URL = "htmlUrl";
    public static final String ROOT_SORT_ORDER = "rootSortOrder";

	public FeedTable() {
		super(TABLE_NAME);
	}
	
	@Override
	protected void defineColumns(Map<String, String> columns) {
		super.defineColumns(columns);
		columns.put(HTML_URL, "TEXT");
        columns.put(ROOT_SORT_ORDER, "INTEGER");
	}

    @Override
    public void upgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 18) {
            db.execSQL("ALTER TABLE feed ADD COLUMN rootSortOrder INTEGER");
        }

        super.upgrade(db, oldVersion, newVersion);
    }
}
