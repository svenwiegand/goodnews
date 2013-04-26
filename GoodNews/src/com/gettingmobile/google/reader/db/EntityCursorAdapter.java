package com.gettingmobile.google.reader.db;

import android.database.Cursor;

public interface EntityCursorAdapter<T> {
    void init(Cursor c);
    long readEntityId(Cursor c);
    T readEntity(Cursor c);
    void readEntityJoin(T entity, Cursor c);
}
