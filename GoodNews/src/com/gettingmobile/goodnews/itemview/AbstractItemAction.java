package com.gettingmobile.goodnews.itemview;

import android.app.Application;
import com.gettingmobile.android.app.actions.AbstractAction;
import com.gettingmobile.android.app.actions.ActionContext;
import com.gettingmobile.google.reader.Item;

abstract class AbstractItemAction extends AbstractAction<Application> {
    protected Item getItem(ActionContext context) {
        return ((ItemViewActivity) context.getActivity()).getItem();
    }

    @Override
    public int getState(ActionContext<? extends Application> context) {
        final Item item = getItem(context);
        return item != null ? getState(context, item) : GONE;
    }

    protected int getState(ActionContext<? extends Application> context, Item item) {
        return ENABLED;
    }

    @Override
    public boolean onFired(ActionContext<? extends Application> context) {
        final Item item = getItem(context);
        return item == null || onFired(context, item);
    }

    protected abstract boolean onFired(ActionContext<? extends Application> context, Item item);
}
