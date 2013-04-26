package com.gettingmobile.goodnews.settings;

import android.content.Intent;
import com.gettingmobile.goodnews.Application;
import com.gettingmobile.google.reader.ElementId;
import com.google.inject.Inject;

public final class SettingsIntentFactory {
    private final Application app;
    private final Class<? extends FullSettingsActivity> settingsActivityClass;

    @Inject
    public SettingsIntentFactory(Application app, Class<? extends FullSettingsActivity> settingsActivityClass) {
        this.app = app;
        this.settingsActivityClass = settingsActivityClass;
    }

    public Intent createStandardIntent() {
        return new Intent(app, settingsActivityClass);
    }

    public Intent createElementSettingsIntent(ElementId elementId, String elementTitle) {
        return ElementSettingsActivity.createElementSettingsIntent(app, elementId, elementTitle);
    }

    public Intent createFeedSettingsIntent(ElementId elementId) {
        return ElementSettingsActivity.createFeedSettingsIntent(app, elementId);
    }
}
