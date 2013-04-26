package com.gettingmobile.goodnews.home;

import com.gettingmobile.android.widget.ListItem;
import com.gettingmobile.android.widget.ListItemCursorAdapter;
import com.gettingmobile.google.reader.SortedElement;
import com.gettingmobile.google.reader.Tag;

final class HomeListItem extends ListItem {
    protected final long id;

    public HomeListItem(SortedElement element) {
        super(ListItemCursorAdapter.VIEW_TYPE_DEFAULT, element);
        this.id = element.getKey();
    }

    public HomeListItem(int itemViewType, long id) {
        super(itemViewType);
        this.id = id;
    }
}
