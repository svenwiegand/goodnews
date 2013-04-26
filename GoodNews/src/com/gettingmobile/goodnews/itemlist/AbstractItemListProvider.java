package com.gettingmobile.goodnews.itemlist;

import android.database.sqlite.SQLiteDatabase;
import com.gettingmobile.goodnews.Application;
import com.gettingmobile.google.reader.ElementId;
import com.gettingmobile.google.reader.db.ItemCursor;
import com.gettingmobile.google.reader.db.ItemDatabaseAdapter;
import com.gettingmobile.google.reader.db.SortOrder;

abstract class AbstractItemListProvider implements ItemListProvider {
    protected static final ItemDatabaseAdapter ITEM_ADAPTER = new ItemDatabaseAdapter();
    protected final Application app;
    protected final ElementId listId;
    protected final String title;

    protected AbstractItemListProvider(Application app, ElementId listId, String title) {
        this.app = app;
        this.listId = listId;
        this.title = title;
    }

    protected ItemCursor readItemsByKeys(SQLiteDatabase db, long[] itemKeys, boolean groupByFeeds, SortOrder sortOrder) {
        return ITEM_ADAPTER.cursorByKeys(db, itemKeys, groupByFeeds, sortOrder);
    }

    protected abstract ItemCursor readItems(SQLiteDatabase db, boolean groupByFeeds, boolean hideRead, SortOrder sortOrder);

    @Override
    public ItemCursor readListItems(SQLiteDatabase db, boolean groupByFeeds, boolean hideRead, SortOrder sortOrder) {
        return readItems(db, groupByFeeds, hideRead, sortOrder);
    }

    @Override
    public ItemCursor readListItemsByKeys(SQLiteDatabase db, long[] itemKeys, boolean groupByFeeds, SortOrder sortOrder) {
        return readItemsByKeys(db, itemKeys, groupByFeeds, sortOrder);
    }
}
