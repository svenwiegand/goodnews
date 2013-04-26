package com.gettingmobile.google.reader.db;

import android.database.sqlite.SQLiteDatabase;
import com.gettingmobile.google.reader.ItemState;
import com.gettingmobile.google.reader.Tag;

import java.util.Map;


public class TagTable extends SortedElementTable {
	public static final String TABLE_NAME = "tag";
	public static final String IS_USER_LABEL = "isUserLabel";
	public static final String IS_FEED_FOLDER = "isFeedFolder";
    public static final String SORT_ORDER = "sortOrder";

	public TagTable() {
		super(TABLE_NAME);
	}

	@Override
	protected void defineColumns(Map<String, String> columns) {
		super.defineColumns(columns);
		columns.put(IS_USER_LABEL, "INTEGER");
		columns.put(IS_FEED_FOLDER, "INTEGER");
        columns.put(SORT_ORDER, "INTEGER");
	}

	@Override
	protected void postCommands(SQLiteDatabase db) {
		super.postCommands(db);
		
		db.execSQL("CREATE INDEX isUserLabelIndex ON " + TABLE_NAME + " (isUserLabel)");
		db.execSQL("CREATE INDEX isFeedFolderIndex ON " + TABLE_NAME + " (isFeedFolder)");
	}

	@Override
	public void init(SQLiteDatabase db) {
		super.init(db);
		
		final TagDatabaseAdapter adapter = new TagDatabaseAdapter();
		adapter.write(db, new Tag(ItemState.STARRED.getId()));
	}

	@Override
	public void upgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion <= 4) {
			// we've renamed the table in version 5
			db.execSQL("DROP TABLE IF EXISTS label");
            create(db);
		}
        if (oldVersion <= 27) {
            final String deprecatedTags = "('user/-/state/com.google/like', 'user/-/state/com.google/broadcast')";
            db.execSQL("DELETE FROM itemTag WHERE tagKey IN (SELECT _id FROM tag WHERE id IN " + deprecatedTags + ")");
            db.execSQL("DELETE FROM tag WHERE id IN " + deprecatedTags);
        }
		
		super.upgrade(db, oldVersion, newVersion);
	}
	
	
}
