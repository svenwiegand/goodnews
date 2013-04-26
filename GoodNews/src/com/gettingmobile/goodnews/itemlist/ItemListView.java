package com.gettingmobile.goodnews.itemlist;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListView;
import com.gettingmobile.google.reader.Item;

final class ItemListView extends ListView {
    public ItemListView(Context context) {
        super(context);
    }

    public ItemListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ItemListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public View getViewByItemKey(long itemKey) {
        final int firstVisiblePosition = getFirstVisiblePosition();
        final int lastVisiblePosition = getLastVisiblePosition();
        for (int pos = firstVisiblePosition; pos <= lastVisiblePosition; ++pos) {
            final View view = getChildAt(pos - firstVisiblePosition);
            if (view != null) {
                final ItemRowViewHandler rowViewHandler = ItemRowViewHandler.getByView(view);
                if (rowViewHandler != null) {
                    final Item item = rowViewHandler.getItem();
                    if (item != null && item.getKey() == itemKey) {
                        return view;
                    }
                }
            }
        }
        return null;
    }
    
    public ItemRowViewHandler getRowViewHandlerByItemKey(long itemKey) {
        return ItemRowViewHandler.getByView(getViewByItemKey(itemKey));
    }

    public void updateVisibleItems() {
        final int firstVisiblePosition = getFirstVisiblePosition();
        final int lastVisiblePosition = getLastVisiblePosition();
        for (int pos = firstVisiblePosition; pos <= lastVisiblePosition; ++pos) {
            final View view = getChildAt(pos - firstVisiblePosition);
            if (view != null) {
                final ItemRowViewHandler rowViewHandler = ItemRowViewHandler.getByView(view);
                if (rowViewHandler != null) {
                    rowViewHandler.updateView();
                }
            }
        }
    }
}
