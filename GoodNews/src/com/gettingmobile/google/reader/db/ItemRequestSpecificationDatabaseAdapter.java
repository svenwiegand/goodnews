package com.gettingmobile.google.reader.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import com.gettingmobile.google.reader.ItemRequestSpecification;
import com.gettingmobile.google.reader.ElementId;

import java.util.List;

import static com.gettingmobile.google.reader.db.ItemRequestSpecificationTable.*;

public class ItemRequestSpecificationDatabaseAdapter extends AbstractDatabaseAdapter<ItemRequestSpecification> {
    public ItemRequestSpecificationDatabaseAdapter() {
        super(TABLE_NAME);
    }

    /*
     * operations
     */

    public void insertOrUpdate(SQLiteDatabase db, ItemRequestSpecification entity) throws SQLException {
        db.execSQL("INSERT OR REPLACE INTO itemRequestSpecification (streamId, maxAgeInDays, maxItemCount)" +
                "VALUES (?, ?, ?)",
                new Object[] {entity.getStreamId(), entity.getMaxAgeInDays(), entity.getMaxItemCount()});
    }

    public List<ItemRequestSpecification> readAll(SQLiteDatabase db) {
        return readList(db.rawQuery("SELECT * FROM itemRequestSpecification", null));
    }
    
    public void delete(SQLiteDatabase db, ElementId streamId) {
        db.execSQL("DELETE FROM itemRequestSpecification WHERE streamId=?", new String[] {streamId.getId()});
    }

    /*
     * mapping
     */

    @Override
    protected void setRowValues(SQLiteDatabase db, ContentValues columns, ItemRequestSpecification spec, Bundle parameters) {
        columns.put(STREAM_ID, spec.getStreamId().getId());
        columns.put(MAX_AGE_IN_DAYS, spec.getMaxAgeInDays());
        columns.put(MAX_ITEM_COUNT, spec.getMaxItemCount());
    }

    @Override
    protected void attachRowId(ItemRequestSpecification entity, long id) {
        // we are not interested in the id
    }

    @Override
    public long getRowKey(Cursor c) {
        return c.getLong(c.getColumnIndex(ID));
    }

    @Override
    protected ItemRequestSpecification create() {
        return new ItemRequestSpecification();
    }

    @Override
    public ItemRequestSpecification readCurrent(Cursor c) {
        final ItemRequestSpecification spec = super.readCurrent(c);
        
        spec.setStreamId(new ElementId(c.getString(c.getColumnIndex(STREAM_ID))));
        spec.setMaxAgeInDays(c.getInt(c.getColumnIndex(MAX_AGE_IN_DAYS)));
        spec.setMaxItemCount(c.getInt(c.getColumnIndex(MAX_ITEM_COUNT)));
        
        return spec;
    }
}
