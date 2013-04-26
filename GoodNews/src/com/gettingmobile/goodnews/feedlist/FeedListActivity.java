package com.gettingmobile.goodnews.feedlist;

import android.database.sqlite.SQLiteDatabase;
import android.view.ContextMenu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import com.gettingmobile.android.widget.Entity2ListItemCursor;
import com.gettingmobile.android.widget.ListItemCursorAdapter;
import com.gettingmobile.goodnews.AutomaticallyClosingElementListActivity;
import com.gettingmobile.goodnews.R;
import com.gettingmobile.goodnews.itemlist.ItemListActivity;
import com.gettingmobile.goodnews.settings.FeedListSettingsHandler;
import com.gettingmobile.goodnews.settings.SettingsHandler;
import com.gettingmobile.google.reader.Feed;
import com.gettingmobile.google.reader.ItemState;
import com.gettingmobile.google.reader.db.EntityCursor;
import com.gettingmobile.google.reader.db.FeedDatabaseAdapter;
import com.gettingmobile.google.reader.db.ItemDatabaseAdapter;

public final class FeedListActivity extends AutomaticallyClosingElementListActivity
        implements OnItemClickListener {
	private final FeedDatabaseAdapter feedAdapter = new FeedDatabaseAdapter();
	private final ItemDatabaseAdapter itemAdapter = new ItemDatabaseAdapter();
    private ListItemCursorAdapter listAdapter = new ListItemCursorAdapter(this, new FeedRowViewType(this));
	
	public FeedListActivity() {
		final FeedListInflator feedListInflator = new FeedListInflator();
		setViewLoader(feedListInflator);
		setViewUpdater(feedListInflator);
	}

    /*
     * life cycle management
     */

    @Override
    public void onCreate() {
        setContentView(R.layout.feed_list);

        super.onCreate();

        final ListView listView = (ListView) findViewById(R.id.feeds);
        listView.setOnItemClickListener(this);
        listView.setAdapter(listAdapter);

        registerForContextMenu(getListView());
    }

    @Override
    protected void closeCursor() {
        listAdapter.close();
    }

   /*
    * helpers
    */

    @Override
    protected Class<? extends SettingsHandler> getViewSettingsHandlerClass() {
        return FeedListSettingsHandler.class;
    }

    @Override
    protected boolean isUnreadListRead() {
        return listAdapter.getCount() == 0;
    }

    @Override
	protected ListView getListView() {
		return (ListView) findViewById(R.id.feeds);
	}

    protected EntityCursor<Feed> getFeedCursor(SQLiteDatabase db) {
        if (ItemState.READING_LIST.getId().equals(getIntentElementId())) {
            return getHideRead() ? feedAdapter.cursorAllUnread(db) : feedAdapter.cursorAll(db);
        } else {
            final boolean sortByDragAndDropOrder = getApp().getSettings().sortByDragAndDropOrder();
            return getHideRead() ? feedAdapter.cursorUnreadByTag(db, getIntentElementKey(), sortByDragAndDropOrder)
                    : feedAdapter.cursorByTag(db, getIntentElementKey(), sortByDragAndDropOrder);
        }
    }

    /*
     * action handling
     */

    @Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		final int viewId = parent.getId();
		if (viewId == R.id.feeds) {
			onFeedClicked((Feed) view.getTag());
		}
	}
	
	protected void onFeedClicked(Feed feed) {
		startActivity(ItemListActivity.class, feed);
	}

    protected Feed getItemRowFeed(ContextMenu.ContextMenuInfo menuInfo) {
        Feed feed = null;
        if (menuInfo instanceof ListView.AdapterContextMenuInfo) {
            final ListView.AdapterContextMenuInfo cmi = (ListView.AdapterContextMenuInfo) menuInfo;
            return (Feed) cmi.targetView.getTag();
        }
        return feed;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);

        if (view.getId() == R.id.feeds) {
            final Feed feed = getItemRowFeed(menuInfo);
            if (feed != null) {
                getMenuInflater().inflate(R.menu.feed_context, menu);
                menu.setHeaderTitle(R.string.contextmenu_feed);
                menu.findItem(R.id.menu_feed_preferences).setIntent(
                        settingsIntentFactory.createElementSettingsIntent(feed.getId(), feed.getTitle()));
            }
        }
    }

	/*
	 * mark read handling
	 */

	@Override
	protected boolean requiresMarkReadConfirmationDialog() {
		return getApp().getSettings().confirmMarkAllReadInFeedList();
	}

    @Override
    protected void markAllRead(SQLiteDatabase db) {
        if (ItemState.READING_LIST.getId().equals(getIntentElementId())) {
            itemAdapter.markAllRead(db);
        } else {
            itemAdapter.markReadByFolder(db, getIntentElementId());
        }
    }

    /*
     * hide read handling
     */
	
	@Override
	protected boolean getHideRead() {
		return getApp().getSettings().hideRead();
	}
	
	@Override
	protected void setHideReadSetting(boolean hideRead) {
		getApp().getSettings().setHideRead(hideRead);
	}

    @Override
    protected void onMarkedRead() {
        if (automaticallyCloseWhenRead()) {
            finish();
        } else {
            super.onMarkedRead();
        }
    }

    /*
     * inner classes
     */
	
	class FeedListInflator extends ViewOperation<EntityCursor<Feed>> {

		@Override
		protected EntityCursor<Feed> fetchData(SQLiteDatabase readOnlyDb) {
			return getFeedCursor(readOnlyDb);
		}

		@Override
		protected boolean updateView(EntityCursor<Feed> feeds) {
            listAdapter.changeCursor(new Entity2ListItemCursor<Feed>(feeds));
			return feeds.getCount() > 0;
		}
		
	}
}