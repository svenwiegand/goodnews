package com.gettingmobile.android.app.actions;

import android.app.Application;

public interface BusyAction<T extends Application> extends Action<T> {
    int BUSY = 3;

    void setListener(BusyActionListener listener);
    boolean onStop(ActionContext<? extends T> context);
}
