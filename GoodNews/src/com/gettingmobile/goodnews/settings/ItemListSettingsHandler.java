package com.gettingmobile.goodnews.settings;

import com.gettingmobile.android.app.actions.GenerateTeaserAction;
import com.gettingmobile.goodnews.R;

public final class ItemListSettingsHandler extends ViewSettingsHandler {
    @Override
    protected int getPrefResourceId() {
        return R.xml.pref_view_item_list;
    }

    @Override
    public void setup(SettingsManager m) {
        super.setup(m);
        m.registerAction("teaser_generate", new GenerateTeaserAction());
    }
}
