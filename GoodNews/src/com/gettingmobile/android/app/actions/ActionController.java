package com.gettingmobile.android.app.actions;

public interface ActionController<T> {
    void registerAction(T key, Action<?> action);
    <A extends Action<?>> A registerAction(T key, Class<A> actionClass);
    void unregisterAction(T key);
}
