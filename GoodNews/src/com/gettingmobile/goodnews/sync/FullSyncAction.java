package com.gettingmobile.goodnews.sync;

import com.gettingmobile.android.app.DialogFactory;
import com.gettingmobile.android.app.actions.ActionContext;
import com.gettingmobile.android.content.LowDeviceStorageDetector;
import com.gettingmobile.goodnews.Application;
import com.gettingmobile.goodnews.R;
import com.google.inject.Inject;

public class FullSyncAction extends SyncAction {
    @Inject
    public FullSyncAction(SyncServiceProxy syncServiceProxy) {
        super(syncServiceProxy);
    }

    @Override
    public int getState(ActionContext context) {
        final SyncService svc = syncServiceProxy.getService();
        return svc == null || svc.isIdle() ? ENABLED : BUSY;
    }

    @Override
    protected void doSync(ActionContext<? extends Application> context, SyncService service) {
        if (context.getApp().getSettings().cancelSyncOnLowDeviceStorage() && LowDeviceStorageDetector.isDeviceStorageLow(context.getApp())) {
            DialogFactory.showErrorDialog(context.getActivity(), R.string.sync_title,
                    context.getActivity().getString(R.string.sync_failed_device_storage_low_long));
        } else {
            service.startFullSync();
        }
    }
}
