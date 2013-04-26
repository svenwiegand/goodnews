package com.gettingmobile.google.reader.db;

import android.database.ContentObserver;
import android.database.CursorIndexOutOfBoundsException;
import android.database.DataSetObserver;

public class EmptyEntityCursor<T> implements EntityCursor<T> {
    private boolean closed = false;
    
    @Override
    public void close() {
        closed = true;
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public boolean isGroupHeader() {
        return fail();
    }

    @Override
    public String getGroupTitle() {
        fail();
        return null;
    }

    @Override
    public long getEntityId() {
        fail();
        return 0;
    }

    @Override
    public T getEntity() {
        fail();
        return null;
    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public int getPosition() {
        return -1;
    }

    @Override
    public boolean isBeforeFirst() {
        return true;
    }

    @Override
    public boolean isAfterLast() {
        return false;
    }

    @Override
    public boolean isFirst() {
        return false;
    }

    @Override
    public boolean isLast() {
        return false;
    }

    @Override
    public boolean moveToFirst() {
        return fail();
    }

    @Override
    public boolean moveToLast() {
        return fail();
    }

    @Override
    public boolean moveToNext() {
        return fail();
    }

    @Override
    public boolean moveToPrevious() {
        return fail();
    }

    @Override
    public boolean move(int offset) {
        return offset == 0 || fail();
    }

    @Override
    public boolean moveToPosition(int position) {
        return position == -1 || fail();
    }

    @Override
    public void registerContentObserver(ContentObserver observer) {

    }

    @Override
    public void unregisterContentObserver(ContentObserver observer) {

    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {

    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {

    }
    
    private boolean fail() throws CursorIndexOutOfBoundsException {
        throw new CursorIndexOutOfBoundsException(-1, 0);        
    }
}
