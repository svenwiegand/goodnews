package com.gettingmobile.goodnews.sync;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import com.gettingmobile.google.reader.sync.SyncCallback;
import com.gettingmobile.google.reader.sync.SyncContext;
import com.gettingmobile.google.reader.sync.Synchronizer;

final class SyncThread extends HandlerThread {
    private final Object startEvent = new Object();
    private final Synchronizer synchronizer;
    private final SyncCallback callback;
    private SyncThreadHandler syncThreadHandler = null;

    public SyncThread(SyncContext context, SyncCallback callback) {
        super(SyncThread.class.getName());
        this.synchronizer = new Synchronizer(context);
        this.callback = callback;
    }

    public Synchronizer getSynchronizer() {
        return synchronizer;
    }

    /*
     * life cycle management
     */

    @Override
    public void start() {
        synchronized (startEvent) {
            super.start();
            try {
                startEvent.wait();
            } catch (InterruptedException ex) {
                // ignore
            }
        }
    }

    @Override
    protected void onLooperPrepared() {
        syncThreadHandler = new SyncThreadHandler();
        synchronized (startEvent) {
            startEvent.notifyAll();
        }

        /*
         * decrease priority to provide more time to the UI thread.
         */
        setPriority(NORM_PRIORITY - 1);
        super.onLooperPrepared();
    }

    public void shutdown() {
        final Looper l = getLooper();
        if (l != null) {
            l.quit();
        }
    }

    /*
     * public interface
     */

    public void startFullSync() {
        syncThreadHandler.sendStartFullSync();
    }

    public void startPushSync() {
        syncThreadHandler.sendStartPushSync();
    }

    /*
     * logic
     */

    private void doFullSync() {
        synchronizer.fullSync(callback);
    }

    private void doPushSync() {
        synchronizer.pushSync(callback);
    }

    /*
    * inner classes
    */

    final class SyncThreadHandler extends Handler {
        private static final int MSG_FULL_SYNC = 1;
        private static final int MSG_PUSH_SYNC = 2;

        public void sendStartFullSync() {
            sendMessage(obtainMessage(MSG_FULL_SYNC));
        }

        public void sendStartPushSync() {
            sendMessage(obtainMessage(MSG_PUSH_SYNC));
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_FULL_SYNC:
                doFullSync();
                break;
            case MSG_PUSH_SYNC:
                doPushSync();
                break;
            }
            super.handleMessage(msg);
        }
    }
}
