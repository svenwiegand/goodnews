package com.gettingmobile.goodnews.backup;

import android.app.backup.BackupAgentHelper;
import android.util.Log;

public class BackupAgent extends BackupAgentHelper {
    private static final String LOG_TAG = "goodnews.Backup";
    private static final String KEY_SETTINGS = "settings";

    @Override
    public void onCreate() {
        Log.i(LOG_TAG, "creating backup agent");
        final SettingsBackupHelper seetingsBackupHelper = new SettingsBackupHelper(this);
        addHelper(KEY_SETTINGS, seetingsBackupHelper);
    }
}
