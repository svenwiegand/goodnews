package com.gettingmobile.goodnews.tip;

import android.app.Activity;
import com.gettingmobile.goodnews.Application;

public class StandardFullScreenTip extends AbstractTip implements FullScreenTip {
    private final int additionalCheckBoxTitleId;
    private boolean additionalCheckBoxState;
    
    public StandardFullScreenTip(String id, boolean automatic) {
        this(id, automatic, 0, false);
    }
    
    public StandardFullScreenTip(String id, boolean automatic,
                                 int additionalCheckBoxTitleId, boolean additionalCheckBoxState) {
        super(id, FLAG_FULL_SCREEN | (automatic ? FLAG_AUTOMATIC : 0));
        this.additionalCheckBoxTitleId = additionalCheckBoxTitleId;
        this.additionalCheckBoxState = additionalCheckBoxState;
    }

    @Override
    public boolean canShowNow(Activity activity) {
        return true;
    }

    @Override
    public boolean forceAutomaticTipCheckBox() {
        return false;
    }

    @Override
    public int getAdditionalCheckBoxTitleId() {
        return additionalCheckBoxTitleId;
    }

    @Override
    public boolean getAdditionalCheckBoxState() {
        return additionalCheckBoxState;
    }

    @Override
    public void onAdditionalCheckBoxStateChanged(Application app, boolean checked) {
        this.additionalCheckBoxState = checked;
    }
}
