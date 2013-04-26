package com.gettingmobile.android.app;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class SimpleNotification extends Notification {
    private final CharSequence title;
    private CharSequence contentText = null;

    public static PendingIntent createPendingIntent(Context context, Intent intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

    }

    public SimpleNotification(Context context, int iconId, int titleId, boolean showTickerText) {
        super(iconId, showTickerText ? context.getText(titleId) : null, System.currentTimeMillis());
        title = context.getText(titleId);
        addFlags(FLAG_AUTO_CANCEL);
    }

    public SimpleNotification(Context context, int iconId, int titleId) {
        this(context, iconId, titleId, false);
    }

    public SimpleNotification updateWhen() {
        when = System.currentTimeMillis();
        return this;
    }

    public SimpleNotification updateLatestEventInfo(Context context) {
        setLatestEventInfo(context, title, contentText, contentIntent);
        return this;
    }

    @Override
    public void setLatestEventInfo(Context context, CharSequence contentTitle, CharSequence contentText,
                                   PendingIntent contentIntent) {
        updateWhen();
        this.contentText = contentText;
        super.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
    }

    public SimpleNotification setLatestEventInfo(Context context, CharSequence contentText, PendingIntent intent) {
        setLatestEventInfo(context, title, contentText, intent);
        return this;
    }

    public SimpleNotification setLatestEventInfo(Context context, int contentTextId, PendingIntent intent) {
        return setLatestEventInfo(context, context.getText(contentTextId), intent);
    }

    public SimpleNotification setContentText(Context context, CharSequence contentText) {
        return setLatestEventInfo(context, contentText, contentIntent);
    }

    public SimpleNotification setContentText(Context context, int contentTextId) {
        return setContentText(context, context.getText(contentTextId));
    }

    public SimpleNotification setContentIntent(PendingIntent contentIntent) {
        this.contentIntent = contentIntent;
        return this;
    }

    public SimpleNotification setContentIntent(Context context, Intent contentIntent) {
        return setContentIntent(createPendingIntent(context, contentIntent));
    }

    public SimpleNotification setFlags(int flags) {
        this.flags = flags;
        return this;
    }

    public SimpleNotification addFlags(int flags) {
        this.flags |= flags;
        return this;
    }

    public SimpleNotification removeFlags(int flags) {
        this.flags &= ~flags;
        return this;
    }
}
