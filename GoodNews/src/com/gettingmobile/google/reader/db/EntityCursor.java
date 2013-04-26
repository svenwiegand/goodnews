package com.gettingmobile.google.reader.db;

import android.database.ContentObserver;
import android.database.DataSetObserver;
import com.gettingmobile.android.widget.ListItem;

import java.io.Closeable;

public interface EntityCursor<T> extends Closeable {
    void close();
    public boolean isClosed();

    boolean isGroupHeader();
    String getGroupTitle();
    long getEntityId();
    T getEntity();

    /*
     * position query
     */

    public int getCount();
    public int getPosition();
    public boolean isBeforeFirst();
    public boolean isAfterLast();
    public boolean isFirst();
    public boolean isLast();

    /*
     * moving
     */

    public boolean moveToFirst();
    public boolean moveToLast();
    public boolean moveToNext();
    public boolean moveToPrevious();
    public boolean move(int offset);
    public boolean moveToPosition(int position);

    /*
     * observers
     */

    public void registerContentObserver(ContentObserver observer);
    public void unregisterContentObserver(ContentObserver observer);
    public void registerDataSetObserver(DataSetObserver observer);
    public void unregisterDataSetObserver(DataSetObserver observer);
}
