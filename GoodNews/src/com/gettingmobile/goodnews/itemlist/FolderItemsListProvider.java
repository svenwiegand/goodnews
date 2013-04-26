package com.gettingmobile.goodnews.itemlist;

import android.database.sqlite.SQLiteDatabase;
import com.gettingmobile.goodnews.Application;
import com.gettingmobile.google.reader.ElementId;
import com.gettingmobile.google.reader.db.ItemCursor;
import com.gettingmobile.google.reader.db.SortOrder;

final class FolderItemsListProvider extends TagItemsListProvider {
    public FolderItemsListProvider(Application app, ElementId listId, long listKey, String title) {
        super(app, listId, listKey, title);
    }

    @Override
    protected ItemCursor readItemsByTagKey(SQLiteDatabase db, long tagKey, boolean groupByFeeds, boolean hideRead, SortOrder sortOrder) {
        final boolean feedDragAndDropOrder = app.getSettings().sortByDragAndDropOrder();
        return ITEM_ADAPTER.cursorByFolder(db, tagKey, groupByFeeds, feedDragAndDropOrder, hideRead, sortOrder);
    }

    @Override
    protected ItemCursor readItemsByKeys(SQLiteDatabase db, long[] itemKeys, boolean groupByFeeds, SortOrder sortOrder) {
        final boolean feedDragAndDropOrder = app.getSettings().sortByDragAndDropOrder();
        return ITEM_ADAPTER.cursorByKeysForFolder(db, itemKeys, getListKey(db), groupByFeeds, feedDragAndDropOrder, sortOrder);
    }
}
