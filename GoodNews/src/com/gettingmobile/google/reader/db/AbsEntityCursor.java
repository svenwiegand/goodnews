package com.gettingmobile.google.reader.db;

import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;

public abstract class AbsEntityCursor<T> implements EntityCursor<T> {
    protected final android.database.Cursor cursor;
    protected final EntityCursorAdapter<T> adapter;

    public AbsEntityCursor(EntityCursorAdapter<T> adapter, Cursor cursor) {
        this.cursor = cursor;
        this.adapter = adapter;
        if (adapter != null) {
            adapter.init(cursor);
        }
    }

    @Override
    public void close() {
        cursor.close();
    }

    @Override
    public boolean isClosed() {
        return cursor.isClosed();
    }

    @Override
    public boolean isGroupHeader() {
        return false;
    }

    @Override
    public String getGroupTitle() {
        return null;
    }

    @Override
    public long getEntityId() {
        return adapter.readEntityId(cursor);
    }

    @Override
    public T getEntity() {
        return adapter.readEntity(cursor);
    }

    /*
     * observers
     */

    @Override
    public void registerContentObserver(ContentObserver observer) {
        cursor.registerContentObserver(observer);
    }

    @Override
    public void unregisterContentObserver(ContentObserver observer) {
        cursor.unregisterContentObserver(observer);
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        cursor.registerDataSetObserver(observer);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        cursor.unregisterDataSetObserver(observer);
    }
}
