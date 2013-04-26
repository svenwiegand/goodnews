package com.gettingmobile.google.reader.sync;

public interface SyncCallback {
	void onProgressUpdate(int progress, int max);
	void onSyncFinished(SyncException error, int unreadCount, int newUnreadCount, int skipCount);
}
