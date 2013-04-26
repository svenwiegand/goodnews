package com.gettingmobile.android.widget;

public class ListItem {
    private final int itemViewType;
    private final Object item;

    public ListItem(int itemViewType, Object data) {
        this.itemViewType = itemViewType;
        this.item = data;
    }

    public ListItem(int itemViewType) {
        this(itemViewType, null);
    }

    public int getViewType() {
        return itemViewType;
    }

    public Object getItem() {
        return item;
    }

    @Override
    public String toString() {
        return "ListItem{" +
                "itemViewType=" + itemViewType +
                ", item=" + item +
                '}';
    }
}
