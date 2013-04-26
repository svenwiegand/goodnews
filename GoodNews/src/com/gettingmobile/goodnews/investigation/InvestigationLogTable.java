package com.gettingmobile.goodnews.investigation;

import android.database.sqlite.SQLiteDatabase;
import com.gettingmobile.google.reader.db.Table;

import java.util.Map;

final class InvestigationLogTable extends Table {
    public static final String TABLE_NAME = "investigationLog";
    public static final String ID = "id";
    public static final String TAG = "tag";
    public static final String TIMESTAMP = "timestamp";
    public static final String MESSAGE = "message";

    public InvestigationLogTable() {
        super(TABLE_NAME);
    }

    @Override
    protected void defineColumns(Map<String, String> columns) {
        columns.put(ID, "INTEGER PRIMARY KEY");
        columns.put(TAG, "TEXT");
        columns.put(TIMESTAMP, "INTEGER");
        columns.put(MESSAGE, "TEXT");
    }

    @Override
    protected void postCommands(SQLiteDatabase db) {
        super.postCommands(db);

        db.execSQL("CREATE INDEX tag ON " + TABLE_NAME + " (tag)");
    }
}
