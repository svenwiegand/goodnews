package com.gettingmobile.google.reader.db;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import com.gettingmobile.google.reader.ElementId;

public class ItemBlacklistDatabaseAdapter {
    public void blacklistItem(SQLiteDatabase db, ElementId itemId) {
        blacklistItem(db, itemId.getItemReferenceId());
    }

    public void blacklistItem(SQLiteDatabase db, long itemReferenceId) {
        db.execSQL("INSERT OR IGNORE INTO itemBlacklist (refId) VALUES (?)", new Long[]{itemReferenceId});
    }

   	public void delete(SQLiteDatabase db) throws SQLException {
   		db.delete(ItemBlacklistTable.TABLE_NAME, null, null);
   	}
}
