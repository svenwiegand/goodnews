package com.gettingmobile.goodnews.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class SubSettingsActivity extends AbstractSettingsActivity {
    public static final String EXTRA_KEY_BASE = "com.gettingmobile.goodnews.";
    public static final String EXTRA_KEY_SETTINGS_HANDLER_CLASS = EXTRA_KEY_BASE + "SETTINGS_HANDLER_CLASS";
    private SettingsHandler settingsHandler = null;

    public static Intent createIntent(Context packageContext, Class<? extends SettingsHandler> settingsHandlerClass) {
        final Intent intent = new Intent(packageContext, SubSettingsActivity.class);
        intent.putExtra(EXTRA_KEY_SETTINGS_HANDLER_CLASS, settingsHandlerClass);
        return intent;
    }

    private SettingsHandler createSettingsHandler() {
        final Class<?> settingsHandlerClass = (Class<?>)
                getIntent().getExtras().getSerializable(EXTRA_KEY_SETTINGS_HANDLER_CLASS);
        try {
            return (SettingsHandler) settingsHandlerClass.newInstance();
        } catch (Exception ex) {
            throw new RuntimeException("Failed to create SettingsHandler", ex);
        }
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        settingsHandler = createSettingsHandler();
        for (int resId : settingsHandler.getPreferenceResourceIds()) {
            addPreferencesFromResource(resId);
        }
        settingsHandler.setup(this);
        updateActionPreferenceStatus();
    }

    @Override
    protected void onDestroy() {
        settingsHandler.cleanup();
        super.onDestroy();
    }
}
