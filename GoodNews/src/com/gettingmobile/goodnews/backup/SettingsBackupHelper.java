package com.gettingmobile.goodnews.backup;

import android.app.backup.BackupDataInputStream;
import android.app.backup.BackupDataOutput;
import android.app.backup.SharedPreferencesBackupHelper;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import com.gettingmobile.goodnews.settings.Settings;

import java.util.Map;
import java.util.Set;

class SettingsBackupHelper extends SharedPreferencesBackupHelper {
    private static final String LOG_TAG = "goodnews.Backup";
    private static final String BACKUP_PREFERENCES_SUFFIX = "_preferences_backup";
    private final Context context;

    SettingsBackupHelper(Context context) {
        super(context, context.getPackageName() + BACKUP_PREFERENCES_SUFFIX);
        this.context = context;
    }

    private void copyPreferences(SharedPreferences from, SharedPreferences to, boolean clear) {
        Log.d(LOG_TAG, "Copying preferences");
        final SharedPreferences.Editor editor = to.edit();
        if (clear)
            editor.clear();
        
        final Map<String, ?> entries = from.getAll();
        for (Map.Entry<String, ?> entry : entries.entrySet()) {
            final String key = entry.getKey();
            final Object value = entry.getValue();
            if (value instanceof Boolean) {
                editor.putBoolean(key, (Boolean) value);
            } else if (value instanceof Float) {
                editor.putFloat(key, (Float) value);
            } else if (value instanceof Integer) {
                editor.putInt(key, (Integer) value);
            } else if (value instanceof Long) {
                editor.putLong(key, (Long) value);
            } else if (value instanceof String) {
                editor.putString(key, (String) value);
            } else if (value instanceof Set) {
                //noinspection unchecked
                editor.putStringSet(key, (Set<String>) value);
            } else {
                Log.w(LOG_TAG, "Value " + key + " of unsupported type " +
                        (value != null ? value.getClass().getName() : "null"));
            }
        }
        editor.commit();
    }

    @Override
    public void performBackup(ParcelFileDescriptor oldState, BackupDataOutput data, ParcelFileDescriptor newState) {
        /*
         * copy original preferences to backup preferences and backup those
         */
        copyPreferences(Settings.getDefaultPreferences(context),
                Settings.getPreferences(context, BACKUP_PREFERENCES_SUFFIX), true);

        /*
         * call base implementation which will backup the backup-file
         */
        super.performBackup(oldState, data, newState);
    }

    @Override
    public void restoreEntity(BackupDataInputStream data) {
        /*
         * restore the backup file
         */
        super.restoreEntity(data);
        
        /*
         * read the backup file and populate the preferences
         */
        copyPreferences(Settings.getPreferences(context, BACKUP_PREFERENCES_SUFFIX),
                Settings.getDefaultPreferences(context), false);
    }
}