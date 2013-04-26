package com.gettingmobile.goodnews.sync;

import com.gettingmobile.android.app.ServiceListener;

public interface SyncListener extends ServiceListener {
    void onSyncStarted();
    void onSyncFinished(boolean fullSync, Throwable error);
}
