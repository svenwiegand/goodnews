package com.gettingmobile.goodnews.itemlist;

import android.content.Context;
import com.gettingmobile.android.widget.Entity2ListItemCursor;
import com.gettingmobile.android.widget.ListItem;
import com.gettingmobile.android.widget.ListItemCursorAdapter;
import com.gettingmobile.google.reader.ElementId;
import com.gettingmobile.google.reader.Item;
import com.gettingmobile.google.reader.db.EntityCursor;
import com.gettingmobile.google.reader.db.ItemCursor;
import com.gettingmobile.google.reader.db.ItemTagChangeDatabaseAdapter;
import com.gettingmobile.google.reader.db.ItemTagChangeListener;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

final class ItemListAdapter extends ListItemCursorAdapter implements ItemTagChangeListener {
    private final ItemTagChangeDatabaseAdapter itemTagChangeAdapter;
    private boolean markAllRead;
    private final SortedMap<Integer, Long> unreadIdsByPosition = new TreeMap<Integer, Long>();
    private final SortedMap<Long, Integer> positionsById = new TreeMap<Long, Integer>();
    private long[] itemKeys = new long[0];

    public ItemListAdapter(Context context, ItemTagChangeDatabaseAdapter itemTagChangeAdapter, ItemRowViewType itemRowViewType) {
        super(context, itemRowViewType);
        registerViewType(new FeedSectionHeaderViewType());
        this.itemTagChangeAdapter = itemTagChangeAdapter;
        itemTagChangeAdapter.addListener(this);
    }

    @Override
    public void changeCursor(EntityCursor<? extends ListItem> cursor) {
        throw new UnsupportedOperationException("Call changeCursor(ItemCursor) instead.");
    }

    public void changeCursor(ItemCursor cursor) {
        /*
         * reset
         */
        markAllRead = false;
        unreadIdsByPosition.clear();
        positionsById.clear();
        if (cursor != null) {
            final List<Long> keys = new ArrayList<Long>();
            int position = 0;
            for (ItemCursor.IndexEntry e : cursor.getIndex()) {
                if (!e.isGroupHeader()) {
                    keys.add(e.getId());
                    positionsById.put(e.getId(), position);
                    if (e.isUnread()) {
                        unreadIdsByPosition.put(position, e.getId());
                    }
                }
                ++position;
            }
            itemKeys = new long[keys.size()];
            for (int i = 0; i < itemKeys.length; ++i) {
                itemKeys[i] = keys.get(i);
            }
        } else {
            itemKeys = new long[0];
        }

        /*
         * set cursor
         */
        super.changeCursor(new Entity2ListItemCursor<Item>(cursor));
    }

    public void markAllRead() {
        markAllRead = true;
        unreadIdsByPosition.clear();
    }
    
    public long[] getItemKeys() {
        return itemKeys;
    }

    public Item adjustItem(Item item) {
        if (item != null) {
            itemTagChangeAdapter.adjustItemTags(item);
            if (markAllRead)
                item.setRead(true);
        }
        return item;
    }

    public boolean isRead() {
        return unreadIdsByPosition.isEmpty();
    }
    
    public List<Long> getUnreadKeysToPosition(int pos) {
        return new ArrayList<Long>(unreadIdsByPosition.headMap(pos + 1).values());
    }

    /*
     * item tag change listener
     */

    @Override
    public void onItemReadStateChanged(long itemKey, boolean read) {
        final Integer position = positionsById.get(itemKey);
        if (position != null) {
            if (read) {
                unreadIdsByPosition.remove(position);
            } else {
                unreadIdsByPosition.put(position, itemKey);
            }
        }
    }

    @Override
    public void onItemTagChanged(long itemKey, ElementId tag, boolean added) {
        // don't care
    }
}
