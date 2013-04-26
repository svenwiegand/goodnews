package com.gettingmobile.google.reader.db;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import com.gettingmobile.google.reader.ElementId;
import com.gettingmobile.google.reader.Item;
import com.gettingmobile.google.reader.ItemState;
import com.gettingmobile.google.reader.ItemTagChangeEvent;

import java.util.*;

public final class ItemTagChangeDatabaseAdapter {
    private final Set<ItemTagChangeListener> listeners = new HashSet<ItemTagChangeListener>();
    private final Map<Long, Map<ElementId, ItemTagChangeEvent>> tagChangeEventsByItemKey =
            new HashMap<Long, Map<ElementId, ItemTagChangeEvent>>();
    private final Map<Long, Boolean> readStateByItemKey = new HashMap<Long, Boolean>();

    public static boolean hasGlobalChanges(SQLiteDatabase db) {
        return new ItemTagChangeEventDatabaseAdapter().readHasChanges(db) ||
                new ItemDatabaseAdapter().hasUpdatedReadStates(db);
    }

    public static void dismissGlobalChanges(SQLiteDatabase db) {
        db.beginTransaction();
        try {
            new ItemTagChangeEventDatabaseAdapter().delete(db);
            new ItemDatabaseAdapter().updateSyncedRead(db);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public boolean addListener(ItemTagChangeListener listener) {
        return listeners.add(listener);
    }

    public boolean removeListener(ItemTagChangeListener listener) {
        return listeners.remove(listener);
    }
    
    public synchronized void markItemRead(Long itemKey, Boolean read) {
        readStateByItemKey.put(itemKey, read);
        fireOnItemReadStateChanged(itemKey, read);
    }
    
    public void markItemRead(Item item, Boolean read) {
        item.setRead(read);
        markItemRead(item.getKey(), read);
    }

    public void markItemsRead(Collection<Item> items) {
        for (Item item : items) {
            markItemRead(item, true);
        }
    }
    
    public void markItemsReadByKey(Collection<Long> itemKeys) {
        for (Long itemKey : itemKeys) {
            markItemRead(itemKey, true);
        }
    }

    public void addItemTag(Item item, ElementId tag) {
        changeItemTag(item, tag, true);
    }

    public void removeItemTag(Item item, ElementId tag) {
        changeItemTag(item, tag, false);
    }

    public void changeItemTag(Item item, ElementId tagId, boolean add) {
        /*
         * at first enqueue an event entry to the sync database
         */
        getOrCreateItemTagChangeEvents(item.getKey()).put(tagId, new ItemTagChangeEvent(item, add, tagId));        

        /*
         * now adjust the object's tag list
         */
        if (add) {
            item.getTagIds().add(tagId);
        } else {
            item.getTagIds().remove(tagId);
        }
        fireOnItemTagChanged(item.getKey(), tagId, add);
    }

    public void setItemTags(Item item, Set<ElementId> tags) {
		// determine which tags to be removed from the item
		for (ElementId tag : item.getTagIds()) {
			if (!tags.contains(tag)) {
                changeItemTag(item, tag, false);
			}
		}
		
		// determine which tags to be added to the item
		for (ElementId tag : tags) {
			if (!item.getTagIds().contains(tag)) {
                changeItemTag(item, tag, true);
			}
		}
    }

    public synchronized boolean needsCommit() {
        return !readStateByItemKey.isEmpty() || !tagChangeEventsByItemKey.isEmpty();
    }

    /**
     * Writes the pending tag and read state changes to the database.
     * @param db the database to write the changes to.
     * @return whether there were changes which have been commited or not.
     */
    public synchronized boolean commitChanges(SQLiteDatabase db) {
        if (!needsCommit())
            return false;

        db.beginTransaction();
        try {
            boolean changes = false;

            /*
             * write item read state to database
             */
            final ItemDatabaseAdapter itemAdapter = new ItemDatabaseAdapter();
            final SQLiteStatement updateItemReadStatement = itemAdapter.compileMarkItemReadStatement(db);
            for (Map.Entry<Long, Boolean> itemReadState : readStateByItemKey.entrySet()) {
                itemAdapter.markItemRead(updateItemReadStatement, itemReadState.getKey(), itemReadState.getValue());
                changes = true;
            }

            /*
             * create tag change events
             */
            final ItemTagChangeEventDatabaseAdapter eventAdapter = new ItemTagChangeEventDatabaseAdapter();
            final SQLiteStatement insertEventStatement = eventAdapter.compileInsertStatement(db);
            for (Map.Entry<Long, Map<ElementId, ItemTagChangeEvent>> entry : tagChangeEventsByItemKey.entrySet()) {
                final long itemKey = entry.getKey();
                final Map<ElementId, ItemTagChangeEvent> tagChangeEvents = entry.getValue();

                for (Map.Entry<ElementId, ItemTagChangeEvent> tagChangeEvent : tagChangeEvents.entrySet()) {
                    final ElementId tagId = tagChangeEvent.getKey();
                    final ItemTagChangeEvent e = tagChangeEvent.getValue();

                    /*
                     * adjust the item in the database
                     */
                    if (e.isAddOperation()) {
                        itemAdapter.addItemTag(db, itemKey, tagId);
                    } else {
                        itemAdapter.removeItemTag(db, itemKey, tagId);
                    }

                    /*
                     * write change event to the database
                     */
                    eventAdapter.insert(insertEventStatement, e);
                }
                changes = true;
            }
            db.setTransactionSuccessful();

            /*
             * dismiss stored changes
             */
            readStateByItemKey.clear();
            tagChangeEventsByItemKey.clear();

            return changes;
        } finally {
            db.endTransaction();
        }
    }
    
    /**
     * Adjust the specified item's tags and read state based on the enqueued changes.
     * @param item the item to be adjusted.
     * @return returns the passed in item.
     */
    public synchronized Item adjustItemTags(Item item) {
        /*
         * process read state
         */
        final Boolean read = readStateByItemKey.get(item.getKey());
        if (read != null) {
            item.setRead(read);
        }

        /*
        * process tag changes
        */
        final Map<ElementId, ItemTagChangeEvent> tagChangeEvents = tagChangeEventsByItemKey.get(item.getKey());
        if (tagChangeEvents != null) {
            for (ItemTagChangeEvent tagChangeEvent : tagChangeEvents.values()) {
                if (ItemState.READ.getId().equals(tagChangeEvent.getTagId())) {
                    // read state changes need to be handled in a special manner as they are no tags
                    // from an item's point of view
                    item.setRead(tagChangeEvent.isAddOperation());
                } else if (!isImplicitItemState(tagChangeEvent.getTagId())) {
                    // generic handling for all other tags
                    if (tagChangeEvent.isAddOperation()) {
                        item.getTagIds().add(tagChangeEvent.getTagId());
                    } else {
                        item.getTagIds().remove(tagChangeEvent.getTagId());
                    }
                }
            }
        }
        return item;
    }

    private synchronized Map<ElementId, ItemTagChangeEvent> getOrCreateItemTagChangeEvents(Long key) {
        Map<ElementId, ItemTagChangeEvent> events = tagChangeEventsByItemKey.get(key);
        if (events == null) {
            events = new HashMap<ElementId, ItemTagChangeEvent>();
            tagChangeEventsByItemKey.put(key, events);
        }
        return events;
    }
    
    private static boolean isImplicitItemState(ElementId tagId) {
        return ItemState.KEPT_UNREAD.getId().equals(tagId) || ItemState.TRACKING_KEPT_UNREAD.getId().equals(tagId);
    }
    
    /*
     * event handling
     */
    
    private void fireOnItemReadStateChanged(long itemKey, boolean read) {
        for (ItemTagChangeListener l : listeners) {
            l.onItemReadStateChanged(itemKey, read);
        }
    }
    
    private void fireOnItemTagChanged(long itemKey, ElementId tagId, boolean added) {
        for (ItemTagChangeListener l : listeners) {
            l.onItemTagChanged(itemKey, tagId, added);
        }
    }
}
