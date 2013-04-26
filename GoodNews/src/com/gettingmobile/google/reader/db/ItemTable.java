package com.gettingmobile.google.reader.db;

import android.database.sqlite.SQLiteDatabase;
import com.gettingmobile.google.reader.ElementId;

import java.util.Map;
import java.util.Set;

public class ItemTable extends ElementTable {
	public static final String TABLE_NAME = "item";
    public static final String REF_ID = "refId";
	public static final String FEED_ID = "feedId";
    public static final String FEED_TITLE = "feedTitle";
	public static final String TIMESTAMP = "timestamp";
    public static final String INSERT_TIMESTAMP = "insertTimestamp";
	public static final String READ = "read";
    public static final String SYNCED_READ = "syncedRead";
	public static final String ALTERNATE_HREF = "alternateHref";
	public static final String ALTERNATE_MIME_TYPE = "alternateMimeType";
	public static final String AUTHOR = "author";
	public static final String HAS_SUMMARY = "hasSummary";
	public static final String HAS_CONTENT = "hasContent";
    public static final String TEASER = "teaser";
    public static final String SUMMARY = "summary";
	public static final String CONTENT = "content";
    public static final String IS_EXTERNAL_CONTENT = "isExternalContent";
    public static final String HAS_IMAGES = "hasImages";

	public ItemTable() {
		super(TABLE_NAME);
	}
	
	@Override
	protected void defineColumns(Map<String, String> columns) {
		super.defineColumns(columns);
        columns.put(REF_ID, "INTEGER");
		columns.put(FEED_ID, "TEXT");
        columns.put(FEED_TITLE, "TEXT");
		columns.put(TIMESTAMP, "INTEGER");
        columns.put(INSERT_TIMESTAMP, "INTEGER");
		columns.put(READ, "INTEGER");
        columns.put(SYNCED_READ, "INTEGER");
		columns.put(ALTERNATE_HREF, "TEXT");
		columns.put(ALTERNATE_MIME_TYPE, "TEXT");
		columns.put(AUTHOR, "TEXT");
		columns.put(HAS_SUMMARY, "INTEGER");
		columns.put(HAS_CONTENT, "INTEGER");
        columns.put(TEASER, "TEXT");
        columns.put(SUMMARY, "TEXT");
		columns.put(CONTENT, "TEXT");
        columns.put(IS_EXTERNAL_CONTENT, "INTEGER");
        columns.put(HAS_IMAGES, "INTEGER");
	}

	@Override
	protected void postCommands(SQLiteDatabase db) {
		super.postCommands(db);

        db.execSQL("CREATE UNIQUE INDEX refIdIndex ON " + TABLE_NAME + " (refId)");
		db.execSQL("CREATE INDEX feedIdIndex ON " + TABLE_NAME + " (feedId)");
		db.execSQL("CREATE INDEX readIndex ON " + TABLE_NAME + " (read)");
        db.execSQL("CREATE INDEX readFlagIndex ON " + TABLE_NAME + " (read)");
        db.execSQL("CREATE INDEX itemFeedTitleIndex ON " + TABLE_NAME + " (feedTitle COLLATE NOCASE ASC)");
        db.execSQL("CREATE INDEX itemSignature ON " + TABLE_NAME + " (feedId, timestamp, title)");
	}

    @Override
    public void upgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 17) {
            create(db);
            return;
        }
        if (oldVersion < 19) {
            db.execSQL("CREATE INDEX readFlagIndex ON " + TABLE_NAME + " (read)");
        }
        if (oldVersion < 21) {
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN refId INTEGER");
            final Set<ElementId> ids = new ItemDatabaseAdapter().readAllIds(db);
            for (ElementId id : ids) {
                db.execSQL("UPDATE " + TABLE_NAME + " SET refId=" + Long.toString(id.getItemReferenceId()) +
                        " WHERE id='" + id.getId()  + "'");
            }
            db.execSQL("CREATE UNIQUE INDEX refIdIndex ON " + TABLE_NAME + " (refId)");
        }
        if (oldVersion < 26) {
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN feedTitle TEXT");
        }
        if (oldVersion < 28) {
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + TEASER + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + SUMMARY + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + CONTENT + " TEXT");
        }
        if (oldVersion < 29) {
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + SYNCED_READ + " INTEGER");
            db.execSQL("UPDATE item SET syncedRead=1 WHERE read=1");
            db.execSQL("UPDATE item SET syncedRead=0 WHERE read=0");
        }
        if (oldVersion < 30) {
            /*
             * add feed title to all items
             */
            new ItemDatabaseAdapter().updateFeedTitles(db);
            db.execSQL("CREATE INDEX itemFeedTitleIndex ON " + TABLE_NAME + " (feedTitle COLLATE NOCASE ASC)");
        }
        if (oldVersion < 32) {
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + IS_EXTERNAL_CONTENT + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + HAS_IMAGES + " INTEGER");
        }
        if (oldVersion < 33) {
            db.execSQL("ALTER TABLE " +TABLE_NAME + " ADD COLUMN " + INSERT_TIMESTAMP + " INTEGER");
            db.execSQL("UPDATE " + TABLE_NAME + " SET " + INSERT_TIMESTAMP + "=item." + TIMESTAMP);
        }
        if (oldVersion < 35) {
            db.execSQL("CREATE INDEX itemSignature ON " + TABLE_NAME + " (feedId, timestamp, title)");
        }
        super.upgrade(db, oldVersion, newVersion);
    }
}
