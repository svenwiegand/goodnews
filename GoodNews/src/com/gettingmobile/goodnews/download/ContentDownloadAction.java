package com.gettingmobile.goodnews.download;

import android.app.Application;
import com.gettingmobile.android.app.actions.AbstractAction;
import com.gettingmobile.android.app.actions.ActionContext;

public class ContentDownloadAction extends AbstractAction<Application> {
    @Override
    public boolean onFired(ActionContext<? extends Application> applicationActionContext) {
        ContentDownloadService.start(applicationActionContext.getApp(), true);
        return true;
    }
}
