package com.gettingmobile.goodnews.sync;

import android.content.SharedPreferences;
import com.gettingmobile.android.app.settings.AbstractSettings;
import com.gettingmobile.android.app.settings.OnSettingChangeListener;

public class OnContinousSyncIntervalSettingChangeListener implements OnSettingChangeListener {
    private final SyncService syncService;

    public OnContinousSyncIntervalSettingChangeListener(SyncService syncService) {
        this.syncService = syncService;
    }

    @Override
    public void onSettingChanged(AbstractSettings settings, SharedPreferences sharedPreferences, String s) {
        syncService.scheduleContinousFullSyncIfApplicable();
    }
}
