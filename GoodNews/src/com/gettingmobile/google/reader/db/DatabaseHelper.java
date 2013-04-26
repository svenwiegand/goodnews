package com.gettingmobile.google.reader.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.gettingmobile.android.database.DatabaseOpenHelper;
import com.gettingmobile.goodnews.settings.Settings;
import com.gettingmobile.goodnews.storage.StorageProvider;
import com.gettingmobile.goodnews.storage.StorageProviderFactory;
import com.gettingmobile.io.IOUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class DatabaseHelper extends DatabaseOpenHelper {
    private static final String LOG_TAG = "goodnews.DatabaseHelper";
	public static final String DATABASE_NAME = "goodnews";
    private static final String SECONDARY_DATABASE_NAME = "goodnewsSecondary";
	public static final int DATABASE_VERSION = 35;
	private final List<Table> tables = new ArrayList<Table>();
	
	protected DatabaseHelper(String dbName) {
		super(dbName, null, DATABASE_VERSION);
		tables.add(new TagTable());
		tables.add(new FeedTable());
		tables.add(new FeedTagTable());
		tables.add(new ItemTable());
		tables.add(new ItemTagTable());
		tables.add(new ItemTagChangeEventTable());
        tables.add(new ItemRequestSpecificationTable());
        tables.add(new ItemBlacklistTable());
	}

    public static DatabaseHelper create(Context context, Settings settings) {
        final String dbName;

        if (settings != null && settings.areDatabasesSwapped()) {
            Log.i(LOG_TAG, "Databases are swapped. Trying to rearrange.");
            final File dbFile = context.getDatabasePath(DATABASE_NAME);
            final File secondaryDbFile = context.getDatabasePath(SECONDARY_DATABASE_NAME);
            if ((!dbFile.exists() || dbFile.delete()) && (!secondaryDbFile.exists() || secondaryDbFile.renameTo(dbFile))) {
                dbName = DATABASE_NAME;
                settings.setDatabasesSwapped(false);
            } else {
                dbName = SECONDARY_DATABASE_NAME;
            }
        } else {
            dbName = DATABASE_NAME;
            final File secondaryDbFile = context.getDatabasePath(SECONDARY_DATABASE_NAME);
            IOUtils.deleteIgnore(secondaryDbFile);
        }
        Log.i(LOG_TAG, "Productive database is " + dbName);
        
        return create(dbName, settings != null ?
                settings.getDatabaseStorageProvider() : StorageProviderFactory.createInternalStorageProvider(context));

    }
    
    protected static DatabaseHelper create(String dbName, StorageProvider storageProvider) {
        final DatabaseHelper dbHelper = new DatabaseHelper(dbName);
        dbHelper.openOrCreateDatabase(storageProvider);
        return dbHelper;        
    }

	public void clean() {
		final SQLiteDatabase db = getDatabase();
		db.beginTransaction();
		try {			
			/*
			 * delete all current data
			 */
			for (Table table : tables) {
				table.clean(db);
			}
			
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}		
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.d(LOG_TAG, "creating database ...");
        db.beginTransaction();
        try {
            for (Table table : tables) {
                table.create(db);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
		Log.d(LOG_TAG, "database has been created");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.d(LOG_TAG, "updating database ...");
        db.beginTransaction();
        try {
            for (Table table : tables) {
                table.upgrade(db, oldVersion, newVersion);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        analyze(db);
		Log.d(LOG_TAG, "database has been updated");
	}

    public void tryVacuum() {
        Log.d(LOG_TAG, "beginning vacuum ...");
        try {
            getDatabase().execSQL("VACUUM");
        } catch (Exception ex) {
            Log.w(LOG_TAG, "vacuum failed", ex);
        } finally {
            Log.d(LOG_TAG, "vacuum finished");
        }
    }

    public void analyze() {
        analyze(getDatabase());
    }

    private void analyze(SQLiteDatabase db) {
        db.execSQL("ANALYZE");
    }
}
