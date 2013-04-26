package com.gettingmobile.goodnews.settings;

import android.os.Bundle;
import com.gettingmobile.goodnews.R;

public final class RootSettingsActivity extends FullSettingsActivity {
    private void registerSubActivity(String key, int iconResource, Class<? extends SettingsHandler> settingsHandlerClass) {
        final IconPreferenceScreen pref = (IconPreferenceScreen) findPreference(key);
        pref.setIcon(getResources().getDrawable(iconResource));
        pref.setIntent(SubSettingsActivity.createIntent(this, settingsHandlerClass));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
        registerSubActivity("sync", R.drawable.ic_pref_sync, SyncSettingsHandler.class);
        registerSubActivity("ui", R.drawable.ic_pref_ui, UiSettingsHandler.class);
        registerSubActivity("news_reading", R.drawable.ic_pref_news_reading, NewsReadingSettingsHandler.class);
        registerSubActivity("storage", R.drawable.ic_pref_storage, StorageSettingsHandler.class);
        registerSubActivity("other", R.drawable.ic_pref_other, OtherSettingsHandler.class);
        registerSubActivity("app", R.drawable.ic_pref_info, AppSettingsHandler.class);
    }
}
