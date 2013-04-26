package com.gettingmobile.goodnews.tip;

import android.app.Activity;
import android.view.View;

public final class VisualViewIdTip extends AbstractVisualTip {
    private final int viewId;

    public VisualViewIdTip(String id, int textId, int viewId) {
        super(id, textId);
        this.viewId = viewId;
    }

    public VisualViewIdTip(String id, int textId, int textViewGravity, int viewId) {
        super(id, textId, textViewGravity);
        this.viewId = viewId;
    }

    @Override
    public View findView(Activity activity) {
        return activity.findViewById(viewId);
    }
}
