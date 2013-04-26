package com.gettingmobile.google.reader.db;

import android.database.Cursor;
import com.gettingmobile.google.reader.Element;
import com.gettingmobile.google.reader.ElementId;

public abstract class ElementCursorAdapter<T extends Element> implements EntityCursorAdapter<T> {
    int keyCol;
    int idCol;
    int titleCol;

    @Override
    public void init(Cursor c) {
        keyCol = c.getColumnIndex(ElementTable.KEY);
        idCol = c.getColumnIndex(ElementTable.ID);
        titleCol = c.getColumnIndex(ElementTable.TITLE);
    }

    @Override
    public long readEntityId(Cursor c) {
        return c.getLong(keyCol);
    }

    @Override
    public T readEntity(Cursor c) {
        final T entity = createEntity();

        entity.setKey(readEntityId(c));
        entity.setId(new ElementId(c.getString(idCol)));
        entity.setTitle(c.getString(titleCol));
        
        return entity;
    }

    @Override
    public void readEntityJoin(T entity, Cursor c) {
        // nothing to be done by default
    }

    protected abstract T createEntity();
}
