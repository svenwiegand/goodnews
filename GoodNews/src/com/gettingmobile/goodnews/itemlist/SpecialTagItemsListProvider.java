package com.gettingmobile.goodnews.itemlist;

import com.gettingmobile.goodnews.Application;
import com.gettingmobile.google.reader.ElementId;

final class SpecialTagItemsListProvider extends LabelItemsListProvider {
    public SpecialTagItemsListProvider(Application app, ElementId listId, long listKey, String title) {
        super(app, listId, listKey, title);
    }

    @Override
    public String getKeywords() {
        return null;
    }
}
