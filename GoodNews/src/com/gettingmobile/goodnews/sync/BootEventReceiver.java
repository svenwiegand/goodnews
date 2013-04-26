package com.gettingmobile.goodnews.sync;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootEventReceiver extends BroadcastReceiver {
    public static final String LOG_TAG = "goodnews.BootEventReceiver";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(LOG_TAG, "system finished boot - scheduling syncs if applicable");
        context.startService(SyncService.createStartIntent(context, SyncService.ACTION_SCHEDULE_SYNCS));
    }
}
