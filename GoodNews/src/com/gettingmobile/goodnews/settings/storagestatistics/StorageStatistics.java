package com.gettingmobile.goodnews.settings.storagestatistics;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;

final class StorageStatistics implements Serializable {
    private static final NumberFormat COUNT_FORMAT = new DecimalFormat("#,###");
    private static final NumberFormat SIZE_FORMAT = new DecimalFormat("#,##0.00");

    public final int activeArticleCount;
    public final int inactiveArticleCount;
    
    public final int articleWithContentFileCount;
    public final int articleWithImageFileCount;

    public final int contentFileCount;
    public final int imageFileCount;

    public final long databaseSize;
    public final long contentFilesSize;
    public final long imageFilesSize;

    StorageStatistics(
            int activeArticleCount, int inactiveArticleCount,
            int articleWithContentFileCount, int articleWithImageFileCount,
            int contentFileCount, int imageFileCount,
            long databaseSize, long contentFilesSize, long imageFilesSize) {
        this.activeArticleCount = activeArticleCount;
        this.inactiveArticleCount = inactiveArticleCount;
        this.articleWithContentFileCount = articleWithContentFileCount;
        this.articleWithImageFileCount = articleWithImageFileCount;
        this.contentFileCount = contentFileCount;
        this.imageFileCount = imageFileCount;
        this.databaseSize = databaseSize;
        this.contentFilesSize = contentFilesSize;
        this.imageFilesSize = imageFilesSize;
    }
    
    private static String formatCount(int count) {
        return COUNT_FORMAT.format(count);
    }

    public String formatActiveArticleCount() {
        return formatCount(activeArticleCount);
    }
    
    public String formatInactiveArticleCount() {
        return formatCount(inactiveArticleCount);
    }
    
    public String formatTotalArticleCount() {
        return formatCount(activeArticleCount + inactiveArticleCount);
    }
    
    public String formatArticleWithContentFileCount() {
        return formatCount(articleWithContentFileCount);
    }
    
    public String formatArticleWithImageFileCount() {
        return formatCount(articleWithImageFileCount);
    }

    public String formatContentFileCount() {
        return formatCount(contentFileCount);
    }
    
    public String formatImageFileCount() {
        return formatCount(imageFileCount);
    }
    
    public String formatTotalFileCount() {
        return formatCount(contentFileCount + imageFileCount);
    }

    private static String formatSize(long size) {
        return SIZE_FORMAT.format((double) size / (1024.0 * 1024.0)) + " MB";
    }

    public String formatDatabaseSize() {
        return formatSize(databaseSize);
    }
    
    public String formatContentFilesSize() {
        return formatSize(contentFilesSize);
    }
    
    public String formatImageFilesSize() {
        return formatSize(imageFilesSize);
    }

    public String formatTotalFilesSize() {
        return formatSize(databaseSize + contentFilesSize + imageFilesSize);
    }
}
