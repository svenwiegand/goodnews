package com.gettingmobile.android.app.actions;

import android.app.Application;

public interface Action<T extends Application> {
    int GONE = 0;
    int DISABLED = 1;
    int ENABLED = 2;

    int getState(ActionContext<? extends T> context);
    boolean onFired(ActionContext<? extends T> context);
}
