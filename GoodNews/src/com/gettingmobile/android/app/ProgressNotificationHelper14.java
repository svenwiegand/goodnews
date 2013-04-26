package com.gettingmobile.android.app;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;

final class ProgressNotificationHelper14 extends ProgressNotificationHelper {
    private final Notification.Builder builder;

    protected ProgressNotificationHelper14(Context context, int iconId, int titleId, boolean showTickerText) {
        final CharSequence title = context.getText(titleId);

        builder = new Notification.Builder(context).
                setOngoing(true).
                setOnlyAlertOnce(true).
                setSmallIcon(iconId).
                setContentTitle(title);
        if (showTickerText) {
            builder.setTicker(title);
        }

        resetProgress();
    }

    @Override
    public void setContentIntent(PendingIntent intent) {
        builder.setContentIntent(intent);
    }

    @Override
    public void setProgress(int max, int progress) {
        builder.setProgress(max, progress, max == 0);
    }

    @Override
    public Notification getNotification() {
        return builder.getNotification();
    }
}
