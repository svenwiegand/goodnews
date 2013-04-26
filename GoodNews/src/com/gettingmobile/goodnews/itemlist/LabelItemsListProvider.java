package com.gettingmobile.goodnews.itemlist;

import android.database.sqlite.SQLiteDatabase;
import com.gettingmobile.goodnews.Application;
import com.gettingmobile.google.reader.ElementId;
import com.gettingmobile.google.reader.db.ItemCursor;
import com.gettingmobile.google.reader.db.SortOrder;

class LabelItemsListProvider extends TagItemsListProvider {
    public LabelItemsListProvider(Application app, ElementId listId, long listKey, String title) {
        super(app, listId, listKey, title);
    }

    @Override
    protected ItemCursor readItemsByTagKey(SQLiteDatabase db, long tagKey, boolean groupByFeeds, boolean hideRead, SortOrder sortOrder) {
        return ITEM_ADAPTER.cursorByLabel(db, tagKey, groupByFeeds, hideRead, sortOrder);
    }
}
