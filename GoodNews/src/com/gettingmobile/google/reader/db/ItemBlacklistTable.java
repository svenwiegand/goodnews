package com.gettingmobile.google.reader.db;

import android.database.sqlite.SQLiteDatabase;

import java.util.Map;

public class ItemBlacklistTable extends Table {
    public static final String TABLE_NAME = "itemBlacklist";
    public static final String REF_ID = "refId";

    public ItemBlacklistTable() {
        super(TABLE_NAME);
    }

    @Override
    protected void defineColumns(Map<String, String> columns) {
        columns.put(REF_ID, "INTEGER");
    }

    @Override
    public void upgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.upgrade(db, oldVersion, newVersion);

        if (oldVersion < 34) {
            create(db);
        }
    }
}
