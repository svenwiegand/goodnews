package com.gettingmobile.google.reader.sync;

import android.util.Log;
import com.gettingmobile.rest.RequestProcessor;

public final class Synchronizer {
    public static final String LOG_TAG = "goodnews.Synchronizer";
    private final SyncSettings settings;
    private final CallbackHandler callbackHandler;
	private final PushSynchronizer pushSynchronizer;
	private final PullSynchronizer pullSynchronizer;
	
	public Synchronizer(SyncContext context) {
        settings = context.getSettings();
		callbackHandler = new CallbackHandler();
		pushSynchronizer = new PushSynchronizer(context);
		pullSynchronizer = new PullSynchronizer(context);
	}
	
	protected PushSynchronizer getPushSynchronizer() {
		return pushSynchronizer;
	}

	protected PullSynchronizer getPullSynchronizer() {
		return pullSynchronizer;
	}

	public void fullSync(SyncCallback callback) {
		if (!pullSynchronizer.isSyncing() && !pushSynchronizer.isSyncing()) {
            resetCancelled();
            final SyncCallbackHelper c = new SimpleSyncCallbackHelper(callbackHandler, callback);
            try {
                c.setMaxProgress(pushSynchronizer.forecastMaxProgress() + pullSynchronizer.forecastMaxProgress());
                final RequestProcessor requestProcessor = new RequestProcessor();
                pushSynchronizer.sync(requestProcessor, c);
                pullSynchronizer.sync(requestProcessor, c);
                c.onSyncFinished(null);
            } catch (SyncException error) {
                Log.e(LOG_TAG, "Full sync failed", error);
                settings.updateLastFailedSyncTimestamp();
                c.onSyncFinished(error);
            } catch (Throwable error) {
                Log.e(LOG_TAG, "Full sync failed", error);
                settings.updateLastFailedSyncTimestamp();
                c.onSyncFinished(new SyncException(error));
            }
		}
	}

	public void pushSync(SyncCallback callback) {
        if (!pullSynchronizer.isSyncing() && !pushSynchronizer.isSyncing()) {
            resetCancelled();
            final SyncCallbackHelper c = new SimpleSyncCallbackHelper(callbackHandler, callback);
            try {
                c.setMaxProgress(pushSynchronizer.forecastMaxProgress());
                final RequestProcessor requestProcessor = new RequestProcessor();
                pushSynchronizer.sync(requestProcessor, c);
                c.onSyncFinished(null);
            } catch (SyncException error) {
                Log.e(LOG_TAG, "Push sync failed", error);
                settings.updateLastFailedSyncTimestamp();
                c.onSyncFinished(error);
            } catch (Throwable error) {
                Log.e(LOG_TAG, "Push sync failed", error);
                settings.updateLastFailedSyncTimestamp();
                c.onSyncFinished(new SyncException(error));
            }
        }
	}

    public void cancel() {
        pullSynchronizer.cancel();
        pushSynchronizer.cancel();
    }

    private void resetCancelled() {
        pullSynchronizer.resetCancelled();
        pushSynchronizer.resetCancelled();
    }

    public boolean isPushRequired() {
        return pushSynchronizer.isPushRequired();
    }
}
