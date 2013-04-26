package com.gettingmobile.goodnews.tip;

import android.app.Activity;
import com.gettingmobile.goodnews.Application;

public interface FullScreenTip extends Tip {
    boolean canShowNow(Activity activity);
    boolean forceAutomaticTipCheckBox();

    /**
     * Allows a checkbox to be shown below the tip with the text referenced by the returned string identifier.
     * @return a string identifier to be used as label for a checkbox to be shown below the tip or 0 if no checkbox
     *  should be shown.
     */
    int getAdditionalCheckBoxTitleId();

    /**
     * The state to be shown for the additional check box. Will only be called if {@link #getAdditionalCheckBoxTitleId()}
     * contained a value different to zero.
     * @return whether to check the check box initially or not.
     */
    boolean getAdditionalCheckBoxState();

    /**
     * Called if the state of the additional check box changed.
     * @param app the application object.
     * @param checked the new state of the check box.
     */
    void onAdditionalCheckBoxStateChanged(Application app, boolean checked);
}
