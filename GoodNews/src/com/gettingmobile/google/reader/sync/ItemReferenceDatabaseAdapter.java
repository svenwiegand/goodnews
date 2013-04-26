package com.gettingmobile.google.reader.sync;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import com.gettingmobile.google.reader.ElementId;
import com.gettingmobile.google.reader.db.AbstractDatabaseAdapter;
import com.gettingmobile.google.reader.db.ItemTagTable;
import com.gettingmobile.google.reader.rest.ItemReference;
import com.gettingmobile.util.StringUtils;

import java.util.*;

import static com.gettingmobile.google.reader.sync.TmpItemReferenceTable.*;

public class ItemReferenceDatabaseAdapter extends AbstractDatabaseAdapter<ItemReference> {
    public ItemReferenceDatabaseAdapter() {
        super(TABLE_NAME);
    }

    public void updateItemReadMarks(SQLiteDatabase db) {
        db.execSQL("UPDATE item SET read=1, syncedRead=1");
        db.execSQL("UPDATE item SET read=0, syncedRead=0 WHERE refId IN (SELECT r.refId FROM tmpItemReference r)");
    }

    public void updateItemTags(SQLiteDatabase db) {
        /*
         * delete all assignments
         */
        db.execSQL("DELETE FROM " + ItemTagTable.TABLE_NAME);

        /*
         * ensure that no feed folders are attached to items
         */
        db.execSQL("DELETE FROM itemTag WHERE tagKey IN (SELECT t._id FROM tag t WHERE t.isFeedFolder<>0)");

        /*
         * update from item references received during sync
         */
        db.execSQL("INSERT INTO " + ItemTagTable.TABLE_NAME + " (itemKey, tagKey) " +
                "SELECT i._id, t._id FROM tmpItemReference tir " +
                "INNER JOIN item i ON i.refId=tir.refId " +
                "INNER JOIN tag t ON t.id=tir.tag " +
                "WHERE tir.tag IS NOT NULL");
    }

    public void deleteKnown(SQLiteDatabase db) {
        db.execSQL("DELETE FROM tmpItemReference WHERE refId IN (SELECT i.refId FROM item i)");
    }

    public void deleteOlder(SQLiteDatabase db, Calendar minTimestamp) {
        db.execSQL("DELETE FROM tmpItemReference WHERE timestamp<?",
                new Object[] {ItemReference.getTimestampUSecByDate(minTimestamp)});
    }

    public void deleteBlacklisted(SQLiteDatabase db) {
        db.execSQL("DELETE FROM tmpItemReference WHERE refId IN (SELECT b.refId FROM itemBlacklist b)");
    }

    public void keepNewest(SQLiteDatabase db, int numberToKeep) {
        db.execSQL("DELETE FROM tmpItemReference WHERE " +
                "refId NOT IN (SELECT r.refId FROM tmpItemReference r ORDER BY r.timestamp DESC LIMIT ?)",
                new String[] {Integer.toString(numberToKeep)});
    }

    public Calendar getMaxTimestamp(SQLiteDatabase db) {
        final Cursor c = db.rawQuery("SELECT MAX(timestamp) FROM tmpItemReference", null);
        try {
            return ItemReference.getDateByTimestampUSec(c.moveToFirst() ? c.getLong(0) : 0);
        } finally {
            c.close();
        }
    }

    public int readCount(SQLiteDatabase db) {
        final Cursor c = db.rawQuery("SELECT COUNT(*) FROM tmpItemReference", null);
        try {
            return c.moveToFirst() ? c.getInt(0) : 0;
        } finally {
            c.close();
        }
    }

    public long[] readAllReferenceIds(SQLiteDatabase db) {
        final Cursor c = db.rawQuery("SELECT refId FROM tmpItemReference", null);
        try {
            final long[] refIds = new long[c.getCount()];
            if (c.moveToFirst()) {
                for (int i = 0; i < refIds.length; ++i) {
                    refIds[i] = c.getLong(0);
                    if (!c.moveToNext()) break;
                }
            }
            return refIds;
        } finally {
            c.close();
        }
    }

    public List<ItemReference> readAll(SQLiteDatabase db) {
        return readList(db.query(TABLE_NAME, null, null, null, null, null, null));
    }

    public Set<ElementId> readTagIds(SQLiteDatabase db) {
        final Cursor c = db.rawQuery("SELECT DISTINCT tag FROM tmpItemReference WHERE tag IS NOT NULL", null);
        try {
            final Set<ElementId> tagIds = new HashSet<ElementId>(c.getCount());
            if (c.moveToFirst()) {
                do {
                    tagIds.add(new ElementId(c.getString(0)));
                } while (c.moveToNext());
            }
            return tagIds;
        } finally {
            c.close();
        }
    }

    public void deleteByTagIds(SQLiteDatabase db, Set<ElementId> tagIds) {
        if (!tagIds.isEmpty()) {
            db.execSQL("DELETE FROM tmpItemReference WHERE tag IN (" + StringUtils.explode(tagIds, ",", "'") + ")");
        }
    }

    @Override
    protected void setRowValues(SQLiteDatabase db, ContentValues columns, ItemReference entity, Bundle parameters) {
        columns.put(REF_ID, entity.getId());
        columns.put(TAG, !entity.getDirectStreamIds().isEmpty() ? entity.getDirectStreamIds().get(0).getId() : null);
        columns.put(TIMESTAMP, entity.getTimestampUSec());
    }

    @Override
    protected void attachRowId(ItemReference entity, long id) {
        // nothing to be done
    }

    @Override
    protected ItemReference create() {
        return new ItemReference();
    }

    @Override
    public ItemReference readCurrent(Cursor c) {
        final ItemReference ref = super.readCurrent(c);

        ref.setId(c.getLong(c.getColumnIndex(REF_ID)));
        final String tag = c.getString(c.getColumnIndex(TAG));
        if (tag != null) {
            final List<ElementId> tags = new ArrayList<ElementId>(1);
            tags.add(new ElementId(tag));
            ref.setDirectStreamIds(tags);
        }
        ref.setTimestampUSec(c.getLong(c.getColumnIndex(TIMESTAMP)));

        return ref;
    }

    @Override
    public long getRowKey(Cursor c) {
        return c.getInt(c.getColumnIndex(KEY));
    }
}
