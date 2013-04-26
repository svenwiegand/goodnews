package com.gettingmobile.goodnews.itemview;

import android.app.Activity;
import android.util.Log;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.ShareActionProvider;

final class ActionProviderItemUrlSharer extends ItemUrlSharer {
    private ShareActionProvider actionProvider = null;

    public ActionProviderItemUrlSharer(Activity activity) {
        super(activity);
    }

    @Override
    public void onPrepareMenu(MenuItem shareItem) {
        Log.d(LOG_TAG, "preparing ShareActionProvider");
        actionProvider = (ShareActionProvider) shareItem.getActionProvider();
        super.onPrepareMenu(shareItem);
    }

    @Override
    public boolean handleAction() {
        Log.d(LOG_TAG, "ignoring handleAction as handled by ShareActionProvider");
        return false;
    }

    @Override
    protected void onIntentChanged() {
        Log.d(LOG_TAG, "updating ShareActionProvider intent");
        if (actionProvider != null)
            actionProvider.setShareIntent(intent);
    }
}
