package com.gettingmobile.google.reader.db;

import android.database.ContentObserver;
import android.database.CursorIndexOutOfBoundsException;
import android.database.DataSetObserver;

import java.util.List;

public abstract class EntityListCursor<T> implements EntityCursor<T> {
    private final List<T> entities;
    private int pos = -1;
    private boolean closed = false;

    public EntityListCursor(List<T> entities) {
        this.entities = entities;
    }
    
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
        checkPosition();
        return false;
    }

    @Override
    public String getGroupTitle() {
        checkPosition();
        return null;
    }

    @Override
    public long getEntityId() {
        checkPosition();
        return getEntityId(entities.get(pos));
    }
    
    protected abstract long getEntityId(T entity);

    @Override
    public T getEntity() {
        checkPosition();
        return entities.get(pos);
    }

    /*
     * position query
     */

    public int getCount() {
        return entities.size();
    }

    public int getPosition() {
        return pos;
    }

    public boolean isBeforeFirst() {
        return pos < 0;
    }

    public boolean isAfterLast() {
        return pos >= entities.size();
    }

    public boolean isFirst() {
        return pos == 0;
    }

    public boolean isLast() {
        return pos == entities.size() - 1;
    }

    /*
     * moving
     */

    public boolean moveToFirst() {
        return moveToPosition(0);
    }

    public boolean moveToLast() {
        return moveToPosition(getCount() - 1);
    }

    public boolean moveToNext() {
        return moveToPosition(pos + 1);
    }

    public boolean moveToPrevious() {
        return moveToPosition(pos - 1);
    }

    public boolean move(int offset) {
        return moveToPosition(pos + offset);
    }

    public boolean moveToPosition(int position) {
        // Make sure position isn't past the end of the cursor
        final int count = getCount();
        if (position >= count) {
            pos = count;
            return false;
        }

        // Make sure position isn't before the beginning of the cursor
        if (position < 0) {
            pos = -1;
            return false;
        }

        // Check for no-op moves, and skip the rest of the work for them
        if (position == pos) {
            return true;
        }

        pos = position;
        return true;
    }

    @Override
    public void registerContentObserver(ContentObserver observer) {
        // nothing can change
    }

    @Override
    public void unregisterContentObserver(ContentObserver observer) {
        // nothing can change
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        // nothing can change
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        // nothing can change
    }

    /*
     * helpers
     */

    protected void checkPosition() {
        if (-1 == pos || getCount() == pos) {
            throw new CursorIndexOutOfBoundsException(pos, getCount());
        }
    }    
}
