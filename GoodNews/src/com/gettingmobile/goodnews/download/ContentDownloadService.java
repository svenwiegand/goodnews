package com.gettingmobile.goodnews.download;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;
import com.gettingmobile.android.app.*;
import com.gettingmobile.goodnews.Application;
import com.gettingmobile.goodnews.R;

public final class ContentDownloadService extends ForegroundService implements ContentDownloadListener {
    private static final String LOG_TAG = "goodnews.ContentDownloadService";
    public static final String EXTRA_TRIGGERED_BY_UI = IntentConstants.EXTRA_BASE + "TRIGGERED_BY_UI";
    private static final int NOTIFICATION_ID = R.string.sync_content_notify_title;
    static boolean running = false;
    private final ServiceBinder<ContentDownloadService> binder = new ServiceBinder<ContentDownloadService>(this);
    private ContentDownloadThread downloadThread = null;
    private ProgressNotificationHelper progressNotificationHelper = null;
    private PowerManager.WakeLock wakeLock = null;
    private boolean triggeredByUI = false;

    /*
     * operations
     */

    public static void start(Context context, boolean triggeredByUi) {
        final Intent intent = new Intent(context, ContentDownloadService.class);
        intent.putExtra(EXTRA_TRIGGERED_BY_UI, triggeredByUi);
        context.startService(intent);
    }

    public static void startAfterTagChangeIfApplicable(Application app) {
        if (app.getSettings().getOfflineStrategy() == OfflineStrategy.READ_LIST) {
            if (ItemDownloader.itemDownloadAdapter.readItemDownloadInfosRequiringDownloadCount(
                        app.getDbHelper().getDatabase(), app.getSettings().getLabelReadListId()) > 0) {
                start(app, false);
            }
        }
    }

    public static boolean isRunning() {
        return running;
    }

    /*
     * lifecycle management
     */

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        onStartCommand(intent, 0, startId);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "onStartCommand");

        if (intent == null) {
            Log.w(LOG_TAG, "intent is null");
            stopSelf();
            return 0;
        }

        if (downloadThread != null) {
            Log.d(LOG_TAG, "already running");
            return 0;
        }

        triggeredByUI = intent.getBooleanExtra(EXTRA_TRIGGERED_BY_UI, false);
        Log.d(LOG_TAG, "triggered by UI: " + triggeredByUI);
        if (!triggeredByUI && getApp().getSettings().offlineDownloadRequiresWifi() && !getApp().isWifiConnected()) {
            Log.d(LOG_TAG, "no wifi available");
            stopSelf();
            return 0;
        }

        Log.d(LOG_TAG, "starting content download");
        acquireWakeLock();
        downloadThread = new ContentDownloadThread(getApp(), this);
        downloadThread.start();

        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "done");
        if (downloadThread != null) {
            downloadThread.shutdown();
            downloadThread = null;
        }
        progressNotificationHelper = null;

        super.onDestroy();
    }

    /*
     * thread listener
     */

    @Override
    public void onDownloadStarted() {
        progressNotificationHelper = ProgressNotificationHelper.create(
                this, R.drawable.notify_download, R.string.sync_content_notify_title);
        progressNotificationHelper.setContentIntent(DialogActivity.createPendingIntent(getApp(), DialogActivity.createIntent(
                this, R.string.sync_content_notify_title, R.string.sync_content_cancel_confirmation,
                R.string.yes, StopServiceBroadcastReceiver.createPendingIntent(this, ContentDownloadService.class),
                R.string.no, null,
                0, null)));
        startForeground(progressNotificationHelper.getNotification());
    }

    @Override
    public void onDownloadProgressUpdate(int progress, int max) {
        if (progressNotificationHelper != null) {
            progressNotificationHelper.setProgress(max, progress);
            getNotificationManager().notify(NOTIFICATION_ID, progressNotificationHelper.getNotification());
        }
    }

    @Override
    public void onDownloadStopped() {
        stopForeground();
        stop();
    }

    @Override
    public void onDownloadSkipped() {
        if (triggeredByUI) {
            Toast.makeText(getApp(), R.string.sync_content_nothing, Toast.LENGTH_LONG).show();
        }
        stop();
    }

    protected void stop() {
        releaseWakeLock();
        downloadThread = null;
        progressNotificationHelper = null;
        stopSelf();
    }

    /*
     * helper
     */

    protected Application getApp() {
        return (Application) getApplication();
    }

    @Override
    protected int getForegroundId() {
        return NOTIFICATION_ID;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    protected void acquireWakeLock() {
        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
        wakeLock.acquire();
        running = true;
    }

    protected void releaseWakeLock() {
        wakeLock.release();
        running = false;
    }

    protected NotificationManager getNotificationManager() {
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }
}
