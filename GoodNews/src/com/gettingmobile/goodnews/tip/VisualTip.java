package com.gettingmobile.goodnews.tip;

import android.app.Activity;
import android.view.View;

public interface VisualTip extends Tip {
    /**
     * Returns the id of the text to be displayed.
     * @return the id of the text to be displayed.
     */
    int getTextResourceId();

    /**
     * Returns the view to be marked by the tip.
     * @param activity the activity that contains the view to be marked.
     * @return the view to be marked by the tip or {@code null} to skip this tip.
     */
    View findView(Activity activity);
    
    int getTextViewGravity();
}
