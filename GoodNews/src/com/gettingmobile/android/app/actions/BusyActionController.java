package com.gettingmobile.android.app.actions;

public class BusyActionController<T> extends SimpleActionController<T> {
    public BusyActionController(ActionContext<?> context) {
        super(context);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean fireAction(T key) {
        final Action<?> action = actions.get(key);
        return action != null && action.getState((ActionContext) context) == BusyAction.BUSY ?
                stopAction(key) : super.fireAction(key);
    }

    @SuppressWarnings("unchecked")
    public boolean stopAction(T key) {
        final BusyAction<?> action = (BusyAction<?>) actions.get(key);
        return action != null && action.onStop((ActionContext) context);
    }
}
