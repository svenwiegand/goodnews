package com.gettingmobile.goodnews.settings;

import com.gettingmobile.goodnews.R;
import com.gettingmobile.goodnews.account.LoginAction;
import com.gettingmobile.goodnews.sync.DismissLocalChangesAction;

final class SyncSettingsHandler extends SingleResourceSettingsHandler {
    @Override
    public int getPrefResourceId() {
        return R.xml.pref_sync;
    }

    @Override
    public void setup(SettingsManager m) {
        m.registerAction("account", new LoginAction());
        m.registerAction("sync_dismiss_local_changes", new DismissLocalChangesAction());
    }
}
