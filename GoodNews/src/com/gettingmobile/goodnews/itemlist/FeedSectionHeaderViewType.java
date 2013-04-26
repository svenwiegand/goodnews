package com.gettingmobile.goodnews.itemlist;

import com.gettingmobile.android.widget.DefaultListSectionHeaderViewType;
import com.gettingmobile.android.widget.ListItemCursorAdapter;

import java.util.Map;

final class FeedSectionHeaderViewType extends DefaultListSectionHeaderViewType {
    public FeedSectionHeaderViewType() {
        super(ListItemCursorAdapter.VIEW_TYPE_HEADER, false);
    }
}
