package com.gettingmobile.google.reader.db;

import com.gettingmobile.google.reader.Item;

import java.util.List;

public interface ItemCursor extends EntityCursor<Item> {
    List<? extends IndexEntry> getIndex();

    public interface IndexEntry {
        long getId();
        boolean isGroupHeader();
        boolean isUnread();
    }
}
