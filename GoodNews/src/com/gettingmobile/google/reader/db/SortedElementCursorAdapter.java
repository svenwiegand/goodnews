package com.gettingmobile.google.reader.db;

import android.database.Cursor;
import com.gettingmobile.google.reader.SortedElement;

public abstract class SortedElementCursorAdapter<T extends SortedElement> extends ElementCursorAdapter<T> {
    protected static final String COL_NAME_UNREAD_COUNT = "unreadCount";
    protected int sortIdCol;
    protected int unreadCountCol;

    public void init(Cursor c) {
        super.init(c);
        sortIdCol = c.getColumnIndex(SortedElementTable.SORT_ID);
        unreadCountCol = c.getColumnIndex(COL_NAME_UNREAD_COUNT);
    }

    @Override
    public T readEntity(Cursor c) {
        final T entity = super.readEntity(c);

        entity.setSortId(c.getString(sortIdCol));
        entity.setUnreadCount(unreadCountCol > -1 ? c.getInt(unreadCountCol) : 0);

        return entity;
    }
}
