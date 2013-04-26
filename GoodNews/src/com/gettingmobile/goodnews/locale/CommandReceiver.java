package com.gettingmobile.goodnews.locale;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.gettingmobile.goodnews.sync.SyncService;

public final class CommandReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = "goodnews.CommandReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (com.twofortyfouram.locale.Intent.ACTION_FIRE_SETTING.equals(intent.getAction())) {
            Log.d(LOG_TAG, "Received locale fire setting intent");

            final Bundle settings = intent.getBundleExtra(com.twofortyfouram.locale.Intent.EXTRA_BUNDLE);
            if (settings == null) {
                Log.w(LOG_TAG, "Locale: received NULL bundle");
                return;
            }

            final int action = settings.getInt(Constants.INTENT_EXTRA_ACTION);
            final int syncType = settings.getInt(Constants.INTENT_EXTRA_SYNC_TYPE);

            switch (action) {
                case Constants.ACTION_SYNC:
                    startSync(context, syncType);
                    break;
                default:
                    Log.e(LOG_TAG, "Unknown action " + action);
            }
        }
    }

    protected void startSync(Context context, int syncType) {
        final String action;
        switch (syncType) {
            case Constants.SYNC_TYPE_FULL:
                action = SyncService.ACTION_SYNC_FULL;
                break;
            case Constants.SYNC_TYPE_PUSH:
                action = SyncService.ACTION_SYNC_PUSH;
                break;
            default:
                Log.e(LOG_TAG, "Unknown sync type " + syncType);
                return;
        }
        context.startService(SyncService.createStartIntent(context, action, true));
    }
}
