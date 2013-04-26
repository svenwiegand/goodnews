package com.gettingmobile.goodnews.itemlist;

import android.database.sqlite.SQLiteDatabase;
import com.gettingmobile.android.widget.ListItem;
import com.gettingmobile.google.reader.Item;
import com.gettingmobile.google.reader.db.EntityCursor;
import com.gettingmobile.google.reader.db.ItemCursor;
import com.gettingmobile.google.reader.db.SortOrder;

import java.util.List;

interface ItemListProvider {
    String getKeywords();
    ItemCursor readListItems(SQLiteDatabase db, boolean groupByFeeds, boolean hideRead, SortOrder sortOrder);
    ItemCursor readListItemsByKeys(SQLiteDatabase db, long[] itemKeys, boolean groupByFeeds, SortOrder sortOrder);
}
