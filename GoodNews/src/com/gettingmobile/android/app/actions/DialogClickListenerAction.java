package com.gettingmobile.android.app.actions;

import android.app.Application;
import android.content.DialogInterface;

public class DialogClickListenerAction<T extends Application> implements DialogInterface.OnClickListener {
    private final ActionContext<? extends T> actionContext;
    private final Action<T> action;
    
    public DialogClickListenerAction(ActionContext<? extends T> actionContext, Action<T> action) {
        this.actionContext = actionContext;
        this.action = action;
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        action.onFired(actionContext);
    }
}
