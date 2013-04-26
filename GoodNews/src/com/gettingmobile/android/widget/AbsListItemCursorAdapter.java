package com.gettingmobile.android.widget;

import android.content.Context;
import android.database.ContentObserver;
import android.database.DataSetObserver;
import android.os.Handler;
import android.widget.BaseAdapter;
import com.gettingmobile.google.reader.db.EntityCursor;

/**
 * Adapter that exposes data from a {@link EntityCursor} to a
 * {@link android.widget.ListView ListView} widget. The Cursor must include
 * a column named "_id" or this class will not work.
 */
public abstract class AbsListItemCursorAdapter extends BaseAdapter {
    /**
     * This field should be made private, so it is hidden from the SDK.
     * {@hide}
     */
    protected boolean mDataValid;
    /**
     * This field should be made private, so it is hidden from the SDK.
     * {@hide}
     */
    protected EntityCursor<? extends ListItem> mCursor;
    /**
     * This field should be made private, so it is hidden from the SDK.
     * {@hide}
     */
    protected Context mContext;
    /**
     * This field should be made private, so it is hidden from the SDK.
     * {@hide}
     */
    protected ChangeObserver mChangeObserver;
    /**
     * This field should be made private, so it is hidden from the SDK.
     * {@hide}
     */
    protected DataSetObserver mDataSetObserver;

    /**
     * If set the adapter will register a content observer on the cursor and will call
     * {@link #onContentChanged()} when a notification comes in.  Be careful when
     * using this flag: you will need to unset the current Cursor from the adapter
     * to avoid leaks due to its registered observers.  This flag is not needed
     * when using a AbsListItemCursorAdapter with a
     * {@link android.content.CursorLoader}.
     */
    //public static final int FLAG_REGISTER_CONTENT_OBSERVER = 0x02;

    /**
     * Recommended constructor.
     *
     * @param c The cursor from which to get the data.
     * @param context The context
     */
    public AbsListItemCursorAdapter(Context context, EntityCursor<? extends ListItem> c, boolean registerContentObserver) {
        init(context, c, registerContentObserver);
    }
    
    public AbsListItemCursorAdapter(Context context, EntityCursor<? extends ListItem> c) {
        this(context, c, false);
    }

    void init(Context context, EntityCursor<? extends ListItem> c, boolean registerContentObserver) {
        boolean cursorPresent = c != null;
        mCursor = c;
        mDataValid = cursorPresent;
        mContext = context;
        if (registerContentObserver) {
            mChangeObserver = new ChangeObserver();
            mDataSetObserver = new MyDataSetObserver();
        } else {
            mChangeObserver = null;
            mDataSetObserver = null;
        }

        if (cursorPresent) {
            if (mChangeObserver != null) c.registerContentObserver(mChangeObserver);
            if (mDataSetObserver != null) c.registerDataSetObserver(mDataSetObserver);
        }
    }

    /**
     * Returns the cursor.
     * @return the cursor.
     */
    public EntityCursor<? extends ListItem> getCursor() {
        return mCursor;
    }

    public void close() {
        if (mCursor != null) {
            mCursor.close();
        }
    }

    /**
     * @see android.widget.ListAdapter#getCount()
     */
    public int getCount() {
        if (mDataValid && mCursor != null) {
            return mCursor.getCount();
        } else {
            return 0;
        }
    }

    /**
     * @see android.widget.ListAdapter#getItem(int)
     */
    public Object getItem(int position) {
        if (mDataValid && mCursor != null) {
            mCursor.moveToPosition(position);
            return mCursor.getEntity();
        } else {
            return null;
        }
    }

    /**
     * @see android.widget.ListAdapter#getItemId(int)
     */
    public long getItemId(int position) {
        if (mDataValid && mCursor != null) {
            if (mCursor.moveToPosition(position)) {
                return mCursor.getEntityId();
            } else {
                return 0;
            }
        } else {
            return 0;
        }
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    /**
     * Change the underlying cursor to a new cursor. If there is an existing cursor it will be
     * closed.
     *
     * @param cursor The new cursor to be used
     */
    public void changeCursor(EntityCursor<? extends ListItem> cursor) {
        EntityCursor<? extends ListItem> old = swapCursor(cursor);
        if (old != null) {
            old.close();
        }
    }

    /**
     * Swap in a new Cursor, returning the old Cursor.  Unlike
     * {@link #changeCursor(EntityCursor<? extends ListItem>)}, the returned old Cursor is <em>not</em>
     * closed.
     *
     * @param newCursor The new cursor to be used.
     * @return Returns the previously set Cursor, or null if there wasa not one.
     * If the given new Cursor is the same instance is the previously set
     * Cursor, null is also returned.
     */
    public EntityCursor<? extends ListItem> swapCursor(EntityCursor<? extends ListItem> newCursor) {
        if (newCursor == mCursor) {
            return null;
        }
        EntityCursor<? extends ListItem> oldCursor = mCursor;
        if (oldCursor != null) {
            if (mChangeObserver != null) oldCursor.unregisterContentObserver(mChangeObserver);
            if (mDataSetObserver != null) oldCursor.unregisterDataSetObserver(mDataSetObserver);
        }
        mCursor = newCursor;
        if (newCursor != null) {
            if (mChangeObserver != null) newCursor.registerContentObserver(mChangeObserver);
            if (mDataSetObserver != null) newCursor.registerDataSetObserver(mDataSetObserver);
            mDataValid = true;
            // notify the observers about the new cursor
            notifyDataSetChanged();
        } else {
            mDataValid = false;
            // notify the observers about the lack of a data set
            notifyDataSetInvalidated();
        }
        return oldCursor;
    }

    /**
     * <p>Converts the cursor into a CharSequence. Subclasses should override this
     * method to convert their results. The default implementation returns an
     * empty String for null values or the default String representation of
     * the value.</p>
     *
     * @param cursor the cursor to convert to a CharSequence
     * @return a CharSequence representing the value
     */
    public CharSequence convertToString(EntityCursor<? extends ListItem> cursor) {
        return cursor == null ? "" : cursor.toString();
    }

    /**
     * Called when the {@link ContentObserver} on the cursor receives a change notification.
     *
     * @see ContentObserver#onChange(boolean)
     */
    protected void onContentChanged() {
    }

    private class ChangeObserver extends ContentObserver {
        public ChangeObserver() {
            super(new Handler());
        }

        @Override
        public boolean deliverSelfNotifications() {
            return true;
        }

        @Override
        public void onChange(boolean selfChange) {
            onContentChanged();
        }
    }

    private class MyDataSetObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            mDataValid = true;
            notifyDataSetChanged();
        }

        @Override
        public void onInvalidated() {
            mDataValid = false;
            notifyDataSetInvalidated();
        }
    }

}
