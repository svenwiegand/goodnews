package com.gettingmobile.goodnews.settings;

import com.gettingmobile.android.app.actions.ActionContext;
import com.gettingmobile.goodnews.Application;
import com.gettingmobile.goodnews.R;

class CleanDatabaseAction extends PreferenceAction {
    public CleanDatabaseAction() {
        super(R.string.pref_database_clean, R.string.pref_database_clean_confirm, R.string.pref_database_clean_failed);
    }

    @Override
    protected void asyncPerform(ActionContext<? extends Application> context) throws Throwable {
        context.getApp().getDbHelper().recreateOpenDatabase(
                context.getApp().getSettings().getDatabaseStorageProvider());
    }
}
