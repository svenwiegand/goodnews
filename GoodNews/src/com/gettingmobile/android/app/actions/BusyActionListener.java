package com.gettingmobile.android.app.actions;

public interface BusyActionListener {
    void onActionStarted(BusyAction<?> action);
    void onActionStopped(BusyAction<?> action);
}
