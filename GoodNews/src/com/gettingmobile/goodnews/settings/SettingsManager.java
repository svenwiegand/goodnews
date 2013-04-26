package com.gettingmobile.goodnews.settings;

import android.preference.Preference;
import android.preference.PreferenceScreen;
import com.gettingmobile.android.app.actions.ActionContext;
import com.gettingmobile.android.app.actions.ActionController;
import com.gettingmobile.goodnews.Application;

public interface SettingsManager extends ActionController<String> {
    Application getApp();
    ActionContext<Application> getActionContext();
    void registerChangeListener(String key, Preference.OnPreferenceChangeListener l);
    PreferenceScreen getPreferenceScreen();
    Preference findPreference(CharSequence key);
    void updateActionPreferenceStatus();
}
