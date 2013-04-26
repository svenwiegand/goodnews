package com.gettingmobile.goodnews.itemlist;

import android.database.sqlite.SQLiteDatabase;
import com.gettingmobile.goodnews.Application;
import com.gettingmobile.google.reader.ElementId;
import com.gettingmobile.google.reader.db.ItemCursor;
import com.gettingmobile.google.reader.db.SortOrder;

final class FeedItemsListProvider extends AbstractItemListProvider {
    public FeedItemsListProvider(Application app, ElementId listId, String title) {
        super(app, listId, title);
    }

    @Override
    public String getKeywords() {
        return title;
    }

    @Override
    public ItemCursor readItems(SQLiteDatabase db, boolean groupByFeeds, boolean hideRead, SortOrder sortOrder) {
        return ITEM_ADAPTER.cursorByFeed(db, listId, hideRead, sortOrder);
    }
}
