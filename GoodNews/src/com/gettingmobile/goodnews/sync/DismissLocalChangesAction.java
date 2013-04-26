package com.gettingmobile.goodnews.sync;

import com.gettingmobile.android.app.actions.ActionContext;
import com.gettingmobile.goodnews.Application;
import com.gettingmobile.goodnews.R;
import com.gettingmobile.goodnews.settings.PreferenceAction;
import com.gettingmobile.google.reader.db.ItemTagChangeDatabaseAdapter;

public class DismissLocalChangesAction extends PreferenceAction {
    public DismissLocalChangesAction() {
        super(R.string.pref_sync_dismiss_local_changes, R.string.pref_sync_dismiss_local_changes_confirmation);
    }

    @Override
    protected void asyncPerform(ActionContext<? extends Application> context) throws Throwable {
        ItemTagChangeDatabaseAdapter.dismissGlobalChanges(context.getApp().getDbHelper().getDatabase());
    }
}
