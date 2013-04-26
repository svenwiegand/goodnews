package com.gettingmobile.goodnews.sync;

import com.gettingmobile.android.app.actions.ActionContext;
import com.gettingmobile.goodnews.Application;
import com.google.inject.Inject;

public class PushSyncAction extends SyncAction {
    @Inject
    public PushSyncAction(SyncServiceProxy syncServiceProxy) {
        super(syncServiceProxy);
    }

    @Override
    public int getState(ActionContext<? extends Application> context) {
        final SyncService svc = syncServiceProxy.getService();
        return svc == null || svc.isIdle() ? ENABLED : DISABLED;
    }

    @Override
    protected void doSync(ActionContext<? extends Application> context, SyncService service) {
        service.startPushSync();
    }
}
