package com.gettingmobile.android.app;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import fixes.android.support.v4.app.NotificationCompat;

public final class SimpleNotificationBuilder extends NotificationCompat.Builder {
    private final Context context;
    
    public static PendingIntent createPendingIntent(Context context, Intent intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

    }

    public static SimpleNotificationBuilder create(Context context, int iconId, int titleId, boolean showTickerText) {
        final CharSequence titleText = context.getText(titleId);

        final SimpleNotificationBuilder b = new SimpleNotificationBuilder(context);
        b.setSmallIcon(iconId);
        if (showTickerText) {
            b.setTicker(titleText);
        }
        b.setContentTitle(titleText);
        b.setWhen(System.currentTimeMillis());
        b.setAutoCancel(true);
        return b;
    }

    public static SimpleNotificationBuilder create(Context context, int iconId, int titleId) {
        return create(context, iconId, titleId, false);
    }

    private SimpleNotificationBuilder(Context context) {
        super(context);
        this.context = context;
    }
    
    public SimpleNotificationBuilder setContentText(int textId) {
        setContentText(context.getText(textId));
        return this;
    }

    public SimpleNotificationBuilder setContentTitle(int titleId) {
        setContentTitle(context.getText(titleId));
        return this;
    }
    
    public SimpleNotificationBuilder setContentIntent(Intent intent) {
        setContentIntent(createPendingIntent(context, intent));
        return this;
    }

    public SimpleNotificationBuilder addAction(int icon, int titleId, PendingIntent intent) {
        addAction(icon, context.getText(titleId), intent);
        return this;
    }
}
