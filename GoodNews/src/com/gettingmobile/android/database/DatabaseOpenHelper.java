package com.gettingmobile.android.database;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteException;
import android.util.Log;
import com.gettingmobile.goodnews.storage.StorageProvider;
import com.gettingmobile.io.IOUtils;

import java.io.File;
import java.io.IOException;

/**
 * A helper class to manage database creation and version management.
 * <p/>
 * <p>You create a subclass by implementing {@link #onCreate} an {@link #onUpgrade}, and this class takes care of
 * opening the database
 * if it exists, creating it if it does not, and upgrading it as necessary.
 * Transactions are used to make sure the database is always in a sensible state.
 * <p/>
 * <p class="note"><strong>Note:</strong> this class assumes
 * monotonically increasing version numbers for upgrades.  Also, there
 * is no concept of a database downgrade; installing a new version of
 * your app which uses a lower version number than a
 * previously-installed version will result in undefined behavior.</p>
 */
public abstract class DatabaseOpenHelper {
    private static final String TAG = "goodnews.DatabaseOpenHelper";

    private final String mName;
    private final CursorFactory mFactory;
    private final int mNewVersion;

    private SQLiteDatabase mDatabase = null;
    private boolean mIsInitializing = false;

    /**
     * Create a helper object to create, open, and/or manage a database.
     * This method always returns very quickly.  The database is not actually
     * created or opened until {@link #openOrCreateDatabase} is called.
     *
     * @param name    of the database file, or null for an in-memory database
     * @param factory to use for creating cursor objects, or null for the default
     * @param version number of the database (starting at 1); if the database is older,
     *                {@link #onUpgrade} will be used to upgrade the database
     */
    public DatabaseOpenHelper(String name, CursorFactory factory, int version) {
        if (version < 1) throw new IllegalArgumentException("Version must be >= 1, was " + version);

        mName = name;
        mFactory = factory;
        mNewVersion = version;
    }

    /**
     * Moves the database from one storage to another one. If the source and the destination storage are the same
     * nothing will be done. Before invoked, the database needs to be closed and afterwards the database needs to be
     * reopened.
     * @param source storage provider referencing the storage to move the database from.
     * @param destination storage provider referencing the storage to move the database to.
     * @throws IOException if moving the database fails.
     */
    public synchronized void moveDatabase(StorageProvider source, StorageProvider destination) throws IOException {
        final File src = source.getDatabasePath(mName);
        final File dest = destination.getDatabasePath(mName);
        final File destDir = dest.getParentFile();
        if (!src.getAbsolutePath().equals(dest.getAbsolutePath())) {
            if (!source.isStorageAvailable())
                throw new IOException("Source storage not readable");
            if (!destination.isStorageWritable())
                throw new IOException("Destination storage not writable");
            if (!src.exists())
                throw new IOException("Source file " + src.getAbsolutePath() + " does not exist!");
            if (!destDir.exists() && !destDir.mkdirs())
                throw new IOException("Failed to create destination directory " + destDir.getAbsolutePath());

            Log.i(TAG, "Moving database from " + src.getAbsolutePath() + " to " + dest.getAbsolutePath());
            IOUtils.move(src, dest);
        }
    }

    /**
     * Same as [@link #moveDatabase} but allows the database to be open and opens the new database afterwards.
     */
    public synchronized void moveOpenDatabase(StorageProvider source, StorageProvider destination) throws IOException {
        /*
         * close the current database
         */
        close();

        /*
         * move the database
         */
        try {
            moveDatabase(source, destination);
        } catch (IOException ex) {
            Log.e(TAG, "Failed to move database!", ex);

            /*
             * we failed, so we should open the old database
             */
            openOrCreateDatabase(source);
            throw ex;
        }

        /*
         * open the new database
         */
        openOrCreateDatabase(destination);
    }

    public synchronized void deleteDatabase(StorageProvider storageProvider) throws IOException {
        IOUtils.delete(storageProvider.getDatabasePath(mName));
    }

    public synchronized void recreateOpenDatabase(StorageProvider storageProvider) throws IOException {
        /*
         * close current database
         */
        close();

        /*
         * delete the database
         */
        deleteDatabase(storageProvider);

        /*
         * open the existing one or create a new database
         */
        openOrCreateDatabase(storageProvider);
    }

    private File getDatabasePath(StorageProvider storageProvider) {
        final File dbPath = storageProvider.getDatabasePath(mName);
        Log.i(TAG, "Open or create database at " + dbPath.getAbsolutePath());

        final File dbDir = dbPath.getParentFile();
        if (!dbDir.exists() && !dbDir.mkdirs()) {
            final String msg = "Failed to create database directory at " + dbDir.getAbsolutePath();
            Log.e(TAG, msg);
            throw new SQLiteException(msg);
        }
        return dbPath;
    }

    /**
     * Create and/or open a database that will be used for reading and writing.
     * The first time this is called, the database will be opened and
     * {@link #onCreate} and if applicable {@link #onUpgrade} will be called.
     * <p/>
     * <p>Once opened successfully, the database is cached, so you can
     * call {@link #getDatabase()} to get access to the database.
     * (Make sure to call {@link #close} when you no longer need the database.)
     * Errors such as bad permissions or a full disk may cause this method
     * to fail, but future attempts may succeed if the problem is fixed.</p>
     * <p/>
     * <p class="caution">Database upgrade may take a long time, you
     * should not call this method from the application main thread, including
     * from {@link android.content.ContentProvider#onCreate ContentProvider.onCreate()}.
     *
     * @param storageProvider the storage provider to be used to reference the database file.
     * @throws android.database.sqlite.SQLiteException if the database cannot be opened for writing
     */
    public synchronized void openOrCreateDatabase(StorageProvider storageProvider) {
        if (mDatabase != null && mDatabase.isOpen() && !mDatabase.isReadOnly()) {
            return;  // The database is already open for business
        }

        if (mIsInitializing) {
            throw new IllegalStateException("openOrCreateDatabase called recursively");
        }

        // If we have a read-only database open, someone could be using it
        // (though they shouldn't), which would cause a lock to be held on
        // the file, and our attempts to open the database read-write would
        // fail waiting for the file lock.  To prevent that, we acquire the
        // lock on the read-only database, which shuts out other users.

        boolean success = false;
        SQLiteDatabase db = null;
        //if (mDatabase != null) mDatabase.lock();
        try {
            mIsInitializing = true;
            final File dbPath = getDatabasePath(storageProvider);
            if (mName == null) {
                db = SQLiteDatabase.create(null);
            } else {
                db = SQLiteDatabase.openOrCreateDatabase(dbPath, mFactory);
            }

            int version = db.getVersion();
            if (version != mNewVersion) {
                db.beginTransaction();
                try {
                    if (version == 0) {
                        onCreate(db);
                    } else {
                        if (version > mNewVersion) {
                            Log.e(TAG, "Can't downgrade read-only database from version " +
                                    version + " to " + mNewVersion + ": " + db.getPath());
                        }
                        onUpgrade(db, version, mNewVersion);
                    }
                    db.setVersion(mNewVersion);
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
            }

            success = true;
        } finally {
            mIsInitializing = false;
            if (success) {
                if (mDatabase != null) {
                    try {
                        mDatabase.close();
                    } catch (Exception e) {
                        Log.w(TAG, "Failed to close database", e);
                    }
                    //mDatabase.unlock();
                }
                mDatabase = db;
            } else {
                //if (mDatabase != null) mDatabase.unlock();
                if (db != null) db.close();
            }
        }
    }

    public synchronized SQLiteDatabase getDatabase() {
        return mDatabase;
    }

    public synchronized SQLiteDatabase getReadOnlyDatabase() {
        return mDatabase;
    }

    /**
     * Close any open database object.
     */
    public synchronized void close() {
        if (mIsInitializing) throw new IllegalStateException("Closed during initialization");

        if (mDatabase != null && mDatabase.isOpen()) {
            mDatabase.close();
            mDatabase = null;
        }
    }

    /**
     * Called when the database is created for the first time. This is where the
     * creation of tables and the initial population of the tables should happen.
     *
     * @param db The database.
     */
    public abstract void onCreate(SQLiteDatabase db);

    /**
     * Called when the database needs to be upgraded. The implementation
     * should use this method to drop tables, add tables, or do anything else it
     * needs to upgrade to the new schema version.
     * <p/>
     * <p>The SQLite ALTER TABLE documentation can be found
     * <a href="http://sqlite.org/lang_altertable.html">here</a>. If you add new columns
     * you can use ALTER TABLE to insert them into a live table. If you rename or remove columns
     * you can use ALTER TABLE to rename the old table, then create the new table and then
     * populate the new table with the contents of the old table.
     *
     * @param db         The database.
     * @param oldVersion The old database version.
     * @param newVersion The new database version.
     */
    public abstract void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion);
}