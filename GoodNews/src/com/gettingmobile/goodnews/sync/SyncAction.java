package com.gettingmobile.goodnews.sync;

import android.content.DialogInterface;
import com.gettingmobile.android.app.DialogFactory;
import com.gettingmobile.android.app.actions.AbstractBusyAction;
import com.gettingmobile.android.app.actions.ActionContext;
import com.gettingmobile.goodnews.Application;
import com.gettingmobile.goodnews.R;
import com.google.inject.Inject;

public abstract class SyncAction extends AbstractBusyAction<Application> implements SyncListener {
    protected final SyncServiceProxy syncServiceProxy;

    @Inject
    public SyncAction(SyncServiceProxy syncServiceProxy) {
        this.syncServiceProxy = syncServiceProxy;
        syncServiceProxy.addListener(this);
    }
    
    public void onDestroy() {
        syncServiceProxy.removeListener(this);
    }

    @Override
    public boolean onFired(ActionContext<? extends Application> context) {
        if (context.getApp().checkInternetAvailableAndRequired(context.getActivity())) {
            try {
                final SyncService service = syncServiceProxy.getService();
                if (service != null) {
                    doSync(context, service);
                }
            } catch (IllegalStateException ex) {
                cancelSync(context);
            }
        }
        return true;
    }

    protected void cancelSync(final ActionContext<? extends Application> context) {
        DialogFactory.buildYesNoDialog(context.getActivity(),
                R.string.sync_title, R.string.sync_cancel_confirmation, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                final SyncService service = context.getApp().getSyncService().getService();
                if (service != null) {
                    service.cancelSync();
                }
            }
        }).show();
    }

    protected abstract void doSync(ActionContext<? extends Application> context, SyncService service);

    @Override
    public boolean onStop(final ActionContext<? extends Application> context) {
        cancelSync(context);
        return true;
    }
    
    /*
     * SyncListener implementation
     */

    @Override
    public void onSyncStarted() {
        fireOnStarted();
    }

    @Override
    public void onSyncFinished(boolean fullSync, Throwable error) {
        fireOnStopped();
    }
}
