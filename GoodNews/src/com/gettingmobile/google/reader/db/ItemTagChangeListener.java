package com.gettingmobile.google.reader.db;

import com.gettingmobile.google.reader.ElementId;

public interface ItemTagChangeListener {
    void onItemReadStateChanged(long itemKey, boolean read);
    void onItemTagChanged(long itemKey, ElementId tag, boolean added);
}
