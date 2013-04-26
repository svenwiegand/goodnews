package com.gettingmobile.google.reader.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class AbstractDatabaseAdapter<T> implements DatabaseAdapter<T> {
	private final String tableName;
	
	public AbstractDatabaseAdapter(String tableName) {
		this.tableName = tableName;
	}
	
	protected String getTableName() {
		return tableName;
	}

	@Override
	public void delete(SQLiteDatabase db) throws SQLException {
		db.delete(getTableName(), null, null);
	}

	protected abstract void setRowValues(SQLiteDatabase db, ContentValues columns, T entity, Bundle parameters);
    protected abstract void attachRowId(T entity, long id);

    protected ContentValues createRowValues(SQLiteDatabase db, T entity, Bundle parameters) {
        final ContentValues rowValues = new ContentValues();
        setRowValues(db, rowValues, entity, parameters);
        return rowValues;
    }
    
    protected long write(SQLiteDatabase db, T entity, Bundle parameters) throws SQLException {
   		final long rowId = db.insert(tableName, null, createRowValues(db, entity, parameters));
   		if (rowId > Table.INVALID_ID) {
               attachRowId(entity, rowId);
   			writeJoins(db, entity, rowId);
   		}
           return rowId;
   	}

	@Override
	public long write(SQLiteDatabase db, T entity) throws SQLException {
        return write(db, entity, new Bundle());
	}

	@Override
	public void writeList(SQLiteDatabase db, Collection<T> entities) throws SQLException {
		for (T entity : entities) {
			write(db, entity);
		}
	}
	
	protected void writeJoins(SQLiteDatabase db, T entity, long rowId) throws SQLException {
		// do nothing by default
	}
	
	protected abstract T create();

	public T readCurrent(Cursor c) {
		return create();
	}
	
	public void readJoinCurrent(T current, Cursor c) {
		// do nothing by default
	}

	@Override
	public T readFirst(Cursor c) {
        try {
            T entity = null;
            if (c.moveToFirst()) {
                long currentKey = -1;
                do {
                    final long rowKey = getRowKey(c);
                    if (entity != null && currentKey == rowKey) {
                        readJoinCurrent(entity, c);
                    } else if (entity == null) {
                        entity = readCurrent(c);
                        currentKey = rowKey;
                    } else {
                        break;
                    }
                } while (c.moveToNext());
            }
            return entity;
        } finally {
            c.close();
        }
	}
    
    public T readFirst(EntityCursor<T> c) {
        try {
            return c.moveToFirst() ? c.getEntity() : null;
        } finally {
            c.close();
        }
    }

    protected long getRowKey(Cursor c, String columnName) {
        final long rowId = c.getLong(c.getColumnIndex(columnName));
        return rowId > 0 ? rowId : Table.INVALID_ID;
    }

	@Override
	public List<T> readList(Cursor c) {
		try {
			final List<T> entities = new ArrayList<T>();
			if (c.moveToFirst()) {
				long currentKey = -1;
				T current = null;
				do {
					final long rowKey = getRowKey(c);
					if (current != null && currentKey == rowKey) {
						readJoinCurrent(current, c);
					} else {
						current = readCurrent(c);
						currentKey = rowKey;
						entities.add(current);
					}
				} while (c.moveToNext());
			}
			return entities;
		} finally {
			c.close();
		}
	}
    
    public List<T> readList(EntityCursor<T> c) {
        try {
            final List<T> entities = new ArrayList<T>();
            if (c.moveToFirst()) {
                do {
                    if (!c.isGroupHeader()) {
                        entities.add(c.getEntity());
                    }
                } while (c.moveToNext());
            }
            return entities;
        } finally {
            c.close();
        }
    }

}
