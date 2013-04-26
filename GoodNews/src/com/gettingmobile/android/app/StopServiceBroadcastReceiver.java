package com.gettingmobile.android.app;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class StopServiceBroadcastReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = "goodnews.StopServiceBroadcastReceiver";
    public static final String EXTRA_SERVICE_NAME = IntentConstants.EXTRA_BASE + "SERVICE_NAME";

    public static Intent createIntent(Context context, Class<? extends Service> serviceClass) {
        final Intent intent = new Intent(context, StopServiceBroadcastReceiver.class);
        final Bundle extras = new Bundle(1);
        extras.putParcelable(EXTRA_SERVICE_NAME, new ComponentName(context, serviceClass));
        intent.putExtras(extras);
        return intent;
    }

    public static PendingIntent createPendingIntent(Context context, Class<? extends Service> serviceClass) {
        return PendingIntent.getBroadcast(context, 0, createIntent(context, serviceClass), 0);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(LOG_TAG, "onReceive");
        if (intent == null || intent.getExtras() == null)
            return;

        final ComponentName serviceName = intent.getExtras().getParcelable(EXTRA_SERVICE_NAME);
        if (serviceName != null) {
            Log.i(LOG_TAG, "Stopping service " + serviceName.getClassName());
            context.stopService(new Intent().setComponent(serviceName));
        }
    }
}
