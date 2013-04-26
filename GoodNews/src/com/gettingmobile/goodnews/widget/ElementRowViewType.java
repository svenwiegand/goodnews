package com.gettingmobile.goodnews.widget;

import com.gettingmobile.android.widget.ListItemCursorAdapter;
import com.gettingmobile.android.widget.SimpleListItemViewType;

public class ElementRowViewType extends SimpleListItemViewType {
    public ElementRowViewType(int layoutId) {
        super(ListItemCursorAdapter.VIEW_TYPE_DEFAULT, true, layoutId);
    }
}
