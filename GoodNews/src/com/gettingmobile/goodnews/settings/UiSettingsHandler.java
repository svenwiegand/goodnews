package com.gettingmobile.goodnews.settings;

import com.gettingmobile.android.util.ApiLevel;

class UiSettingsHandler extends SettingsHandler {
    protected final SingleResourceSettingsHandler[] viewSettingsHandlers = new SingleResourceSettingsHandler[] {
            new TagListSettingsHandler(), new FeedListSettingsHandler(), 
            new ItemListSettingsHandler(), new ItemViewSettingsHandler()
    };

    @Override
    public int[] getPreferenceResourceIds() {
        final int[] resIds = new int[viewSettingsHandlers.length];
        for (int i = 0; i < resIds.length; ++i) {
            resIds[i] = viewSettingsHandlers[i].getPrefResourceId();
        }
        return resIds;
    }

    @Override
    public void setup(SettingsManager m) {
        super.setup(m);
        for (SingleResourceSettingsHandler h : viewSettingsHandlers) {
            h.setup(m);
        }
    }

    @Override
    public void cleanup() {
        super.cleanup();
        for (SingleResourceSettingsHandler h : viewSettingsHandlers) {
            h.cleanup();
        }
    }
}
