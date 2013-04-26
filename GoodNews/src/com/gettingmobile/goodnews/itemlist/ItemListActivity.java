package com.gettingmobile.goodnews.itemlist;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.gettingmobile.android.widget.ListItem;
import com.gettingmobile.goodnews.AutomaticallyClosingElementListActivity;
import com.gettingmobile.goodnews.R;
import com.gettingmobile.goodnews.itemview.ItemViewActivity;
import com.gettingmobile.goodnews.settings.ItemDisplayType;
import com.gettingmobile.goodnews.settings.ItemListSettingsHandler;
import com.gettingmobile.goodnews.settings.SettingsHandler;
import com.gettingmobile.google.reader.*;
import com.gettingmobile.google.reader.db.ItemCursor;
import com.gettingmobile.google.reader.db.ItemDatabaseAdapter;
import com.gettingmobile.google.reader.db.ItemTagChangeDatabaseAdapter;
import com.gettingmobile.google.reader.db.SortOrder;

import java.util.Arrays;

public final class ItemListActivity extends AutomaticallyClosingElementListActivity implements
        OnItemClickListener, AbsListView.OnScrollListener {
    private static final String LOG_TAG = "goodnews.ItemListActivity";
    private static final String EXTRA_KEY_ITEM_KEYS = EXTRA_KEY_BASE + "ITEM_KEYS";

    protected final ItemTagChangeDatabaseAdapter itemTagChangeAdapter = new ItemTagChangeDatabaseAdapter();
    private ItemListView listView = null;
    private ItemRowViewType itemRowViewType = new ItemRowViewType(this);
    protected final ItemListAdapter itemListAdapter = new ItemListAdapter(this, itemTagChangeAdapter, itemRowViewType);
	private long[] itemKeys = null;
    private ItemListProvider itemListProvider = null;

	public ItemListActivity() {
        super(R.integer.tip_group_item_list);

		setViewLoader(new ItemListLoader());
		setViewUpdater(new ItemListUpdater());
	}

    protected ItemListProvider createItemListProvider() {
        final ElementId elementId = getIntentElementId();
        if (elementId.equals(ItemState.STARRED.getId()) || elementId.equals(getApp().getSettings().getLabelReadListId())) {
            return new SpecialTagItemsListProvider(getApp(), elementId, getIntentElementKey(), getIntentElementTitle());
        } else if (elementId.equals(ItemState.READING_LIST.getId())) {
            return new AllItemsListProvider(getApp(), elementId, getIntentElementTitle());
        } else if (elementId.getType() == ElementType.FEED) {
            return new FeedItemsListProvider(getApp(), elementId, getIntentElementTitle());
        } else if (getIntentElementIsStateOrLabel()) {
            return new LabelItemsListProvider(getApp(), elementId, getIntentElementKey(), getIntentElementTitle());
        } else {
            return new FolderItemsListProvider(getApp(), elementId, getIntentElementKey(), getIntentElementTitle());
        }
    }

    /*
     * life cycle management
     */

    @Override
    public void onCreate() {
        itemListProvider = createItemListProvider();

        setContentView(R.layout.item_list);

        super.onCreate();

        listView = (ItemListView) findViewById(R.id.items);
        listView.setOnItemClickListener(this);
        registerForContextMenu(listView);
        listView.setOnScrollListener(this);
        listView.setAdapter(itemListAdapter);
    }

    @Override
    protected void onPause() {
        commitTagChanges();
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d(LOG_TAG, "Saving instance state. itemKeys = " + Arrays.toString(itemKeys));
        outState.putLongArray(EXTRA_KEY_ITEM_KEYS, itemKeys);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onPostCreateRestoreSavedInstanceState(Bundle savedInstanceState) {
        itemKeys = savedInstanceState.getLongArray(EXTRA_KEY_ITEM_KEYS);
        Log.d(LOG_TAG, "Restoring instance state. itemKeys = " + Arrays.toString(itemKeys));
        super.onPostCreateRestoreSavedInstanceState(savedInstanceState);
    }

    protected void commitTagChanges() {
        if (itemTagChangeAdapter.commitChanges(getDb())) {
            getApp().onItemTagsChanged();
        }
    }

    @Override
    protected void closeCursor() {
        itemListAdapter.close();
    }

    /*
     * helpers
     */

    public static Intent createItemListIntent(Context context, Tag tag) {
        return createIntent(ItemListActivity.class, context, tag);
    }

    @Override
	protected ListView getListView() {
		return listView;
	}

    @Override
    protected boolean isUnreadListRead() {
        return itemListAdapter.isRead();
    }

    /*
     * click events
     */

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (parent.getId() == R.id.items) {
            final ListItem listItem = (ListItem) itemListAdapter.getItem(position);
            if (listItem.getViewType() == ItemListAdapter.VIEW_TYPE_DEFAULT) {
			    onItemClick((Item) listItem.getItem());
            }
		}
	}

    public void openItem(Item item) {
        ItemViewActivity.startItemViewActivity(this, getIntentElementTitle(), item.getKey(), itemKeys);
    }

    public void openItemInBrowser(Item item) {
        startActivity(new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(item.getAlternate().getHref())));

        /*
         * mark item as read and remove it from read list
         */
        if (getApp().getSettings().markReadOnView()) {
            itemTagChangeAdapter.markItemRead(item, true);
        }
        if (getApp().getSettings().removeFromReadListOnView()) {
            itemTagChangeAdapter.removeItemTag(item, getApp().getSettings().getLabelReadListId());
        }
    }
	
	public void onItemClick(Item item) {
        Log.d(LOG_TAG, "Clicked item " + item.getId());
		boolean openInBrowser = getApp().getSettings().getItemDisplayType(item) == ItemDisplayType.BROWSER;
		if (openInBrowser && item.getAlternate() != null) {
            openItemInBrowser(item);
		} else {
            openItem(item);
		}
	}

	/*
	 * mark read handling
	 */

	@Override
	protected boolean requiresMarkReadConfirmationDialog() {
		return getApp().getSettings().confirmMarkAllReadInItemList();
	}

    @Override
    protected void markAllRead(SQLiteDatabase db) {
        final ItemDatabaseAdapter itemAdapter = new ItemDatabaseAdapter();
        final ElementId id = getIntentElementId();
        if (ItemState.READING_LIST.getId().equals(getIntentElementId())) {
            itemAdapter.markAllRead(db);
        } else if (id.getType() == ElementType.FEED) {
            itemAdapter.markReadByFeed(db, id);
        } else if (getIntentElementIsStateOrLabel()) {
            itemAdapter.markReadByLabel(db, id);
        } else {
            itemAdapter.markReadByFolder(db, id);
        }
        itemListAdapter.markAllRead();
    }

    @Override
	protected void onMarkedRead() {
		if (automaticallyCloseWhenRead()) {
			finish();
		} else {
			listView.updateVisibleItems();
		}
	}

    protected void onMarkedItemRead() {
        if (automaticallyCloseWhenRead() && isUnreadListRead()) {
            finish();
        }
    }

    protected void markReadTo(int lastIndex) {
        itemTagChangeAdapter.markItemsReadByKey(itemListAdapter.getUnreadKeysToPosition(lastIndex));
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int i) {
        // not interested
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (firstVisibleItem > 0 && getApp().getSettings().markReadOnScrollOver()) {
            markReadTo(firstVisibleItem - 1);
        }
    }

	/*
	 * options menus
	 */

    @Override
	protected int getOptionsMenuResourceId() {
		return R.menu.item_list_options;
	}

    @Override
    protected Class<? extends SettingsHandler> getViewSettingsHandlerClass() {
        return ItemListSettingsHandler.class;
    }

    @Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.menu_sort_ascending).setVisible(SortOrder.DESCENDING.equals(getSortOrder()));
		menu.findItem(R.id.menu_sort_descending).setVisible(SortOrder.ASCENDING.equals(getSortOrder()));

        /*
         * handle grouping items
         */
        final boolean supportsGrouping = supportsGroupingByFeed();
        menu.findItem(R.id.menu_grouped_feed).setVisible(supportsGrouping && !getGroupByFeeds());
        menu.findItem(R.id.menu_grouped_flat).setVisible(supportsGrouping && getGroupByFeeds());

        /*
         * enable feed options if applicable
         */
        menu.findItem(R.id.menu_feed_preferences).setVisible(isFeedsList());

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_sort_ascending) {
            setSortOrder(SortOrder.ASCENDING);
        } else if (item.getItemId() == R.id.menu_sort_descending) {
            setSortOrder(SortOrder.DESCENDING);
        }else if (item.getItemId() == R.id.menu_grouped_feed) {
            setGroupByFeeds(true);
        } else if (item.getItemId() == R.id.menu_grouped_flat) {
            setGroupByFeeds(false);
        } else if (item.getItemId() == R.id.menu_feed_preferences) {
            showFeedOptions();
		} else {
		    return super.onOptionsItemSelected(item);
        }
        return true;
	}

    /*
     * context menu handling
     */

    protected ItemRowViewHandler getItemRowViewHandler(ContextMenu.ContextMenuInfo menuInfo) {
        ItemRowViewHandler rowViewHandler = null;
        if (menuInfo instanceof ListView.AdapterContextMenuInfo) {
            final ListView.AdapterContextMenuInfo cmi = (ListView.AdapterContextMenuInfo) menuInfo;
            if (cmi.targetView != null) {
                rowViewHandler = (ItemRowViewHandler) cmi.targetView.getTag();
            }
        }
        return rowViewHandler;
    }

    protected int getItemIndex(ContextMenu.ContextMenuInfo menuInfo) {
        return (menuInfo instanceof ListView.AdapterContextMenuInfo) ?
                ((ListView.AdapterContextMenuInfo) menuInfo).position : -1;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        if (v.getId() == R.id.items) {
            final ItemRowViewHandler rowViewHandler = getItemRowViewHandler(menuInfo);
            if (rowViewHandler != null) {
                getMenuInflater().inflate(R.menu.item_context, menu);
                menu.setHeaderTitle(R.string.contextmenu_item);
                rowViewHandler.onPrepareContextMenu(menu);
            }
        }
    }

    @Override
    public boolean onContextItemSelected(android.view.MenuItem item) {
        final ItemRowViewHandler rowViewHandler = getItemRowViewHandler(item.getMenuInfo());
        if (rowViewHandler != null) {
            if (item.getItemId() == R.id.menu_item_mark_read_until_here) {
                markReadTo(getItemIndex(item.getMenuInfo()));
                return true;
            } else {
                return rowViewHandler.onContextItemSelected(item.getItemId());
            }
        } else {
            return super.onContextItemSelected(item);
        }
    }

    /*
    * command handling
    */

    protected boolean isFeedsList() {
        final ElementId id = getIntentElementId();
        return id.getType() == ElementType.FEED;
    }

    protected boolean supportsGroupingByFeed() {
        return !isFeedsList();
    }

    protected boolean getGroupByFeeds() {
        return supportsGroupingByFeed() && (getIntentElementIsStateOrLabel() ?
                getApp().getSettings().groupByFeedsInLabels() : getApp().getSettings().groupByFeedsInFolders());
    }

    protected void setGroupByFeeds(boolean groupByFeeds) {
        if (getIntentElementIsStateOrLabel()) {
            getApp().getSettings().setGroupByFeedsInLabels(groupByFeeds);
        } else {
            getApp().getSettings().setGroupByFeedsInFolders(groupByFeeds);
        }
        if (itemKeys != null) {
            updateView();
        } else {
            loadView();
        }
    }

	@Override
	protected boolean getHideRead() {
		return getIntentElementIsStateOrLabel() ? 
				getApp().getSettings().hideReadInLabelItemList() : getApp().getSettings().hideRead();
	}
	
	@Override
	protected void setHideReadSetting(boolean hideRead) {
		if (getIntentElementIsStateOrLabel()) {
			getApp().getSettings().setHideReadInLabelItemList(hideRead);
		} else {
			getApp().getSettings().setHideRead(hideRead);
		}
	}
	
	protected SortOrder getSortOrder() {
		return getApp().getSettings().getItemSortOrder();
	}
	
	protected void setSortOrder(SortOrder sortOrder) {
        getApp().getSettings().setItemSortOrder(sortOrder);
		if (itemKeys != null) {
			updateView();
		} else {
			loadView();
		}
	}

    private void showFeedOptions() {
        startActivity(settingsIntentFactory.createFeedSettingsIntent(getIntentElementId()));
    }

    /*
    * inner classes
    */

	private class ItemListUpdater extends ViewOperation<ItemCursor> {
		@Override
		protected ItemCursor fetchData(SQLiteDatabase readOnlyDb) {
            Log.d(LOG_TAG, "committing tag changes");
            commitTagChanges();
            Log.d(LOG_TAG, "done committing tag changes");
            return itemKeys != null ? loadItemsByKey(readOnlyDb) : loadItemsByList(readOnlyDb);
		}

        private ItemCursor loadItemsByKey(SQLiteDatabase readOnlyDb) {
            Log.d(LOG_TAG, "loading items by key; itemKeys = " + Arrays.toString(itemKeys));
            final ItemCursor c = itemListProvider.readListItemsByKeys(readOnlyDb, itemKeys, getGroupByFeeds(), getSortOrder());
            Log.d(LOG_TAG, "number of keys: " + itemKeys.length + "; number of entries: " + c.getCount());
            return c;
        }

        private ItemCursor loadItemsByList(SQLiteDatabase readOnlyDb) {
            Log.d(LOG_TAG, "loading item list using " + itemListProvider.getClass() + "; group=" + getGroupByFeeds() + "; sortOrder=" + getSortOrder() + "; itemKeys=" + Arrays.toString(itemKeys));
            final ItemCursor itemCursor = itemListProvider.readListItems(readOnlyDb, getGroupByFeeds(), getHideRead(), getSortOrder());
            Log.d(LOG_TAG, "received item cursor for " + itemCursor.getCount() + " entries");
            return itemCursor;
        }

		@Override
		protected boolean updateView(ItemCursor items) {
            Log.d(LOG_TAG, "applying item cursor to adapter");
			itemListAdapter.changeCursor(items);
			itemKeys = itemListAdapter.getItemKeys();
            Log.d(LOG_TAG, "Received itemKeys = " + Arrays.toString(itemKeys));
			return items.getCount() > 0;
		}
	}

    final private class ItemListLoader extends ItemListUpdater {
        @Override
        protected ItemCursor fetchData(SQLiteDatabase readOnlyDb) {
            itemKeys = null;
            return super.fetchData(readOnlyDb);
        }
    }
}