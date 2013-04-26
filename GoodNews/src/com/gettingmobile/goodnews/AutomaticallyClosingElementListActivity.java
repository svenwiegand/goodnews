package com.gettingmobile.goodnews;

import com.gettingmobile.android.app.actions.AbstractAction;
import com.gettingmobile.android.app.actions.Action;
import com.gettingmobile.android.app.actions.ActionContext;
import com.gettingmobile.goodnews.actions.FetchOldArticlesAction;
import com.gettingmobile.goodnews.sync.SyncListener;
import com.gettingmobile.goodnews.widget.MessageBar;
import com.gettingmobile.google.reader.ElementType;

public abstract class AutomaticallyClosingElementListActivity extends ElementListActivity implements SyncListener {
    private final Action<Application> reloadViewAction = new ReloadViewAction();
    private final Action<Application> deactivateSyncNotificationAction = new DeactivateSyncFinishedNotificationAction();
    protected MessageBar msgBar;

    protected AutomaticallyClosingElementListActivity() {
        super();
    }

    @Override
    protected void onCreate() {
        super.onCreate();
        msgBar = new MessageBar(this);
        getApp().getSyncService().addListener(this);
    }

    @Override
    protected void onDestroy() {
        getApp().getSyncService().removeListener(this);
        super.onDestroy();
    }

    private boolean canFetchOldArticles() {
        return getIntentElementId().getType() == ElementType.FEED || getIntentElementIsFolder();
    }

    @Override
    protected void onShowedNoContentPanel() {
        super.onShowedNoContentPanel();

        /*
         * adjust the no content message
         */
        if (getHideRead()) {
            /*
             * register action to show read items
             */
            setNoContentMsg(R.string.no_content_show_read);
            registerAction(R.id.no_content, new AbstractAction<android.app.Application>() {
                @Override
                public boolean onFired(ActionContext<? extends android.app.Application> applicationActionContext) {
                    setHideRead(false);
                    return true;
                }
            });
        } else if (canFetchOldArticles()) {
            /*
             * register action to bring up fetch old item dialog
             */
            setNoContentMsg(R.string.no_content_fetch_old);
            registerAction(R.id.no_content, new FetchOldArticlesAction(getIntentElementId()));
        } else {
            /*
             * show standard message
             */
            setNoContentMsg(R.string.no_content);
            unregisterAction(R.id.no_content);
        }
    }

    @Override
    protected void onRegisterActions() {
        super.onRegisterActions();

        /*
         * allow to fetch old items if applicable
         */
        if (canFetchOldArticles()) {
            registerAction(R.id.menu_fetch_old, new FetchOldArticlesAction(getIntentElementId()));
        }
    }

    protected AutomaticallyClosingElementListActivity(int tipGroupId) {
        super(tipGroupId);
    }

    protected boolean automaticallyCloseWhenRead() {
        return getHideRead() && getApp().getSettings().automaticallyCloseReadItemList();
    }

    /**
     * Returns whether all items of the list are marked as read. This only needs to respect the case when read items
     * are filtered out.
     * @return whether all items of the list are marked as read.
     */
    protected abstract boolean isUnreadListRead();

    protected boolean finishIfApplicable() {
        if (automaticallyCloseWhenRead() && isUnreadListRead()) {
            finish();
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected boolean onSetHideRead(boolean hideRead) {
        return !finishIfApplicable();
    }

    @Override
    protected boolean onViewUpdated() {
        super.onViewUpdated();
        return !finishIfApplicable();
    }

    @Override
    protected boolean onViewLoaded() {
        super.onViewLoaded();
        return !(isRecreated() && finishIfApplicable());
    }

    /*
     * sync handler
     */

    @Override
    public void onSyncStarted() {
        // don't care
    }

    @Override
    public void onSyncFinished(boolean fullSync, Throwable error) {
        if (fullSync && error == null && getApp().getSettings().showSyncFinishedNotificationInLists()) {
            msgBar.setClickAction(reloadViewAction);
            msgBar.setLongClickAction(deactivateSyncNotificationAction);
            msgBar.showInfo(R.string.sync_reload_view);
        }
    }

    /*
     * inner classes
     */

    class ReloadViewAction extends AbstractAction<Application> {
        @Override
        public boolean onFired(ActionContext<? extends Application> actionContext) {
            msgBar.dismiss();
            loadView();
            return true;
        }
    }

    class DeactivateSyncFinishedNotificationAction extends AbstractAction<Application> {
        @Override
        public boolean onFired(ActionContext<? extends Application> actionContext) {
            msgBar.dismiss();
            getApp().getSettings().setShowSyncFinishedNotificationInLists(false);
            return true;
        }
    }
}
