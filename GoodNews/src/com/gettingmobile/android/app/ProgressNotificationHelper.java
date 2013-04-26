package com.gettingmobile.android.app;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import com.gettingmobile.android.util.ApiLevel;

public abstract class ProgressNotificationHelper {
    public static ProgressNotificationHelper create(Context context, int iconId, int titleId, boolean showTickerText) {
        if (ApiLevel.isAtLeast(14)) {
            return new ProgressNotificationHelper14(context, iconId, titleId, showTickerText);
        } else {
            return new ProgressNotificationHelperBase(context, iconId, titleId, showTickerText);
        }
    }
    
    public static ProgressNotificationHelper create(Context context, int iconId, int titleId) {
        return create(context, iconId, titleId, false);
    }

    public abstract void setContentIntent(PendingIntent intent);

    public void resetProgress() {
        setProgress(0, 0);
    }
    
    public abstract void setProgress(int max, int progress);
    public abstract Notification getNotification();
}
