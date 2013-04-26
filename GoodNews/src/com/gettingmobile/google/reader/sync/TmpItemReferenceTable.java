package com.gettingmobile.google.reader.sync;

import android.database.sqlite.SQLiteDatabase;
import com.gettingmobile.google.reader.db.Table;

import java.util.Map;

class TmpItemReferenceTable extends Table {
    public static final String TABLE_NAME = "tmpItemReference";
    public static final String KEY = "_id";
    public static final String REF_ID = "refId";
    public static final String TAG = "tag";
    public static final String TIMESTAMP = "timestamp";
    public static final String UNREAD = "unread";

    TmpItemReferenceTable() {
        super(TABLE_NAME);
    }

    @Override
    protected void defineColumns(Map<String, String> columns) {
        columns.put(KEY, "INTEGER PRIMARY KEY");
        columns.put(REF_ID, "INTEGER");
        columns.put(TAG, "TEXT");
        columns.put(TIMESTAMP, "INTEGER");
        columns.put(UNREAD, "INTEGER");
    }

    @Override
    protected void postCommands(SQLiteDatabase db) {
        super.postCommands(db);

        db.execSQL("CREATE UNIQUE INDEX refIdTagIndex ON " + TABLE_NAME + " (refId, tag)");
    }
}
