package com.gettingmobile.goodnews.changelog;

import android.app.Application;
import com.gettingmobile.android.app.actions.AbstractAction;
import com.gettingmobile.android.app.actions.ActionContext;

public final class ViewChangelogAction extends AbstractAction<Application> {
    @Override
    public boolean onFired(ActionContext<? extends Application> applicationActionContext) {
        ChangelogDialogHandler.start(applicationActionContext.getActivity());
        return true;
    }
}
