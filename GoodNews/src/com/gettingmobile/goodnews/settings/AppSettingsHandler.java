package com.gettingmobile.goodnews.settings;

import com.gettingmobile.android.app.actions.BrowserAction;
import com.gettingmobile.goodnews.R;
import com.gettingmobile.goodnews.changelog.ViewChangelogAction;

final class AppSettingsHandler extends SingleResourceSettingsHandler {
    @Override
    public int getPrefResourceId() {
        return R.xml.pref_app;
    }

    @Override
    public void setup(final SettingsManager m) {
        m.registerAction("info_changelog", new ViewChangelogAction());
        m.registerAction("info_homepage", new BrowserAction("http://www.goodnews-mobile.com"));
    }
}
