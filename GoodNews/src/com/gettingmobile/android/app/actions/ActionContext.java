package com.gettingmobile.android.app.actions;

import android.app.Activity;
import android.app.Application;

public interface ActionContext<T extends Application> {
    T getApp();
    Activity getActivity();
    void showWaitDialog();
    void dismissWaitDialog();
}
