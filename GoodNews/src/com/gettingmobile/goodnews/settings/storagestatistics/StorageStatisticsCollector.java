package com.gettingmobile.goodnews.settings.storagestatistics;

import android.database.sqlite.SQLiteDatabase;
import com.gettingmobile.goodnews.storage.StorageProvider;
import com.gettingmobile.google.reader.Item;
import com.gettingmobile.google.reader.db.DatabaseHelper;
import com.gettingmobile.google.reader.db.ItemDatabaseAdapter;

import java.io.File;

final class StorageStatisticsCollector {
    private final StorageStatistics storageStatistics;

    public StorageStatisticsCollector(SQLiteDatabase db, StorageProvider databaseStorageProvider, 
                                      StorageProvider contentStorageProvider) {
        final int activeArticleCount = getActiveArticleCount(db);
        final int totalArticleCount = getTotalArticleCount(db);
        
        final ContentTreeTraverser contentTreeTraverser = 
                new ContentTreeTraverser().traverse(contentStorageProvider.getDirectory(Item.STORAGE_CATEGORY));
        storageStatistics = new StorageStatistics(
                activeArticleCount, totalArticleCount - activeArticleCount,
                contentTreeTraverser.articleWithContentFileCount,
                contentTreeTraverser.articleWithImageFileCount,
                contentTreeTraverser.contentFileCount,
                contentTreeTraverser.imageFileCount,
                getDatabaseFileSize(databaseStorageProvider),
                contentTreeTraverser.contentFilesSize,
                contentTreeTraverser.imageFilesSize);
    }

    public StorageStatistics getStorageStatistics() {
        return storageStatistics;
    }
    
    private int getActiveArticleCount(SQLiteDatabase db) {
        return new ItemDatabaseAdapter().readActiveCount(db);
    }
    
    private int getTotalArticleCount(SQLiteDatabase db) {
        return new ItemDatabaseAdapter().readTotalCount(db);
    }
    
    private long getDatabaseFileSize(StorageProvider databaseStorageProvider) {
        return databaseStorageProvider.getDatabasePath(DatabaseHelper.DATABASE_NAME).length();
    }

    private static final class ContentTreeTraverser {
        public int articleWithContentFileCount = 0;
        public int articleWithImageFileCount = 0;
        public int contentFileCount = 0;
        public int imageFileCount = 0;
        public long contentFilesSize = 0;
        public long imageFilesSize = 0;

        private boolean countedArticleForContent = false;
        private boolean countedArticleForImages = false;

        public ContentTreeTraverser traverse(File file) {
            if (file.isDirectory()) {
                resetArticleStats();
                for (File f : file.listFiles()) {
                    traverse(f);
                }
            } else {
                handleFile(file);    
            }
            return this;
        }

        private void resetArticleStats() {
            countedArticleForContent = false;
            countedArticleForImages = false;
        }
        
        private void handleFile(File file) {
            if (isContentFile(file) || isSummaryFile(file)) {
                countContentFile(file);
            } else {
                countImageFile(file);
            }
        }
        
        private void countContentFile(File file) {
            if (!countedArticleForContent) {
                ++articleWithContentFileCount;
                countedArticleForContent = true;
            }
            ++contentFileCount;
            contentFilesSize+= file.length();
        }
        
        private void countImageFile(File file) {
            if (!countedArticleForImages) {
                ++articleWithImageFileCount;
                countedArticleForImages = true;
            }
            ++imageFileCount;
            imageFilesSize+= file.length();
        }
        
        private boolean isContentFile(File file) {
            return Item.FILE_NAME_CONTENT.equals(file.getName());
        }
        
        private boolean isSummaryFile(File file) {
            return Item.FILE_NAME_SUMMARY.equals(file.getName());
        }
    }
}
