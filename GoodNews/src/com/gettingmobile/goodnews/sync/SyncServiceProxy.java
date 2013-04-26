package com.gettingmobile.goodnews.sync;

import com.gettingmobile.android.app.ServiceProxy;

public class SyncServiceProxy extends ServiceProxy<SyncService, SyncListener> {
    public SyncServiceProxy() {
        super(SyncService.class);
    }
}
