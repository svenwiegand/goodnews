package com.gettingmobile.android.app;

import android.app.Notification;
import com.gettingmobile.android.util.ApiLevel;
import roboguice.service.RoboService;

public abstract class ForegroundService extends RoboService {
    protected abstract int getForegroundId();

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void startForeground(Notification notification) {
        startForeground(getForegroundId(), notification);
    }

    public void stopForeground() {
        stopForeground(true);
    }
}
