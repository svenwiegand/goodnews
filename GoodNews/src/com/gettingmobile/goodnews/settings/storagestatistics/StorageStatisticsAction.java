package com.gettingmobile.goodnews.settings.storagestatistics;

import com.gettingmobile.android.app.actions.ActionContext;
import com.gettingmobile.goodnews.Application;
import com.gettingmobile.goodnews.R;
import com.gettingmobile.goodnews.settings.PreferenceAction;

public class StorageStatisticsAction extends PreferenceAction {
    private StorageStatistics statistics = null;
    
    public StorageStatisticsAction() {
        super(R.string.pref_storage_statistics, R.string.pref_storage_statistics_confirm, R.string.pref_storage_statistics_failed);
    }

    @Override
    protected void asyncPerform(ActionContext<? extends Application> context) throws Throwable {
        final Application app = context.getApp();
        statistics = new StorageStatisticsCollector(
                app.getDbHelper().getReadOnlyDatabase(),
                app.getSettings().getDatabaseStorageProvider(),
                app.getSettings().getContentStorageProvider()).getStorageStatistics();
    }

    @Override
    protected void onSuccess(ActionContext<? extends Application> context) {
        if (statistics != null) {
            StorageStatisticsDialogHandler.start(context.getActivity(), statistics);
        }
    }
}
