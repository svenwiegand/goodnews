package com.gettingmobile.android.widget;

import android.view.View;
import android.view.ViewGroup;

public interface ListItemViewType {
    int getId();
    boolean isEnabled();
    View createView(ViewGroup parent);
    void bindView(View view, Object item);
}
