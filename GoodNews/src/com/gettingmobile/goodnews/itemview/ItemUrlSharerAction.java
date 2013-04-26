package com.gettingmobile.goodnews.itemview;

import com.gettingmobile.android.app.actions.Action;
import com.gettingmobile.android.app.actions.ActionContext;
import com.gettingmobile.goodnews.Application;

final class ItemUrlSharerAction implements Action<Application> {
    private final ItemUrlSharer sharer;

    public ItemUrlSharerAction(ItemUrlSharer sharer) {
        this.sharer = sharer;
    }

    @Override
    public int getState(ActionContext<? extends Application> actionContext) {
        return sharer.hasUrl() ? Action.ENABLED : Action.DISABLED;
    }

    @Override
    public boolean onFired(ActionContext<? extends Application> actionContext) {
        return sharer.handleAction();
    }
}
