package com.gettingmobile.google.reader.sync;

interface SyncCallbackHelper {
	void setMaxProgress(int maxProgress);
	void addMaxProgress(int additionalMaxProgress);
	void incrementProgress();
    void incrementProgress(int steps);
    void setUnreadCount(int unreadCount);
    void setNewUnreadCount(int newUnreadCount);
    void incrementSkipCount();
	void onSyncFinished(SyncException error);
}
