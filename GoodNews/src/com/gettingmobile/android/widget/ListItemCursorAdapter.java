package com.gettingmobile.android.widget;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import com.gettingmobile.google.reader.db.EntityCursor;

import java.util.HashMap;
import java.util.Map;

public class ListItemCursorAdapter extends AbsListItemCursorAdapter {
    public static final int VIEW_TYPE_DEFAULT = 0;
    public static final int VIEW_TYPE_SEPARATOR = 1;
    public static final int VIEW_TYPE_HEADER = 2;
    public static final int VIEW_TYPE_CUSTOM_FIRST_ID = 3;

    private final Map<Integer, ListItemViewType> viewTypes = new HashMap<Integer, ListItemViewType>();
    private final Map<Integer, Integer> viewTypeIndexById = new HashMap<Integer, Integer>();

    public ListItemCursorAdapter(Context context, EntityCursor<? extends ListItem> c, ListItemViewType defaultViewType, boolean registerContentObserver) {
        super(context, c, registerContentObserver);
        if (defaultViewType != null) {
            registerViewType(defaultViewType);
        }
    }
    

    public ListItemCursorAdapter(Context context, EntityCursor<? extends ListItem> c, ListItemViewType defaultViewType) {
        this(context, c, defaultViewType, false);
    }
    
    public ListItemCursorAdapter(Context context, ListItemViewType defaultViewType) {
        this(context, null, defaultViewType);
    }
    
    public ListItemCursorAdapter(Context context) {
        this(context, null);
    }

    public void registerViewType(ListItemViewType viewType) {
        viewTypeIndexById.put(viewType.getId(), viewTypes.size());
        viewTypes.put(viewType.getId(), viewType);
    }

    public int getViewTypeCount() {
        return viewTypes.size();
    }

    public ListItemViewType getViewTypeById(int id) {
        return viewTypes.get(id);
    }
    
    public int getItemViewTypeId(int position) {
        if (!mDataValid) {
            throw new IllegalStateException("this should only be called when the cursor is valid");
        }
        if (!mCursor.moveToPosition(position)) {
            throw new IllegalStateException("couldn't move cursor to position " + position);
        }
        return mCursor.getEntity().getViewType();
    }
    
    @Override
   	public int getItemViewType(int position) {
   		return viewTypeIndexById.get(getItemViewTypeId(position));
   	}

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (!mDataValid) {
            throw new IllegalStateException("this should only be called when the cursor is valid");
        }
        if (!mCursor.moveToPosition(position)) {
            throw new IllegalStateException("couldn't move cursor to position " + position);
        }

        final ListItem item = mCursor.getEntity();
        final ListItemViewType viewType = viewTypes.get(item.getViewType());
        final View view;
        if (convertView != null) {
            view = convertView;
        } else {
            view = viewType.createView(parent);
        }
        viewType.bindView(view, item.getItem());
        return view;
    }

    @Override
    public boolean isEnabled(int position) {
        return  viewTypes.get(getItemViewTypeId(position)).isEnabled();
    }
}
