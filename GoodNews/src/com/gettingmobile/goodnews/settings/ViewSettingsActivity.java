package com.gettingmobile.goodnews.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import roboguice.RoboGuice;

public final class ViewSettingsActivity extends AbstractSettingsActivity {
    public static final String EXTRA_KEY_BASE = "com.gettingmobile.goodnews.";
    public static final String EXTRA_KEY_SETTINGS_HANDLER_CLASS = EXTRA_KEY_BASE + "SETTINGS_HANDLER_CLASS";
    private SettingsHandler settingsHandler = null;

    public static Intent createIntent(Context packageContext, Class<? extends SettingsHandler> handlerClass) {
        final Intent intent = new Intent(packageContext, ViewSettingsActivity.class);
        intent.putExtra(EXTRA_KEY_SETTINGS_HANDLER_CLASS, handlerClass);
        return intent;
    }
    
    private SettingsHandler createSettingsHandler() {
        //noinspection unchecked
        return RoboGuice.getInjector(this).getInstance((Class<? extends SettingsHandler>)
                getIntent().getSerializableExtra(EXTRA_KEY_SETTINGS_HANDLER_CLASS));
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        settingsHandler = createSettingsHandler();
        for (int resId : settingsHandler.getPreferenceResourceIds()) {
            addPreferencesFromResource(resId);
        }

        /*
         * We are using a trick for all the view settings to provide them all as preference screen in the global
         * settings, they are each wrapped into a preference screen which we would like to omit if they are used
         * standalone, which is the case here. So we take the single preference entry and -- if it is a preference
         * screen -- set it as the preference root.
         */
        final PreferenceScreen currentRoot = getPreferenceScreen();
        if (currentRoot.getPreferenceCount() == 1) {
            final Preference singlePref = currentRoot.getPreference(0);
            if (singlePref instanceof PreferenceScreen) {
                setPreferenceScreen((PreferenceScreen) singlePref);
            }
        }

        settingsHandler.setup(this);
    }

    @Override
    protected void onDestroy() {
        settingsHandler.cleanup();
        super.onDestroy();
    }
}
