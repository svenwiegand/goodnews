package com.gettingmobile.goodnews.actions;

import com.gettingmobile.android.app.DialogFactory;
import com.gettingmobile.android.app.actions.AbstractAction;
import com.gettingmobile.android.app.actions.ActionContext;
import com.gettingmobile.goodnews.Application;
import com.gettingmobile.goodnews.R;
import com.gettingmobile.google.reader.db.ItemBlacklistDatabaseAdapter;

public class DeleteItemBlacklistAction extends AbstractAction<Application> {
    @Override
    public boolean onFired(ActionContext<? extends Application> actionContext) {
        new ItemBlacklistDatabaseAdapter().delete(actionContext.getApp().getDbHelper().getDatabase());
        DialogFactory.showConfirmationDialog(actionContext.getActivity(),
                R.string.pref_item_blacklist_reset, R.string.pref_item_blacklist_reset_done, R.string.ok);
        return true;
    }
}
