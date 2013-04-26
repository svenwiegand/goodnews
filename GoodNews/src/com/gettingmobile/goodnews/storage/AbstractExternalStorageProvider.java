package com.gettingmobile.goodnews.storage;

import android.content.Context;
import android.os.Environment;

import java.io.File;

public abstract class AbstractExternalStorageProvider extends AbstractStorageProvider {
    protected static final String DATABASES = "databases";

    public AbstractExternalStorageProvider(Context context) {
        super(context, StorageProvider.Storage.EXTERNAL);
    }

    @Override
    public boolean isStorageWritable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    @Override
    public boolean isStorageAvailable() {
        return isStorageWritable() || Environment.MEDIA_MOUNTED_READ_ONLY.equals(Environment.getExternalStorageState());
    }

    @Override
    public File getDatabasePath(String databaseName) {
        return new File(getDirectory(DATABASES), databaseName);
    }
}
