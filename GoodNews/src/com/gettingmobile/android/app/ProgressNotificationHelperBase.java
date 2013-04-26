package com.gettingmobile.android.app;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.widget.RemoteViews;
import com.gettingmobile.goodnews.R;

final class ProgressNotificationHelperBase extends ProgressNotificationHelper {
    private final Notification notification;

    protected ProgressNotificationHelperBase(Context context, int iconId, int titleId, boolean showTickerText) {
        final CharSequence title = context.getText(titleId);

        final RemoteViews view = new RemoteViews(context.getPackageName(), R.layout.notify_progress);
        view.setImageViewResource(R.id.notify_icon, iconId);
        view.setTextViewText(R.id.notify_title, title);
        
        notification = new Notification(iconId, showTickerText ? title : null, System.currentTimeMillis());
        notification.flags |= Notification.FLAG_ONLY_ALERT_ONCE | Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;
        notification.contentView = view;

        resetProgress();
    }

    @Override
    public void setContentIntent(PendingIntent intent) {
        notification.contentIntent = intent;
    }

    @Override
    public void setProgress(int max, int progress) {
        notification.contentView.setProgressBar(R.id.notify_progress, max, progress, max == 0);
    }

    @Override
    public Notification getNotification() {
        return notification;
    }
}
