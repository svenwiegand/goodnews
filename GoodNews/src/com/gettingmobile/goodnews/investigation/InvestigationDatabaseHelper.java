package com.gettingmobile.goodnews.investigation;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.gettingmobile.google.reader.db.Table;

import java.util.ArrayList;
import java.util.List;

final class InvestigationDatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "goodnews-investigation";
    public static final int DATABASE_VERSION = 1;
    private final List<Table> tables = new ArrayList<Table>();

    InvestigationDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        tables.add(new InvestigationLogTable());
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("db", "creating database ...");
        db.beginTransaction();
        try {
            for (Table table : tables) {
                table.create(db);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        Log.d("db", "database has been created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("db", "updating database ...");
        db.beginTransaction();
        try {
            for (Table table : tables) {
                table.upgrade(db, oldVersion, newVersion);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        Log.d("db", "database has been updated");
    }

    public SQLiteDatabase getDatabase() {
        return getWritableDatabase();
    }

    @Override
    public void close() {
        super.close();
    }
}
