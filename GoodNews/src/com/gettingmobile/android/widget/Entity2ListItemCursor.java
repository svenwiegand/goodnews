package com.gettingmobile.android.widget;

import android.database.ContentObserver;
import android.database.DataSetObserver;
import com.gettingmobile.google.reader.db.EntityCursor;

public class Entity2ListItemCursor<T> implements EntityCursor<ListItem> {
    protected final EntityCursor<T> cursor;

    public Entity2ListItemCursor(EntityCursor<T> cursor) {
        this.cursor = cursor;
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
    public long getEntityId() {
        return cursor.getEntityId();
    }

    @Override
    public boolean isGroupHeader() {
        return cursor.isGroupHeader();
    }

    @Override
    public String getGroupTitle() {
        return cursor.getGroupTitle();
    }

    @Override
    public ListItem getEntity() {
        if (cursor.isGroupHeader()) {
            return new ListItem(ListItemCursorAdapter.VIEW_TYPE_HEADER, cursor.getGroupTitle());
        } else {
            return new ListItem(ListItemCursorAdapter.VIEW_TYPE_DEFAULT, cursor.getEntity());
        }
    }

    @Override
    public int getCount() {
        return cursor.getCount();
    }

    @Override
    public int getPosition() {
        return cursor.getPosition();
    }

    @Override
    public boolean isBeforeFirst() {
        return cursor.isBeforeFirst();
    }

    @Override
    public boolean isAfterLast() {
        return cursor.isAfterLast();
    }

    @Override
    public boolean isFirst() {
        return cursor.isFirst();
    }

    @Override
    public boolean isLast() {
        return cursor.isLast();
    }

    @Override
    public boolean moveToFirst() {
        return cursor.moveToFirst();
    }

    @Override
    public boolean moveToLast() {
        return cursor.moveToLast();
    }

    @Override
    public boolean moveToNext() {
        return cursor.moveToNext();
    }

    @Override
    public boolean moveToPrevious() {
        return cursor.moveToPrevious();
    }

    @Override
    public boolean move(int offset) {
        return cursor.move(offset);
    }

    @Override
    public boolean moveToPosition(int position) {
        return cursor.moveToPosition(position);
    }

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
