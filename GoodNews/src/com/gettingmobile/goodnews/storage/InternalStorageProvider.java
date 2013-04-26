package com.gettingmobile.goodnews.storage;

import android.content.Context;

import java.io.File;

public class InternalStorageProvider extends AbstractStorageProvider {
    public InternalStorageProvider(Context context) {
        super(context, Storage.INTERNAL);
    }

    @Override
    public boolean isStorageAvailable() {
        /*
         * is always available
         */
        return true;
    }

    @Override
    public boolean isStorageWritable() {
        /*
         * is always writable
         */
        return true;
    }

    @Override
    public File getDirectory(String category) {
        final File dir = new File(context.getFilesDir(), category);
        dir.mkdirs();
        return dir;
    }

    @Override
    public File getDatabasePath(String databaseName) {
        return context.getDatabasePath(databaseName);
    }
}
