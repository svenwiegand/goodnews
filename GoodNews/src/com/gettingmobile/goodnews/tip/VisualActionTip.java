package com.gettingmobile.goodnews.tip;

import android.app.Activity;
import android.view.Gravity;
import android.view.View;

public final class VisualActionTip extends AbstractVisualTip {
    private final int[] actionIds;

    public VisualActionTip(String id, int textId, int... actionIds) {
        super(id, textId);
        this.actionIds = actionIds;
    }

    @Override
    public View findView(Activity activity) {
        for (int actionId : actionIds) {
            final View view = activity.findViewById(actionId);
            if (view != null && view.getVisibility() == View.VISIBLE) {
                return view;
            }
        }
        return null;
    }
}
