package com.gettingmobile.goodnews.backup;

import android.app.backup.BackupManager;
import android.content.SharedPreferences;
import android.util.Log;
import com.gettingmobile.goodnews.Application;

class StandardBackupManager implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String LOG_TAG = "goodnews.Backup";
    private final BackupManager backupManager;

    public StandardBackupManager(Application app) {
        backupManager = new BackupManager(app);
        app.getSettings().registerChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        Log.d(LOG_TAG, "scheduling cloud backup");
        backupManager.dataChanged();
    }
}
