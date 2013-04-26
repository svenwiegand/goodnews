package com.gettingmobile.android.content;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

public final class ApplicationContextUtil {
    private static final String LOG_TAG = "goodnews.ApplicationContextUtil";

    public static PackageInfo getPackageInfo(Context context) {
        try {
            final PackageManager pm = context.getPackageManager();
            return pm.getPackageInfo(context.getPackageName(), 0);
        } catch (Exception ex) {
            // this will not happen as the package name is not dynamic but taken from the context
            Log.e(LOG_TAG, "Failed to determine package info", ex);
            return null;
        }
    }

    public static ApplicationInfo getApplicationInfo(Context context) {
        try {
            final PackageManager pm = context.getPackageManager();
            return pm.getApplicationInfo(context.getPackageName(), 0);
        } catch (Exception ex) {
            // this will not happen as the package name is not dynamic but taken from the context
            Log.e(LOG_TAG, "Failed to determine application info", ex);
            return null;
        }
    }

    public static String getApplicationName(Context context) {
        return context.getPackageManager().getApplicationLabel(getApplicationInfo(context)).toString();
    }
}
