package com.gettingmobile.goodnews.itemview;

import android.app.Application;
import com.gettingmobile.android.app.actions.ActionContext;
import com.gettingmobile.goodnews.settings.SettingsIntentFactory;
import com.gettingmobile.google.reader.Item;
import com.google.inject.Inject;

final class ItemFeedSettingsAction extends AbstractItemAction {
    private final SettingsIntentFactory settingsIntentFactory;

    @Inject
    public ItemFeedSettingsAction(SettingsIntentFactory settingsIntentFactory) {
        this.settingsIntentFactory = settingsIntentFactory;
    }

    @Override
    protected int getState(ActionContext<? extends Application> context, Item item) {
        return item.getFeedId() != null ? ENABLED : GONE;
    }

    @Override
    protected boolean onFired(ActionContext<? extends Application> context, Item item) {
        context.getActivity().startActivity(
                settingsIntentFactory.createFeedSettingsIntent(item.getFeedId()));
        return true;
    }
}
