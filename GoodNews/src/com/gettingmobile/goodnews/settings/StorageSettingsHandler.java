package com.gettingmobile.goodnews.settings;

import com.gettingmobile.goodnews.R;
import com.gettingmobile.goodnews.download.DeleteContentAction;
import com.gettingmobile.goodnews.settings.storagestatistics.StorageStatisticsAction;

final class StorageSettingsHandler extends SingleResourceSettingsHandler {
    @Override
    public int getPrefResourceId() {
        return R.xml.pref_storage;
    }

    @Override
    public void setup(SettingsManager m) {
        m.registerAction("storage_statistics", new StorageStatisticsAction());
        m.registerAction("database_clean", new CleanDatabaseAction());
        m.registerAction("delete_unreferenced", new DeleteUnreferencedItemsAction());
        m.registerAction("content_delete", new DeleteContentAction());

        m.registerChangeListener("database_storage_provider",
                new DatabaseStorageProviderChangeListener(m.getActionContext()));
    }
}
