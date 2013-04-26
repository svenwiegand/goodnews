package com.gettingmobile.goodnews.investigation;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import com.gettingmobile.google.reader.db.AbstractDatabaseAdapter;

import java.util.ArrayList;
import java.util.List;

import static com.gettingmobile.goodnews.investigation.InvestigationLogTable.*;

final class InvestigationLogEntryDatabaseAdapter extends AbstractDatabaseAdapter<InvestigationLogEntry> {
    public static final long MAX_ENTRY_AGE_MILLIS = 12 /*h*/ * 60 /*min*/ * 60 /*s*/ * 1000 /*ms*/;

    public InvestigationLogEntryDatabaseAdapter() {
        super(TABLE_NAME);
    }

    public void cleanupOld(SQLiteDatabase db) {
        final long minTime = System.currentTimeMillis() - MAX_ENTRY_AGE_MILLIS;
        db.delete(TABLE_NAME, TIMESTAMP + "<?", new String[] { Long.toString(minTime) });
    }

    public void writeEntry(SQLiteDatabase db, InvestigationLogEntry entry) {
        cleanupOld(db);
        write(db, entry);
    }

    public List<InvestigationLogEntry> readByTags(SQLiteDatabase db, String... tags) {
        if (tags.length < 1)
            return new ArrayList<InvestigationLogEntry>(0);
        
        final StringBuilder tagList = new StringBuilder();
        for (String tag : tags) {
            if (tagList.length() > 0) {
                tagList.append(',');
            }
            tagList.append(tag);
        }
        return readList(db.query(TABLE_NAME, null, TAG + " IN (?)", new String[] { tagList.toString() }, null, null, TIMESTAMP + " DESC"));
    }

    @Override
    protected void setRowValues(SQLiteDatabase db, ContentValues columns, InvestigationLogEntry entity, Bundle parameters) {
        columns.put(TAG, entity.getTag());
        columns.put(TIMESTAMP, entity.getTimestamp());
        columns.put(MESSAGE, entity.getMessage());
    }

    @Override
    public InvestigationLogEntry readCurrent(Cursor c) {
        final InvestigationLogEntry e = super.readCurrent(c);
        e.setTag(c.getString(c.getColumnIndex(TAG)));
        e.setTimestamp(c.getLong(c.getColumnIndex(TIMESTAMP)));
        e.setMessage(c.getString(c.getColumnIndex(MESSAGE)));
        return e;
    }

    @Override
    protected void attachRowId(InvestigationLogEntry entity, long id) {
        entity.setId(id);
    }

    @Override
    protected InvestigationLogEntry create() {
        return new InvestigationLogEntry();
    }

    @Override
    public long getRowKey(Cursor c) {
        return c.getLong(c.getColumnIndex(ID));
    }
}
