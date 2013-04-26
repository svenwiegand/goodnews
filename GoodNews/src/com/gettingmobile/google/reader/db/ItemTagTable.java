package com.gettingmobile.google.reader.db;

import java.util.Map;

import android.database.sqlite.SQLiteDatabase;

public class ItemTagTable extends Table {
	public static final String TABLE_NAME = "itemTag";
	public static final String ITEM_KEY = "itemKey";
	public static final String TAG_KEY = "tagKey";

	public ItemTagTable() {
		super(TABLE_NAME);
	}
	
	@Override
	protected void defineColumns(Map<String, String> columns) {
		columns.put(ITEM_KEY, "INTEGER");
		columns.put(TAG_KEY, "INTEGER");
	}

	@Override
	protected void postCommands(SQLiteDatabase db) {
		super.postCommands(db);
		
		db.execSQL("CREATE UNIQUE INDEX " + getTableName() + "Relation ON " + getTableName() + " (itemKey, tagKey)");
        db.execSQL("CREATE UNIQUE INDEX tag2item ON " + getTableName() + " (tagKey, itemKey)");
        db.execSQL("CREATE INDEX itemKeyIndex ON " + getTableName() + " (itemKey)");
		db.execSQL("CREATE INDEX itemTagKeyIndex ON " + getTableName() + " (tagKey)");		
	}

    @Override
    public void upgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.upgrade(db, oldVersion, newVersion);

        if (oldVersion < 30) {
            db.execSQL("CREATE UNIQUE INDEX tag2item ON " + getTableName() + " (tagKey, itemKey)");
        }
        if (oldVersion < 31) {
            /*
             * delete feed folder assignments to articles
             */
            db.execSQL("DELETE FROM itemTag WHERE tagKey IS NULL");
            db.execSQL("DELETE FROM itemTag WHERE tagKey IN (SELECT t._id FROM tag t WHERE t.isFeedFolder<>0)");
        }
    }
}
