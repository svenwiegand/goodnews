package com.gettingmobile.goodnews.tip;

import android.app.Activity;
import com.gettingmobile.android.app.actions.AbstractAction;
import com.gettingmobile.android.app.actions.ActionContext;
import com.gettingmobile.goodnews.Application;

public class TipGroupAction extends AbstractAction<Application> {
    private final int tipGroupId;
    private final boolean unshownOnly;

    public TipGroupAction(int tipGroupId, boolean unshownOnly) {
        this.tipGroupId = tipGroupId;
        this.unshownOnly = unshownOnly;
    }

    public static void showTips(final Application app, final Activity activity, final TipManager tipManager,
                                final int tipGroupId, final boolean unshownOnly) {
        if (tipGroupId != TipManager.NO_TIPS) {
            final VisualTipController vtc = new VisualTipController(app, activity, tipManager, tipGroupId, unshownOnly,
                    new VisualTipController.OnDismissListener() {
                        @Override
                        public void onVisualTipsDismissed() {
                            TipDialogHandler.start(app, activity, tipManager, tipGroupId, unshownOnly);
                        }
                    });
            if (unshownOnly) {
                vtc.showDelayed();
            } else {
                vtc.show();
            }
        }
    }

    @Override
    public boolean onFired(final ActionContext<? extends Application> context) {
        showTips(context.getApp(), context.getActivity(), context.getApp().getTipManager(), tipGroupId, unshownOnly);
        return true;
    }
}
