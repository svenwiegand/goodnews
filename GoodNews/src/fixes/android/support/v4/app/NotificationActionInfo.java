package fixes.android.support.v4.app;

import android.app.PendingIntent;

class NotificationActionInfo {
    public final int icon;
    public final CharSequence title;
    public final PendingIntent intent;

    public NotificationActionInfo(int icon, CharSequence title, PendingIntent intent) {
        this.icon = icon;
        this.title = title;
        this.intent = intent;
    }
}
