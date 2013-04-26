package com.gettingmobile.goodnews.sync;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public final class NetworkConnectionStateChangeReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = "goodnews.NetworkConnectionStateChangeReceiver";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            onConnectivityStateChanged(context);
        } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
            onWifiStateChanged(context, intent);
        }
    }
    
    private void onConnectivityStateChanged(Context context) {
        /*
         * schedule syncs if network is available
         */
        final ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni != null && ni.isConnected()) {
            Log.i(LOG_TAG, "network connection is available");
            scheduleSyncsIfApplicable(context);
        }
    }

    private void onWifiStateChanged(Context context, Intent intent) {
        try {
            final NetworkInfo ni = (NetworkInfo) intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if (ni != null && ni.isConnected()) {
                Log.i(LOG_TAG, "wifi connection is available");
                scheduleSyncsIfApplicable(context);
            }
        } catch (RuntimeException error) {
            /*
             * I've had cases where intent.getParcelableExtra() failed with a
             * "ClassNotFoundException when unmarshalling"
             */
            Log.e(LOG_TAG, "Failed to retrieve NetworkInfo", error);
        }
    }

    private void scheduleSyncsIfApplicable(Context context) {
        context.startService(SyncService.createStartIntent(context, SyncService.ACTION_SCHEDULE_SYNCS));
    }
}
