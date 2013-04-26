package com.gettingmobile.google.reader.sync;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.gettingmobile.goodnews.storage.StorageProvider;
import com.gettingmobile.google.reader.ElementId;
import com.gettingmobile.google.reader.Item;
import com.gettingmobile.google.reader.db.ItemDatabaseAdapter;
import com.gettingmobile.io.Base64;
import com.gettingmobile.io.IOUtils;
import com.gettingmobile.io.SimpleFileFilter;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class ItemFileCleanup {
    private static final String LOG_TAG = "goodnews.ItemFileCleanup";
    private final FileFilter dirFilter = new SimpleFileFilter(false, true, true);
    private final ItemDatabaseAdapter dbAdapter = new ItemDatabaseAdapter();
    private final SQLiteDatabase db;
    private final StorageProvider storageProvider;

    public ItemFileCleanup(SQLiteDatabase db, StorageProvider storageProvider) {
        this.db = db;
        this.storageProvider = storageProvider;
    }

    public void cleanup() {
        Log.d(LOG_TAG, "starting cleanup");
        if (storageProvider == null) {
            Log.d(LOG_TAG, "nothing to cleanup (no storage provider set)");
            return;
        }

        /*
         * get the directory storing the content and check whether it is existing
         */

        /*
         * get the names of the directory we need to keep
         */
        final Set<ElementId> ids = dbAdapter.readAllIds(db);
        final Set<String> requiredDirNames = new HashSet<String>(ids.size());
        for (ElementId id : ids) {
            requiredDirNames.add(Item.getDirectoryName(id));
        }

        /*
         * iterate through all directories and check whether they are required or not.
         *
         * We may encounter an OutOfMemoryException if we use the old (flat) directory naming and have a lot of files.
         * That's why we loop based on the start character
         */
        final File dir = storageProvider.getDirectory(Item.STORAGE_CATEGORY);
        if (dir.exists()) {
            for (char c : Base64.ALPHABET) {
                if (c == '/') {
                    c = '-';
                }
                final File[] subdirs = dir.listFiles(new DirFilter(c));
                if (subdirs != null) {
                    for (File d : subdirs) {
                        deleteDirectoryIfApplicable(requiredDirNames, storageProvider, d, true);
                    }
                }
            }
        }

        Log.d(LOG_TAG, "cleanup done");
    }

    private void deleteDirectoryIfApplicable(Set<String> requiredDirNames, StorageProvider storageProvider, File dir, boolean isAtRootLevel) {
        Log.d(LOG_TAG, "Looking at directory " + dir);

        if (dir.getName().length() == 1) {
            /*
             * handle new tree like directory structure and recurse into sub directories
             */
            final File[] subdirs = dir.listFiles(dirFilter);
            if (subdirs != null) {
                for (File d : subdirs) {
                    deleteDirectoryIfApplicable(requiredDirNames, storageProvider, d, false);
                }
            }
        } else if (isAtRootLevel) {
            /*
             * handle old styled base64 encoded directory name
             */
            try {
                final String newDirName = Item.convertFromOldDirectoryName(dir.getName());
                Log.d(LOG_TAG, "Converted directory name is " + newDirName);
                if (!requiredDirNames.contains(newDirName)) {
                    deleteDirectory(dir);
                } else {
                    final File newDir = Item.getDirectory(storageProvider, newDirName);
                    //noinspection ResultOfMethodCallIgnored
                    newDir.getParentFile().mkdirs();
                    //noinspection ResultOfMethodCallIgnored
                    dir.renameTo(newDir);
                }
            } catch (IOException ex) {
                Log.w(LOG_TAG, "Failed to convert old directory name to new: " + dir.getName());
            }
        } else if (!requiredDirNames.contains(dir.getName())) {
            /*
             * delete leafe-directories
             */
            deleteDirectory(dir);
        }
    }

    private void deleteDirectory(File dir) {
        try {
            Log.d(LOG_TAG, "Deleting directory " + dir);
            IOUtils.deleteRecursive(dir);
        } catch (IOException ex) {
            Log.w(LOG_TAG, "Failed to delete content directory " + dir);
        }
    }

    /*
     * inner classes
     */

    static final class DirFilter extends SimpleFileFilter {
        private final char startChar;

        DirFilter(char startChar) {
            super(false, true, true);
            this.startChar = startChar;
        }

        @Override
        public boolean accept(File f) {
            return super.accept(f) && f.getName().charAt(0) == startChar;
        }
    }
}
