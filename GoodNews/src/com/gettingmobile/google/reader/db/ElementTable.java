package com.gettingmobile.google.reader.db;

import android.database.sqlite.SQLiteDatabase;

import java.util.Map;

public abstract class ElementTable extends Table {
	public static final String KEY = "_id";
	public static final String ID = "id";
	public static final String TITLE = "title";

	public ElementTable(String tableName) {
		super(tableName);
	}
	
	@Override
	protected void defineColumns(Map<String, String> columns) {
		columns.put(KEY, "INTEGER PRIMARY KEY");
		columns.put(ID, "TEXT");
		columns.put(TITLE, "TEXT");
	}

	@Override
	protected void postCommands(SQLiteDatabase db) {
		super.postCommands(db);
		
		db.execSQL("CREATE UNIQUE INDEX " + getTableName() + "Id ON " + getTableName() + " (id)");
        db.execSQL("CREATE INDEX " + getTableName() + "TitleIndex ON " + getTableName() + " (title COLLATE NOCASE ASC)");
	}

    @Override
    public void upgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 20) {
            db.execSQL("CREATE INDEX IF NOT EXISTS " + getTableName() + "TitleIndex ON " + getTableName() + " (title COLLATE NOCASE ASC)");
        }
        super.upgrade(db, oldVersion, newVersion);
    }
}
