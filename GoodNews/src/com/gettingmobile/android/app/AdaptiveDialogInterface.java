package com.gettingmobile.android.app;

import android.widget.Button;
import com.gettingmobile.goodnews.Application;

public interface AdaptiveDialogInterface {
    Application getApp();
    void setTitle(CharSequence title);
    void setTitle(int titleId);
    void dismiss();
    Button getRightButton();
    Button getLeftButton();
    Button getMiddleButton();
}
