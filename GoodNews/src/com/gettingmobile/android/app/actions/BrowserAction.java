package com.gettingmobile.android.app.actions;

import android.app.Application;
import android.content.Intent;
import android.net.Uri;

public class BrowserAction extends AbstractAction<Application>{
    private final String url;
    
    public BrowserAction(String url) {
        this.url = url;
    }
    
    @Override
    public boolean onFired(ActionContext<? extends Application> context) {
        context.getActivity().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        return true;
    }
}
