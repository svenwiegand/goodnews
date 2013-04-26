package com.gettingmobile.google.reader.sync;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

class CallbackHandler extends Handler {
	private static final int PROGRESS_UPDATE = 1;
	private static final int SYNC_FINISHED = 2;
	private static final String DATAKEY_ERROR = "error";
    private static final String DATAKEY_UNREAD_COUNT = "unreadCount";
    private static final String DATAKEY_NEW_UNREAD_COUNT = "newUnreadCount";
    private static final String DATAKEY_SKIP_COUNT = "skipCount";
	
	public void sendProgressUpdate(SyncCallback callback, int progress, int max) {
		if (callback != null) {
			sendMessage(obtainMessage(PROGRESS_UPDATE, progress, max, callback));
		}
	}
	
	public void sendSyncFinished(SyncCallback callback, SyncException error,
                                 int unreadCount, int newUnreadCount, int skipCount) {
        Log.d(AbstractSynchronizer.LOG_TAG, "Sync finished called.");
		if (callback != null) {
			final Message msg = obtainMessage(SYNC_FINISHED, callback);
            msg.getData().putInt(DATAKEY_UNREAD_COUNT, unreadCount);
            msg.getData().putInt(DATAKEY_NEW_UNREAD_COUNT, newUnreadCount);
            msg.getData().putInt(DATAKEY_SKIP_COUNT, skipCount);
			msg.getData().putSerializable(DATAKEY_ERROR, error);
			sendMessage(msg);
		}
	}

	@Override
	public void handleMessage(Message msg) {
		switch (msg.what) {
		case PROGRESS_UPDATE:
			((SyncCallback) msg.obj).onProgressUpdate(msg.arg1, msg.arg2);
			break;
		case SYNC_FINISHED:
			((SyncCallback) msg.obj).onSyncFinished(
                    (SyncException) msg.getData().getSerializable(DATAKEY_ERROR),
                    msg.getData().getInt(DATAKEY_UNREAD_COUNT),
                    msg.getData().getInt(DATAKEY_NEW_UNREAD_COUNT),
                    msg.getData().getInt(DATAKEY_SKIP_COUNT));
			break;
		}
		super.handleMessage(msg);
	}
	
}
