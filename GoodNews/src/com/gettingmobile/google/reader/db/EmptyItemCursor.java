package com.gettingmobile.google.reader.db;

import com.gettingmobile.google.reader.Item;

import java.util.ArrayList;
import java.util.List;

public final class EmptyItemCursor extends EmptyEntityCursor<Item> implements ItemCursor {
    @Override
    public List<? extends IndexEntry> getIndex() {
        return new ArrayList<IndexEntry>(0);
    }
}
