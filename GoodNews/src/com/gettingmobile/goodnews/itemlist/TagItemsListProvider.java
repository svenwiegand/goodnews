package com.gettingmobile.goodnews.itemlist;

import android.database.sqlite.SQLiteDatabase;
import com.gettingmobile.goodnews.Application;
import com.gettingmobile.google.reader.ElementId;
import com.gettingmobile.google.reader.db.ItemCursor;
import com.gettingmobile.google.reader.db.SortOrder;
import com.gettingmobile.google.reader.db.TagDatabaseAdapter;

abstract class TagItemsListProvider extends AbstractItemListProvider {
    private final static TagDatabaseAdapter tagAdapter = new TagDatabaseAdapter();
    private long listKey;

    public TagItemsListProvider(Application app, ElementId listId, long listKey, String title) {
        super(app, listId, title);
        this.listKey = listKey;
    }

    protected long getListKey(SQLiteDatabase db) {
        if (listKey <= 0) {
            listKey = tagAdapter.readKeyById(db, listId);
        }
        return listKey;
    }

    @Override
    public String getKeywords() {
        return title;
    }

    @Override
    public ItemCursor readItems(SQLiteDatabase db, boolean groupByFeeds, boolean hideRead, SortOrder sortOrder) {
        return readItemsByTagKey(db, getListKey(db), groupByFeeds, hideRead, sortOrder);
    }
    
    protected abstract ItemCursor readItemsByTagKey(SQLiteDatabase db, long tagKey, boolean groupByFeeds, boolean hideRead, SortOrder sortOrder);
}
