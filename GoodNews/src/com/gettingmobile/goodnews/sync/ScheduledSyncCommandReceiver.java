package com.gettingmobile.goodnews.sync;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public final class ScheduledSyncCommandReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (SyncService.ACTION_SYNC_PUSH.equals(intent.getAction())) {
            context.startService(SyncService.createStartIntent(context, SyncService.ACTION_SYNC_PUSH, false));
        } else if (SyncService.ACTION_SYNC_FULL.equals(intent.getAction())) {
            context.startService(SyncService.createStartIntent(context, SyncService.ACTION_SYNC_FULL, false));
        }
    }
}
