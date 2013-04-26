package com.gettingmobile.goodnews.storage;

import java.io.File;

public final class TempFileFactory {
    public static final String STORAGE_CATEGORY = "temp";

    public static File create(StorageProvider storageProvider, String fileName) {
        return storageProvider.getFile(STORAGE_CATEGORY, fileName);
    }
}
