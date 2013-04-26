package com.gettingmobile.android.widget;

import android.view.View;
import android.view.ViewGroup;

public class SimpleListItemViewType implements ListItemViewType {
    private final int id;
    private final boolean enabled;
    private final int layoutId;

    public SimpleListItemViewType(int id, boolean enabled, int layoutId) {
        this.id = id;
        this.enabled = enabled;
        this.layoutId = layoutId;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public View createView(ViewGroup parent) {
        return View.inflate(parent.getContext(), layoutId, null);
    }

    @Override
    public void bindView(View view, Object item) {
        // do nothing by default
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
