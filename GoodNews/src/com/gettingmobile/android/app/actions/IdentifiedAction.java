package com.gettingmobile.android.app.actions;

public class IdentifiedAction<T> {
    public final T key;
    public final Action action;

    public IdentifiedAction(T key, Action action) {
        this.key = key;
        this.action = action;
    }
}
