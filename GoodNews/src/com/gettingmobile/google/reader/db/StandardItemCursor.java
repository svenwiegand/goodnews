package com.gettingmobile.google.reader.db;

import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.util.Log;
import com.gettingmobile.google.reader.Item;

import java.util.ArrayList;
import java.util.List;

final class StandardItemCursor extends AbsEntityCursor<Item> implements ItemCursor {
    private static final String LOG_TAG = "goodnews.ItemCursor";
    private final ArrayList<Entry> entries;
    private int pos = -1;
    private final int feedTitleCol;

    public StandardItemCursor(boolean group, Cursor indexCursor, Cursor cursor) {
        super(new ItemCursorAdapter(), cursor);

        try {
            if (indexCursor.getCount() != cursor.getCount()) {
                Log.w(LOG_TAG, "Detected differing cursor counts: indexCursor=" + indexCursor.getCount() + "; cursor=" + cursor.getCount());
            }
            feedTitleCol = cursor.getColumnIndex("feedTitle");

            /*
             * process index
             */
            final int groupIdCol = 0;
            final int entityIdCol = 1;
            final int joinCountCol = 2;
            final int readStateCol = 3;

            entries = new ArrayList<Entry>(indexCursor.getCount());
            if (indexCursor.moveToFirst()) {
                int cursorPos = 0;
                long lastGroupId = 0;
                do {
                    /*
                     * insert group header if this position starts a new group
                     */
                    if (group) {
                        final long groupId = indexCursor.getLong(groupIdCol);
                        if (lastGroupId != groupId) {
                            // a new group starts
                            entries.add(new GroupHeader(groupId, cursorPos));
                            lastGroupId = groupId;
                        }
                    }

                    /*
                     * add item entry
                     */
                    final long entityId = indexCursor.getLong(entityIdCol);
                    int joinCount = indexCursor.getInt(joinCountCol);
                    if (joinCount < 1) {
                        joinCount = 1;
                    }
                    entries.add(new Entity(entityId, cursorPos, joinCount, indexCursor.getInt(readStateCol) == 0));
                    cursorPos+= joinCount;
                } while (indexCursor.moveToNext());
            }
        } finally {
            indexCursor.close();
        }
    }

    @Override
    public List<? extends IndexEntry> getIndex() {
        return entries;
    }

    @Override
    public long getEntityId() {
        checkPosition();
        return entries.get(pos).id;
    }

    @Override
    public Item getEntity() {
        checkPosition();
        try {
            final Entity entry = (Entity) entries.get(pos);
            cursor.moveToPosition(entry.startPos);
            final Item item = adapter.readEntity(cursor);
            for (int i = 1; i < entry.count; ++i) {
                cursor.moveToNext();
                adapter.readEntityJoin(item, cursor);
            }
            return item;
        } catch (ClassCastException ex) {
            throw new IllegalStateException("The entry at cursor position " + pos + " is a header item");
        }
    }

    @Override
    public boolean isGroupHeader() {
        checkPosition();
        return entries.get(pos).isGroupHeader;
    }

    @Override
    public String getGroupTitle() {
        checkPosition();
        try {
            final GroupHeader gh = (GroupHeader) entries.get(pos);
            cursor.moveToPosition(gh.startPos);
            return cursor.getString(feedTitleCol);
        } catch (ClassCastException ex) {
            throw new IllegalStateException("The entry at cursor position " + pos + " is no group header");
        }
    }

    /*
     * position query
     */

    public int getCount() {
        return entries.size();
    }

    public int getPosition() {
        return pos;
    }

    public boolean isBeforeFirst() {
        return pos < 0;
    }

    public boolean isAfterLast() {
        return pos >= entries.size();
    }

    public boolean isFirst() {
        return pos == 0;
    }

    public boolean isLast() {
        return pos == entries.size() - 1;
    }

    /*
     * moving
     */

    public boolean moveToFirst() {
        return moveToPosition(0);
    }

    public boolean moveToLast() {
        return moveToPosition(getCount() - 1);
    }

    public boolean moveToNext() {
        return moveToPosition(pos + 1);
    }

    public boolean moveToPrevious() {
        return moveToPosition(pos - 1);
    }

    public boolean move(int offset) {
        return moveToPosition(pos + offset);
    }

    public boolean moveToPosition(int position) {
        // Make sure position isn't past the end of the cursor
        final int count = getCount();
        if (position >= count) {
            pos = count;
            return false;
        }

        // Make sure position isn't before the beginning of the cursor
        if (position < 0) {
            pos = -1;
            return false;
        }

        // Check for no-op moves, and skip the rest of the work for them
        if (position == pos) {
            return true;
        }

        pos = position;
        return true;
    }

    /*
    * helpers
    */

    protected void checkPosition() {
        if (-1 == pos || getCount() == pos) {
            throw new CursorIndexOutOfBoundsException(pos, getCount());
        }
    }

    /*
    * inner classes
    */

    private static abstract class Entry implements IndexEntry {
        public final long id;
        public final int startPos;
        public final boolean isGroupHeader;
        public final boolean isUnread;

        protected Entry(long id, int startPos, boolean isGroupHeader, boolean isUnread) {
            this.id = id;
            this.startPos = startPos;
            this.isGroupHeader = isGroupHeader;
            this.isUnread = isUnread;
        }

        @Override
        public long getId() {
            return id;
        }

        @Override
        public boolean isGroupHeader() {
            return isGroupHeader;
        }

        @Override
        public boolean isUnread() {
            return isUnread;
        }
    }

    private static class GroupHeader extends Entry {
        public GroupHeader(long id, int startPos) {
            super(-1 * id, startPos, true, false);
        }
    }
    
    private static class Entity extends Entry {
        public final int count;
        
        public Entity(long id, int startPos, int count, boolean isUnread) {
            super(id, startPos, false, isUnread);
            this.count = count;
        }
    }
}
