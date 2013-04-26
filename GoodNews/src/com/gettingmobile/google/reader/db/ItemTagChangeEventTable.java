package com.gettingmobile.google.reader.db;

import android.database.sqlite.SQLiteDatabase;

import java.util.Map;

public class ItemTagChangeEventTable extends Table {
	public static final String TABLE_NAME = "itemTagChangeEvent";	
	public static final String ID = "id";
	public static final String FEED_ID = "feedId";
	public static final String ITEM_ID = "itemId";
	public static final String OPERATION = "operation";
	public static final String TAG_ID = "tagId";

	public ItemTagChangeEventTable() {
		super(TABLE_NAME);
	}
	
	@Override
	protected void defineColumns(Map<String, String> columns) {
		columns.put(ID, "INTEGER PRIMARY KEY");
		columns.put(FEED_ID, "TEXT");
		columns.put(ITEM_ID, "TEXT");
		columns.put(OPERATION, "INTEGER");
		columns.put(TAG_ID, "TEXT");
	}

	@Override
	protected void postCommands(SQLiteDatabase db) {
		super.postCommands(db);

        // pre version 25
		//db.execSQL("CREATE INDEX tagOp ON " + TABLE_NAME + " (tagId, operation)");
		//db.execSQL("CREATE UNIQUE INDEX itemTagOp ON " + TABLE_NAME + " (itemId, tagId, operation)");
	}

    @Override
    public void upgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.upgrade(db, oldVersion, newVersion);
        if (oldVersion < 25) {
            db.execSQL("DROP INDEX IF EXISTS tagOp");
            db.execSQL("DROP INDEX IF EXISTS itemTagOp");
        }
    }
}
