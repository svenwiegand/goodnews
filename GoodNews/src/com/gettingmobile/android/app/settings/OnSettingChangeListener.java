package com.gettingmobile.android.app.settings;

import android.content.SharedPreferences;

public interface OnSettingChangeListener {
    public void onSettingChanged(AbstractSettings settings, SharedPreferences sharedPreferences, String key);
}
