package com.gettingmobile.android.app.actions;

import roboguice.RoboGuice;

import java.util.HashMap;
import java.util.Map;

public class SimpleActionController<T> implements ActionController<T> {
    public static final int STATE_UNKNOWN = -1;

    protected final ActionContext<?> context;
    protected final Map<T, Action<?>> actions = new HashMap<T, Action<?>>();

    public SimpleActionController(ActionContext<?> context) {
        this.context = context;
    }

    @Override
    public void registerAction(T key, Action<?> action) {
        RoboGuice.getInjector(context.getApp()).injectMembers(action);
        actions.put(key, action);
    }

    @Override
    public <A extends Action<?>> A registerAction(T key, Class<A> actionClass) {
        final A action = RoboGuice.getInjector(context.getApp()).getInstance(actionClass);
        actions.put(key, action);
        return action;
    }

    @Override
    public void unregisterAction(T key) {
        actions.remove(key);
    }

    @SuppressWarnings("unchecked")
    public int getActionState(T key) {
        final Action<?> action = actions.get(key);
        return action != null ? action.getState((ActionContext) context) : STATE_UNKNOWN;
    }
    
    public Action<?> getAction(T key) {
        return actions.get(key);
    }

    @SuppressWarnings("unchecked")
    public boolean fireAction(T key) {
        final Action<?> action = actions.get(key);
        return action != null && action.getState((ActionContext) context) == Action.ENABLED &&
                action.onFired((ActionContext) context);
    }
}
