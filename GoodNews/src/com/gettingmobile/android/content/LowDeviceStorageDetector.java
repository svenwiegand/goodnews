package com.gettingmobile.android.content;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public final class LowDeviceStorageDetector {
    public static final IntentFilter LOW_DEVICE_STORAGE_INTENT_FILTER =
            new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);

    public static boolean isDeviceStorageLow(Context context) {
        final Intent i = context.registerReceiver(null, LOW_DEVICE_STORAGE_INTENT_FILTER);
        return i != null && i.getAction().equals(Intent.ACTION_DEVICE_STORAGE_LOW);
    }
}
