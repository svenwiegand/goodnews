package com.gettingmobile.google.reader.sync;

import com.gettingmobile.goodnews.storage.StorageProvider;
import com.gettingmobile.google.reader.ElementId;
import com.gettingmobile.google.reader.ItemContentTreatment;
import com.gettingmobile.google.reader.ItemTeaserSource;

import java.util.Calendar;

public interface SyncSettings {
	boolean getPushImmediately();
	int getMaxUnreadSync();
    Calendar getMinUnreadTimestamp();
    void setMinUnreadTimestamp(Calendar timestamp);
    int getMaxUnreadKeeping();
	int getMaxTaggedSync();
    int getDaysToCache();
    int getDaysToCleanupUnread();
    boolean shouldSyncTag(ElementId tagId);
    void updateLastSuccessfulPullTimestamp();
    void updateLastSuccessfulPushTimestamp();
    void updateLastFailedSyncTimestamp();
	ElementId getLabelReadListId();
    boolean cancelSyncOnLowDeviceStorage();
    StorageProvider getContentStorageProvider();
    StorageProvider getDatabaseStorageProvider();
    boolean storeContentInFiles();
    boolean shouldIgnoreUnread(ElementId feedId);
    boolean autoListFeedArticles(ElementId feedId);
    ItemContentTreatment getFeedSummaryTreatment(ElementId feedId);
    ItemContentTreatment getFeedContentTreatment(ElementId feedId);
    int getTeaserWordCount();
    ItemTeaserSource getFeedTeaserSource(ElementId feedId);
    int getFeedTeaserStartChar(ElementId feedId);
    boolean shouldFixDuplicateItems();
}
