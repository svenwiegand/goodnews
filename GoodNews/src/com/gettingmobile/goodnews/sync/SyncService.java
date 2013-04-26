package com.gettingmobile.goodnews.sync;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import com.gettingmobile.android.app.*;
import com.gettingmobile.goodnews.Application;
import com.gettingmobile.goodnews.R;
import com.gettingmobile.goodnews.account.LoginCallback;
import com.gettingmobile.goodnews.download.ContentDownloadService;
import com.gettingmobile.goodnews.download.OfflineStrategy;
import com.gettingmobile.goodnews.home.HomeActivity;
import com.gettingmobile.google.reader.sync.SyncCallback;
import com.gettingmobile.google.reader.sync.SyncException;
import com.google.inject.Inject;

public final class SyncService extends ForegroundService implements SyncCallback {
    private static final String LOG_TAG = "goodnews.SyncService";

    private static final String ACTION_BASE = "com.gettingmobile.goodnews.action.";
    public static final String ACTION_SCHEDULE_SYNCS = ACTION_BASE + "SCHEDULE_SYNCS";
    public static final String ACTION_SYNC_FULL = ACTION_BASE + "SYNC_FULL";
    public static final String ACTION_SYNC_PUSH = ACTION_BASE + "SYNC_PUSH";
    public static final String INTENT_EXTRA_AUTOMATION = ACTION_BASE + ".AUTOMATION";

    public static final long IMMEDIATE_PUSH_DELAY = 5 * 1000;
    public static final long FAILED_DELAY = 60 * 1000;

    private static final int NOTIFICATION_ID = R.drawable.notify_sync;
    private static final int NOTIFICATION_ERROR = R.drawable.notify_sync_error;
    private static final int NOTIFICATION_PULL_SUCCESS = R.drawable.notify_sync_finished;

    private static final int STATE_IDLE = 0;
    private static final int STATE_PUSH_SYNCING = 1;
    private static final int STATE_FULL_SYNCING = 2;
    private boolean startedByIntent = false;

    @Inject
    private SyncServiceProxy serviceProxy = null;
	private final ServiceBinder<SyncService> binder = new ServiceBinder<SyncService>(this);
	private SyncThread syncThread = null;
    private PowerManager.WakeLock wakeLock = null;
    private int state = STATE_IDLE;
    private ProgressNotificationHelper progressNotificationHelper = null;
    private SimpleNotification syncErrorNotification = null;
    private final OnContinousSyncIntervalSettingChangeListener continousSyncIntervalSettingChangeListener =
            new OnContinousSyncIntervalSettingChangeListener(this);

	/*
	 * public interface 
	 */

    public boolean isIdle() {
        return state == STATE_IDLE;
    }

    public boolean isPushSyncing() {
        return state == STATE_PUSH_SYNCING;
    }

    public boolean isFullSyncing() {
        return state == STATE_FULL_SYNCING;
    }

    public boolean isSyncing() {
        return isFullSyncing() || isPushSyncing();
    }

    public boolean isPushRequired() {
        return syncThread != null && syncThread.getSynchronizer().isPushRequired();
    }

    /**
     * Starts a full sync.
     * @throws IllegalStateException if the service is not in the idle state.
     */
	public void startFullSync() throws IllegalStateException {
        startSync(STATE_FULL_SYNCING, new SyncInvocator() {
            @Override
            protected void doSync() {
                syncThread.startFullSync();
            }
        });
    }

    public boolean scheduleContinousFullSyncIfApplicable() {
        Log.d(LOG_TAG, "Scheduling continous sync");
        return scheduleSync(getApp().getSettings().getNextContinousSyncTimestampInMillis(), ACTION_SYNC_FULL);
    }

    /**
     * Starts a push sync.
     * @throws IllegalStateException if the service is not in the idle state.
     */
	public void startPushSync() throws IllegalStateException {
        startSync(STATE_PUSH_SYNCING, new SyncInvocator() {
            @Override
            protected void doSync() {
                syncThread.startPushSync();
            }
        });
	}

    /**
     * Schedules a push sync if the immediate push option is active and the service is in the idle state or does nothing
     * otherwise.
     */
    public void scheduleImmediatePushSyncIfApplicable() {
        if (isIdle() && getApp().getSettings().getPushImmediately() && isPushRequired()) {
            scheduleImmediatePushSync();
        }
    }

    public boolean scheduleImmediatePushSync() {
        Log.d(LOG_TAG, "Scheduling immediate push sync");
        return scheduleSync(System.currentTimeMillis() + IMMEDIATE_PUSH_DELAY, ACTION_SYNC_PUSH);
    }

    public void postProcessSyncIfRequired() {
        clearSuccess();
    }

    public void cancelSync() {
        if (syncThread != null) {
            syncThread.getSynchronizer().cancel();
        }
    }

    /*
     * intent handling
     */

    public static Intent createStartIntent(Context context, String action, boolean automation) {
        final Intent intent = new Intent(context, SyncService.class);
        intent.setAction(action);
        intent.putExtra(INTENT_EXTRA_AUTOMATION, automation);
        return intent;
    }

    public static Intent createStartIntent(Context context, String action) {
        return createStartIntent(context, action, false);
    }

    protected boolean scheduleSync(long sysTimeInMillis, String action) {
        final AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        final PendingIntent pi = PendingIntent.getBroadcast(this, 0, new Intent(action), 0);
        if (sysTimeInMillis > 0) {
            am.set(AlarmManager.RTC_WAKEUP,
                    Math.max(sysTimeInMillis, getApp().getSettings().getLastFailedSyncTimestampInMillis() + FAILED_DELAY),
                    pi);
            return true;
        } else {
            am.cancel(pi);
            return false;
        }
    }

    @Override
    public void onStart(Intent intent, int startId) {
        onStartCommand(intent, 0, startId);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "received sync request by intent");

        /*
         * is there already a pending intent?
         */
        if (startedByIntent) {
            return 0;
        }

        /*
         * are we able to run?
         */
        if (isSyncing()) {
            stopSelf();
            return 0;
        }

        /*
         * schedule syncs if applicable (e.g. after boot or network connection available again)
         */
        if (ACTION_SCHEDULE_SYNCS.equals(intent.getAction())) {
            if (!scheduleContinousFullSyncIfApplicable()) {
                scheduleImmediatePushSyncIfApplicable();
            }
            stopSelf();
            return 0;
        }

        /*
         * postpone sync if internet isn't available
         */
        final boolean fullSync = ACTION_SYNC_FULL.equals(intent.getAction());
        if (!getApp().isInternetAvailable()) {
            stopSelf();
            return 0;
        }

        /*
         * are we allowed to sync (maybe Wifi is required for continous sync?)
         */
        if (!intent.getBooleanExtra(INTENT_EXTRA_AUTOMATION, false) &&
                getApp().getSettings().requiresWifiForContinousSync() && !getApp().isWifiConnected()) {
            stopSelf();
            return 0;
        }

        /*
         * start
         */
        startedByIntent = true;
        try {
            if (fullSync) {
                Log.d(LOG_TAG, "starting full sync triggered by intent");
                startFullSync();
            } else {
                Log.d(LOG_TAG, "starting push sync triggered by intent");
                if (!isPushRequired()) {
                    stopSelf();
                    return 0;
                }
                startPushSync();
            }
        } catch (Throwable error) {
            Log.e(LOG_TAG, "An error occured while trying to sync triggered by intent.", error);
            startedByIntent = false;
            scheduleSyncIfApplicable(fullSync);
            stopSelf();
            return 0;
        }
        return START_REDELIVER_INTENT;
    }

    protected void scheduleSyncIfApplicable(boolean fullSync) {
        if (fullSync) {
            scheduleContinousFullSyncIfApplicable();
        } else {
            scheduleImmediatePushSyncIfApplicable();
        }
    }

    /*
     * logic
     */

    protected void startSync(int state, SyncInvocator invocator) {
        enterSyncingState(state);
        loginIfRequired(invocator);
    }

    protected void loginIfRequired(LoginCallback callback) {
        if (!getApp().isLoggedIn()) {
            try {
                getApp().getAccountHandler().login(callback);
            } catch (IllegalStateException ex) {
                stopForeground();
                notifyError(R.string.login_credentials_missing);
            }
        } else {
            callback.onLoginFinished(null);
        }
    }

    protected void enterSyncingState(int state) throws IllegalStateException {
        if (this.state != STATE_IDLE)
            throw new IllegalStateException("Syning state can only be entered when in idle state.");

        Log.d(LOG_TAG, "Changing from idle state to state " + state);
        this.state = state;
        startForeground();

        fireOnSyncStarted();
    }

    protected void leaveSyncingState(Throwable error, int skipCount, int errorMsgId) throws IllegalStateException {
        Log.d(LOG_TAG, "Changing from state " + state + " to idle state");
        stopForeground();
        final boolean fullSync = isFullSyncing();
        state = STATE_IDLE;

        if (error != null) {
            Log.e(LOG_TAG, "Sync failed!", error);
            notifyError(errorMsgId);
        }
        if (skipCount > 0) {
            Log.e(LOG_TAG, "Skipped " + skipCount + " item(s) during sync!");
        }

        fireOnSyncFinished(fullSync, error);

        /*
         * reschedule full sync if applicable
         */
        if (fullSync) {
            scheduleContinousFullSyncIfApplicable();

            /*
             * trigger content download if applicable
             */
            final OfflineStrategy offlineStrategy = getApp().getSettings().getOfflineStrategy();
            if (error == null &&
                    (offlineStrategy == OfflineStrategy.SYNC || offlineStrategy == OfflineStrategy.READ_LIST)) {
                ContentDownloadService.start(getApp(), false);
            }
        }

        /*
         * handle service stop
         */
        if (startedByIntent) {
            startedByIntent = false;

            Log.d(LOG_TAG, "Stopping sync triggered by intent");
            stopSelf();
        } else if (error != null) {
            scheduleImmediatePushSyncIfApplicable();
        }
    }

    /*
     * sync callback
     */

    @Override
    public void onProgressUpdate(int progress, int max) {
        Log.d(LOG_TAG, "Progress update: " + progress + "/" + max);
        progressNotificationHelper.setProgress(max, progress);
        getNotificationManager().notify(NOTIFICATION_ID, progressNotificationHelper.getNotification());
    }

    @Override
    public void onSyncFinished(SyncException error, int unreadCount, int newUnreadCount, int skipCount) {
        final int msgId;
        if (error != null) {
            switch (error.getErrorCode()) {
                case CONNECTION:
                    msgId = R.string.sync_failed_connection;
                    break;
                case STORAGE:
                    msgId = R.string.sync_failed_storage;
                    break;
                case DEVICE_STORAGE_LOW:
                    msgId = R.string.sync_failed_device_storage_low;
                    break;
                case CANCELLED:
                    msgId = 0;
                    error = null;
                    break;
                default:
                    msgId = R.string.sync_failed_error;
            }
        } else {
            msgId = 0;
            if (isFullSyncing()) {
                notifySyncEnd(unreadCount, newUnreadCount);
            }
        }
        leaveSyncingState(error, skipCount, msgId);
    }

    /*
     * listener handling
     */

    protected void fireOnSyncStarted() {
        for (SyncListener l : serviceProxy.getListeners()) {
            l.onSyncStarted();
        }
    }

    protected void fireOnSyncFinished(boolean fullSync, Throwable error) {
        for (SyncListener l : serviceProxy.getListeners()) {
            l.onSyncFinished(fullSync, error);
        }
    }

    /*
      * helpers
      */
	
	protected Application getApp() {
		return (Application) getApplication();
	}

    protected NotificationManager getNotificationManager() {
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    protected PowerManager.WakeLock acquireWakeLock() {
        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        final PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
        wakeLock.acquire();
        return wakeLock;
    }

    protected Intent createHomeActivityIntent() {
        return new Intent(this, HomeActivity.class);
    }

    protected void startForeground() {
        wakeLock = acquireWakeLock();
        clearNotifications();

        progressNotificationHelper.resetProgress();
        progressNotificationHelper.setContentIntent(PendingIntent.getActivity(
                getApp(), 0, createHomeActivityIntent(), PendingIntent.FLAG_UPDATE_CURRENT));
        super.startForeground(progressNotificationHelper.getNotification());
    }

    public void stopForeground() {
        wakeLock.release();
        super.stopForeground();
    }

    protected void notifyError(int msgId) {
        syncErrorNotification.setContentText(this, msgId);
        getNotificationManager().notify(NOTIFICATION_ERROR, syncErrorNotification);
    }

    protected void clearError() {
        getNotificationManager().cancel(NOTIFICATION_ERROR);
    }
    
    private Notification createSyncSucceededNotification(int unreadCount) {
        final String msg;
        if (unreadCount > 1) {
            msg = String.format(getString(R.string.sync_notify_unread_items_msg), unreadCount);
        } else {
            msg = getString(R.string.sync_notify_unread_item_msg);
        }
        return SimpleNotificationBuilder.create(
                this, R.drawable.notify_sync_finished, R.string.sync_notify_unread_items_title).
                setContentIntent(createHomeActivityIntent()).
                setContentText(msg).
                setNumber(unreadCount).
                getNotification();
    }

    protected void notifySyncEnd(int unreadCount, int newUnreadCount) {
        if (getApp().getSettings().shouldNotifyOnNewUnreadItems()) {
            if (newUnreadCount > 0) {
                getNotificationManager().notify(NOTIFICATION_PULL_SUCCESS, createSyncSucceededNotification(unreadCount));
            } else {
                getNotificationManager().cancel(NOTIFICATION_PULL_SUCCESS);
            }
        }
    }

    protected void clearSuccess() {
        getNotificationManager().cancel(NOTIFICATION_PULL_SUCCESS);
    }

    protected void clearNotifications() {
        clearError();
        clearSuccess();
    }

	/*
	 * life cycle management
	 */

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

    @Override
    protected int getForegroundId() {
        return NOTIFICATION_ID;
    }

    @Override
	public void onCreate() {
		super.onCreate();
        Log.d(LOG_TAG, "Creating sync service");
		syncThread = new SyncThread(getApp(), this);
		syncThread.start();

        /*
         * prepare notification
         */
        progressNotificationHelper = ProgressNotificationHelper.create(
                this, R.drawable.notify_sync, R.string.sync_notify_title);
        syncErrorNotification =
                new SimpleNotification(this, R.drawable.notify_sync_error, R.string.sync_notify_failed_title, true).
                        setContentIntent(this, createHomeActivityIntent());

        /*
         * handle continous syncing
         */
        getApp().getSettings().registerChangeListener("sync_continous", continousSyncIntervalSettingChangeListener);
        scheduleImmediatePushSyncIfApplicable();
        scheduleContinousFullSyncIfApplicable();
	}

	@Override
	public void onDestroy() {
        Log.d(LOG_TAG, "Destroying sync service");
        getApp().getSettings().unregisterChangeListener("sync_continous", continousSyncIntervalSettingChangeListener);
		syncThread.shutdown();
		super.onDestroy();
	}

	/*
	 * inner classes
	 */

    abstract class SyncInvocator implements LoginCallback {
        @Override
        public final void onLoginStarted() {
            // not interested
        }

        @Override
        public final void onLoginFinished(Throwable error) {
            if (error != null) {
                leaveSyncingState(error, 0, R.string.login_failed);
            } else {
                doSync();
            }
        }

        protected abstract void doSync();
    }
}
