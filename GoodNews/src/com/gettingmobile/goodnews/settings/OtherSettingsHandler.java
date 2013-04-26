package com.gettingmobile.goodnews.settings;

import com.gettingmobile.goodnews.R;
import com.gettingmobile.goodnews.actions.DeleteItemBlacklistAction;

final class OtherSettingsHandler extends SingleResourceSettingsHandler {
    @Override
    public int getPrefResourceId() {
        return R.xml.pref_other;
    }

    @Override
    public void setup(SettingsManager m) {
        super.setup(m);

        m.registerAction("item_blacklist_reset", new DeleteItemBlacklistAction());
    }
}
