package com.gettingmobile.goodnews.sync;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import com.gettingmobile.android.app.ServiceBinder;
import com.gettingmobile.goodnews.Application;
import com.gettingmobile.goodnews.settings.Settings;
import com.gettingmobile.google.reader.db.DatabaseHelper;
import com.gettingmobile.google.reader.sync.ItemFileCleanup;

import java.util.Calendar;

public final class CleanupService extends Service {
    private static final String LOG_TAG = "goodnews.CleanupService";
    private final ServiceBinder<CleanupService> binder = new ServiceBinder<CleanupService>(this);
    private boolean running = false;

    /*
     * operations
     */

    public static void start(Context context) {
        final Intent intent = new Intent(context, CleanupService.class);
        context.startService(intent);
    }

    /*
     * lifecycle management
     */

    @Override
    public void onStart(Intent intent, int startId) {
        onStartCommand(intent, 0, startId);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(LOG_TAG, "starting cleanup service");

        if (running) {
            Log.d(LOG_TAG, "already running");
            return 0;
        }

        if (!needsRun()) {
            Log.d(LOG_TAG, "already ran today");
            return 0;
        }

        start();
        return 0;
    }

    /*
     * implementation
     */

    private boolean needsRun() {
        final Calendar mostRecentCleanupTimestamp = getApp().getSettings().getMostRecentCleanupTimestamp();
        if (mostRecentCleanupTimestamp == null)
            return true;

        final Calendar now = Calendar.getInstance();
        return now.get(Calendar.YEAR) > mostRecentCleanupTimestamp.get(Calendar.YEAR) ||
                now.get(Calendar.DAY_OF_YEAR) > mostRecentCleanupTimestamp.get(Calendar.DAY_OF_YEAR);
    }

    private void cleanup() {
        final DatabaseHelper dbHelper = getApp().getDbHelper();
        final Settings settings = getApp().getSettings();

        Log.d(LOG_TAG, "Cleaning up content files");
        new ItemFileCleanup(dbHelper.getDatabase(), settings.getContentStorageProvider()).cleanup();

        Log.d(LOG_TAG, "Cleanup done");
    }

    private PowerManager.WakeLock acquireWakeLock() {
        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        final PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
        wakeLock.acquire();
        return wakeLock;
    }

    private void start() {
        running = true;
        //noinspection unchecked
        new AsyncTask<Boolean, Integer, Boolean>() {
            @Override
            protected Boolean doInBackground(Boolean... booleans) {
                final PowerManager.WakeLock wakeLock = acquireWakeLock();
                try {
                    /*
                     * set lower thread priority to give more time to the foreground thread
                     */
                    Thread.currentThread().setPriority(Thread.NORM_PRIORITY - 1);
                    cleanup();
                    getApp().getSettings().setMostRecentCleanupTimestamp();
                } finally {
                    wakeLock.release();
                }
                return Boolean.TRUE;
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                super.onPostExecute(aBoolean);
                stop();
            }
        }.execute();
    }

    private void stop() {
        running = false;
        stopSelf();
    }

    /*
     * helper
     */

    protected Application getApp() {
        return (Application) getApplication();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
}
