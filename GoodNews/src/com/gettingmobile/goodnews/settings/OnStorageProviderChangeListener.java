package com.gettingmobile.goodnews.settings;

import android.preference.Preference;
import com.gettingmobile.android.app.DialogFactory;
import com.gettingmobile.goodnews.R;

class OnStorageProviderChangeListener implements Preference.OnPreferenceChangeListener {
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        DialogFactory.showConfirmationDialog(preference.getContext(),
                R.string.pref_storage_provider, R.string.pref_storage_provider_confirmation, R.string.ok);
        return true;
    }
}
