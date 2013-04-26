package com.gettingmobile.android.app.actions;

import android.app.Application;

public abstract class AbstractAction<T extends Application> implements Action<T> {
    @Override
    public int getState(ActionContext<? extends T> context) {
        return ENABLED;
    }
}
