package com.gettingmobile.goodnews.investigation;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public final class InvestigationLog {
    private static InvestigationLog instance = null;
    private final InvestigationDatabaseHelper dbHelper;
    private final InvestigationLogEntryDatabaseAdapter adapter = new InvestigationLogEntryDatabaseAdapter();

    public static void createInstance(Context context) {
        instance = new InvestigationLog(context);
    }

    public static void log(String tag, String message) {
        if (instance != null) {
            instance.logHelper(tag, message);
        }
    }

    public static String readLog(String... tags) {
        return instance != null ? instance.readLogHelper(tags) : "";
    }

    public InvestigationLog(Context context) {
        dbHelper = new InvestigationDatabaseHelper(context);
    }

    private void logHelper(String tag, String message) {
        final String msg = Thread.currentThread().getName() + ": " + message;
        Log.d(tag, msg);
        final SQLiteDatabase db = dbHelper.getDatabase();
        db.beginTransaction();
        try {
            adapter.writeEntry(db, new InvestigationLogEntry(tag, msg));
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private String readLogHelper(String... tags) {
        final StringBuilder msg = new StringBuilder();
        for (InvestigationLogEntry e : adapter.readByTags(dbHelper.getDatabase(), tags)) {
            if (msg.length() > 0) {
                msg.append('\n');
            }
            msg.append(e.toString());
        }
        return msg.toString();
    }
}
