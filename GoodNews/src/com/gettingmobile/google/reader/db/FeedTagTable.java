package com.gettingmobile.google.reader.db;

import android.database.sqlite.SQLiteDatabase;

import java.util.Map;

public class FeedTagTable extends Table {
	public static final String TABLE_NAME = "feedTag";
	public static final String FEED_KEY = "feedKey";
	public static final String TAG_KEY = "tagKey";
    public static final String SORT_ORDER = "sortOrder";

	public FeedTagTable() {
		super(TABLE_NAME);
	}
	
	@Override
	protected void defineColumns(Map<String, String> columns) {
		columns.put(FEED_KEY, "INTEGER");
		columns.put(TAG_KEY, "INTEGER");
        columns.put(SORT_ORDER, "INTEGER");
	}

	@Override
	protected void postCommands(SQLiteDatabase db) {
		super.postCommands(db);
		
		db.execSQL("CREATE UNIQUE INDEX " + getTableName() + "Relation ON " + getTableName() + " (feedKey, tagKey)");
        db.execSQL("CREATE UNIQUE INDEX tag2feed ON " + getTableName() + " (tagKey, feedKey)");
        db.execSQL("CREATE INDEX feedKeyIndex ON " + getTableName() + " (feedKey)");
		db.execSQL("CREATE INDEX feedTagKeyIndex ON " + getTableName() + " (tagKey)");		
	}

    @Override
    public void upgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 16) {
            db.execSQL("ALTER TABLE feedTag ADD COLUMN sortOrder INTEGER");            
        }
        if (oldVersion < 30) {
            db.execSQL("DROP INDEX IF EXISTS tag2feed");
            db.execSQL("CREATE UNIQUE INDEX tag2feed ON " + getTableName() + " (tagKey, feedKey)");
        }
        super.upgrade(db, oldVersion, newVersion);
    }
}
