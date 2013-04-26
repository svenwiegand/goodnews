package com.gettingmobile.goodnews.storage;

import java.io.File;

public interface StorageProvider {
    enum Storage {
        INTERNAL,
        EXTERNAL
    }

    Storage getType();
    boolean isStorageAvailable();
    boolean isStorageWritable();
    File getDirectory(String category);
    File getFile(String category, String name);
    File getDatabasePath(String databaseName);
}
