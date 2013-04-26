package com.gettingmobile.goodnews.storage;

import android.content.Context;

import java.io.File;

public abstract class AbstractStorageProvider implements StorageProvider {
    protected final Context context;
    private final Storage type;

    public AbstractStorageProvider(Context context, Storage type) {
        this.context = context;
        this.type = type;
    }

    @Override
    public Storage getType() {
        return type;
    }

    @Override
    public File getFile(String category, String name) {
        return new File(getDirectory(category), name);
    }
}
