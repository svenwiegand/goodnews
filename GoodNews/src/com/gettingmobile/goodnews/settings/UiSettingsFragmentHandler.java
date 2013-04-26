package com.gettingmobile.goodnews.settings;

import android.preference.PreferenceScreen;

final class UiSettingsFragmentHandler extends UiSettingsHandler {
    @Override
    public void setup(SettingsManager m) {
        // *not* calling super implementation

        /*
         * tell each preference screen which settings handler to instantiate
         */
        final PreferenceScreen root = m.getPreferenceScreen();
        if (root.getPreferenceCount() != viewSettingsHandlers.length)
            throw new IllegalStateException("Unexpected number of preferences!");
        
        for (int i = 0; i < viewSettingsHandlers.length; ++i) {
            root.getPreference(i).getExtras().putString(SettingsFragment.ARG_SETTINGS_HANDLER,
                    viewSettingsHandlers[i].getClass().getSimpleName());
        }
    }

    @Override
    public void cleanup() {
        // nothing to be done
    }
}
