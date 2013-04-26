package com.gettingmobile.google.reader.db;

import android.database.sqlite.SQLiteDatabase;

import java.util.Map;

public class ItemRequestSpecificationTable extends Table {
    public static final String TABLE_NAME = "itemRequestSpecification";
    public static final String ID = "_id";
    public static final String STREAM_ID = "streamId";
    public static final String MAX_AGE_IN_DAYS = "maxAgeInDays";
    public static final String MAX_ITEM_COUNT = "maxItemCount";

    public ItemRequestSpecificationTable() {
        super(TABLE_NAME);
    }

    @Override
    protected void defineColumns(Map<String, String> columns) {
        columns.put(ID, "INTEGER PRIMARY KEY");
        columns.put(STREAM_ID, "TEXT");
        columns.put(MAX_AGE_IN_DAYS, "INTEGER");
        columns.put(MAX_ITEM_COUNT, "INTEGER");
    }

    @Override
    protected void postCommands(SQLiteDatabase db) {
        super.postCommands(db);
        db.execSQL("CREATE UNIQUE INDEX itemRequestSpecificationStreamId ON " + TABLE_NAME + "(streamId)");
    }

    @Override
    public void upgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.upgrade(db, oldVersion, newVersion);
        if (oldVersion < 33) {
            create(db);
        }
    }
}
