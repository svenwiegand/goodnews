package com.gettingmobile.android.app.actions;

import android.app.Application;

public abstract class AbstractBusyAction<T extends Application> extends AbstractAction<T> implements BusyAction<T> {
    private BusyActionListener listener = null;

    @Override
    public void setListener(BusyActionListener listener) {
        this.listener = listener;
    }

    protected void fireOnStarted() {
        if (listener != null) {
            listener.onActionStarted(this);
        }
    }

    protected void fireOnStopped() {
        if (listener != null) {
            listener.onActionStopped(this);
        }
    }
}
