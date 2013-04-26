package com.gettingmobile.goodnews.home;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;
import android.view.ContextMenu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import com.gettingmobile.android.app.DialogFactory;
import com.gettingmobile.android.app.actions.AbstractAction;
import com.gettingmobile.android.app.actions.ActionContext;
import com.gettingmobile.android.widget.ListItem;
import com.gettingmobile.goodnews.ElementListActivity;
import com.gettingmobile.goodnews.R;
import com.gettingmobile.goodnews.account.AccountHandler;
import com.gettingmobile.goodnews.account.LoginCallback;
import com.gettingmobile.goodnews.changelog.ChangelogController;
import com.gettingmobile.goodnews.download.ContentDownloadAction;
import com.gettingmobile.goodnews.feedlist.FeedListActivity;
import com.gettingmobile.goodnews.itemlist.ItemListActivity;
import com.gettingmobile.goodnews.settings.SettingsHandler;
import com.gettingmobile.goodnews.settings.TagListSettingsHandler;
import com.gettingmobile.goodnews.sync.FullSyncAction;
import com.gettingmobile.goodnews.sync.PushSyncAction;
import com.gettingmobile.goodnews.sync.SyncListener;
import com.gettingmobile.goodnews.sync.SyncService;
import com.gettingmobile.goodnews.tip.RatingRequestDialog;
import com.gettingmobile.goodnews.tip.TipDialogHandler;
import com.gettingmobile.goodnews.util.TagFilter;
import com.gettingmobile.google.reader.Feed;
import com.gettingmobile.google.reader.ItemState;
import com.gettingmobile.google.reader.SortedElement;
import com.gettingmobile.google.reader.Tag;
import com.gettingmobile.google.reader.db.FeedDatabaseAdapter;
import com.gettingmobile.google.reader.db.ItemDatabaseAdapter;
import com.gettingmobile.google.reader.db.TagDatabaseAdapter;

import java.util.*;

public final class HomeActivity extends ElementListActivity implements
        AdapterView.OnItemClickListener, OnFolderEdgeButtonClickListener, SyncListener {
	private final TagDatabaseAdapter labelAdapter = new TagDatabaseAdapter();
    private final FeedDatabaseAdapter feedAdapter = new FeedDatabaseAdapter();
	private final ItemDatabaseAdapter itemAdapter = new ItemDatabaseAdapter();
    private final HomeListAdapter listAdapter = new HomeListAdapter(this);
    private FullSyncAction fullSyncAction = null;
    private PushSyncAction pushSyncAction = null;
	
	public HomeActivity() {
        super(R.integer.tip_group_tag_list);

		final TagListInflator tagListInflator = new TagListInflator();
		setViewLoader(tagListInflator);
		setViewUpdater(tagListInflator);
	}

    @Override
	protected ListView getListView() {
		return (ListView) findViewById(R.id.tags);
	}

    @Override
    protected boolean showActionBarIconAsUp() {
        return false;
    }

    /*
     * life cycle management
     */

	@Override
    public void onCreate() {
        setContentView(R.layout.tag_list);

        super.onCreate();

		/*
		 * handle click events
		 */
        final ListView listView = getListView();
        listView.setOnItemClickListener(this);
        registerForContextMenu(listView);

        listAdapter.init(this);
        listView.setAdapter(listAdapter);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        promptForAccount();
    }

    @Override
    protected void onStart() {
        super.onStart();

        getApp().getSyncService().addListener(this);
    }

    @Override
    protected void onResume() {
        if (!showWelcomeTips()) {
            if (!promptForAccount()) {
                if (!ChangelogController.automaticallyShowChangelogIfApplicable(getApp(), this)) {
                    RatingRequestDialog.showIfApplicable(this);
                }
            }
        }

        super.onResume();
    }

    @Override
    protected void onStop() {
        getApp().getSyncService().removeListener(this);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        fullSyncAction.onDestroy();
        pushSyncAction.onDestroy();

        super.onDestroy();
    }

    @Override
    protected void closeCursor() {
        listAdapter.close();
    }

    /*
     * helpers
     */

    private boolean showWelcomeTips() {
        return TipDialogHandler.start(getApp(), this, getApp().getTipManager(), R.integer.tip_group_welcome, true);
    }
    
    private boolean promptForAccount() {
        final AccountHandler accountHandler = getApp().getAccountHandler();
        if (!accountHandler.hasAccount() && !accountHandler.isPromptShowing()) {
            accountHandler.promptAccount(this, new LoginCallback() {
                @Override
                public void onLoginStarted() {
                    showWaitDialog();
                }

                @Override
                public void onLoginFinished(Throwable error) {
                    dismissWaitDialog();
                    if (error != null) {
                        DialogFactory.showErrorDialog(HomeActivity.this,
                                R.string.login_title, getString(R.string.login_failed));
                    }
                }
            });
            return true;
        } else {
            return false;
        }
    }

    protected void openItemList(Tag tag) {
        startActivity(ItemListActivity.class, tag);
    }

    protected void openFeedList(Tag tag) {
        startActivity(FeedListActivity.class, tag);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final int viewId = parent.getId();
        if (viewId == R.id.tags) {
            final SortedElement element = getItem(position);
            if (element != null && element instanceof Tag) {
                onTagClick((Tag) element);
            } else if (element != null && element instanceof Feed) {
                onFeedClick((Feed) element);
            }
        }
    }

	public void onTagClick(Tag tag) {
		if (tag.isFeedFolder() && !getApp().getSettings().folderClickOpensItemList()) {
            openFeedList(tag);
        } else {
            openItemList(tag);
        }
    }

	@Override
	public void onFolderEdgeButtonClick(Tag tag) {
        if (getApp().getSettings().folderClickOpensItemList()) {
            openFeedList(tag);
        } else {
            openItemList(tag);
        }
	}

    public void onFeedClick(Feed feed) {
        startActivity(ItemListActivity.class, feed);
    }

    protected List<Tag> getSpecialTags(SQLiteDatabase db) {
		final List<Tag> specialTags = new ArrayList<Tag>();

        if (getApp().getSettings().shouldShowStarredTag()) {
            final Tag starred = new Tag();
            starred.setId(ItemState.STARRED.getId());
            starred.setTitle(getString(R.string.tag_starred));
            starred.setUnreadCount(itemAdapter.readUnreadCountByLabel(db, starred.getId()));
            starred.setKey(-1);
            specialTags.add(starred);
        }

        if (getApp().getSettings().shouldShowReadListTag()) {
            final Tag readList = new Tag();
            readList.setId(getApp().getSettings().getLabelReadListId());
            readList.setUnreadCount(itemAdapter.readUnreadCountByLabel(db, readList.getId()));
            readList.setKey(-2);
            specialTags.add(readList);
        }

        if (getApp().getSettings().shouldShowAllItemsTag()) {
            final Tag allItemsTag = new Tag();
            allItemsTag.setId(ItemState.READING_LIST.getId());
            allItemsTag.setTitle(getString(R.string.tag_all));
            allItemsTag.setUnreadCount(itemAdapter.readUnreadCount(db));
            allItemsTag.setFeedFolder(true);
            allItemsTag.setKey(-4);
            specialTags.add(allItemsTag);
        }

		return specialTags;
	}

    /*
     * action handling
     */

    @Override
    protected void onRegisterActions() {
        super.onRegisterActions();
        registerAction(R.id.menu_sync_content, new ContentDownloadAction());

        fullSyncAction = registerAction(R.id.menu_sync_full, FullSyncAction.class);
        pushSyncAction = registerAction(R.id.menu_sync_push, PushSyncAction.class);
    }

    protected int getItemIndex(ContextMenu.ContextMenuInfo menuInfo) {
        return (menuInfo instanceof ListView.AdapterContextMenuInfo) ?
                ((ListView.AdapterContextMenuInfo) menuInfo).position : -1;
    }

    protected SortedElement getItem(int itemIndex) {
        SortedElement element = null;
        if (itemIndex >= 0 && itemIndex < listAdapter.getCount()) {
            final ListItem li = (ListItem) listAdapter.getItem(itemIndex);
            if (li != null && li.getItem() != null) {
                element = (SortedElement) li.getItem();
            }
        }
        return element;
    }

    protected ContextMenu inflateContextMenu(ContextMenu menu, int menuId, int titleId) {
        getMenuInflater().inflate(menuId, menu);
        menu.setHeaderTitle(titleId);
        return menu;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        if (v.getId() == R.id.tags) {
            final SortedElement element = getItem(getItemIndex(menuInfo));
            if (element instanceof Tag) {
                final Tag tag = (Tag) element;
                if ((tag.isUserLabel() && !tag.isFeedFolder()) || ItemState.STARRED.getId().equals(tag.getId())) {
                    inflateContextMenu(menu, R.menu.tag_context, R.string.contextmenu_tag);
                    menu.findItem(R.id.menu_tag_preferences).setIntent(settingsIntentFactory.
                            createElementSettingsIntent(element.getId(), element.getTitle()));
                }
            } else if (element instanceof Feed) {
                inflateContextMenu(menu, R.menu.feed_context, R.string.contextmenu_feed);
                menu.findItem(R.id.menu_feed_preferences).setIntent(settingsIntentFactory.
                        createElementSettingsIntent(element.getId(), element.getTitle()));
            }
        }
    }

	/*
	 * event handling
	 */
	
	@Override
	protected int getOptionsMenuResourceId() {
		return R.menu.home_options;
	}

    @Override
    protected Class<? extends SettingsHandler> getViewSettingsHandlerClass() {
        return TagListSettingsHandler.class;
    }

    @Override
    protected void onShowedNoContentPanel() {
        super.onShowedNoContentPanel();

        /*
         * adjust the no content message
         */
        if (getHideRead()) {
            /*
             * register action to show read items
             */
            setNoContentMsg(R.string.no_content_show_read);
            registerAction(R.id.no_content, new AbstractAction<Application>() {
                @Override
                public boolean onFired(ActionContext<? extends Application> applicationActionContext) {
                    setHideRead(false);
                    return true;
                }
            });
        } else {
            /*
             * register action to start sync
             */
            setNoContentMsg(R.string.no_content_sync);
            registerAction(R.id.no_content, new FullSyncAction(getApp().getSyncService()));
        }
    }

    /*
	 * mark read handling
	 */

	@Override
	protected boolean requiresMarkReadConfirmationDialog() {
		return getApp().getSettings().confirmMarkAllReadInTagList();
	}

    @Override
    protected void markAllRead(SQLiteDatabase db) {
        itemAdapter.markAllRead(db);
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

    /*
     * sync handling
     */

    @Override
    public void onSyncStarted() {
        // nothing to be done
    }

    @Override
    public void onSyncFinished(boolean fullSync, Throwable error) {
        if (fullSync) {
            updateView();
        } else {
            /*
             * only update sync status header
             */
            final ListView listView = getListView();
            if (listView.getFirstVisiblePosition() == 0) {
                final View syncStatusView = listView.getChildAt(0);
                if (syncStatusView != null) {
                    final HomeListAdapter adapter = (HomeListAdapter) getListView().getAdapter();
                    if (adapter != null && !adapter.isEmpty() &&
                            adapter.getItemViewTypeId(0) == HomeListAdapter.VIEW_TYPE_SYNC_STATUS) {
                        final SyncStatusHeaderViewType syncStatusHeaderViewType =
                                (SyncStatusHeaderViewType) adapter.getViewTypeById(HomeListAdapter.VIEW_TYPE_SYNC_STATUS);
                        syncStatusHeaderViewType.updateView(syncStatusView);
                    }
                }
            }
        }
    }

    /*
     * inner classes
     */

    class DragAndDropOrderComparator implements Comparator<SortedElement> {
        @Override
        public int compare(SortedElement o1, SortedElement o2) {
            int result = o1.getRootSortOrder() - o2.getRootSortOrder();
            if (result == 0) {
                result = o1.getTitle().compareToIgnoreCase(o2.getTitle());
            }
            return result;
        }
    }

    /**
     * Mimics alphabetical sorting of google reader. Type order is folders, feeds, tags.
     */
    final class AlphabeticalComparator implements Comparator<SortedElement> {
        private int getElementTypeOrder(SortedElement o) {
            if (o instanceof Tag) {
                final Tag t = (Tag) o;
                return t.isFeedFolder() ? 0 : 2;
            } else {
                return 1;
            }
        }

        @Override
        public int compare(SortedElement o1, SortedElement o2) {
            int result = getElementTypeOrder(o1) - getElementTypeOrder(o2);
            if (result == 0) {
                result = o1.getTitle().compareToIgnoreCase(o2.getTitle());
            }
            return result;
        }
    }

	final class TagListInflator extends ViewOperation<List<HomeListItem>> {
        protected List<HomeListItem> createItemList(Collection<? extends SortedElement> elements) {
            final List<HomeListItem> items = new ArrayList<HomeListItem>(elements.size());
            for (SortedElement e : elements) {
                items.add(new HomeListItem(e));
            }
            return items;
        }
        
        @Override
        protected void onPreFetchData() {
            super.onPreFetchData();
            final SyncService syncService = getApp().getSyncService().getService();
            if (syncService != null) {
                syncService.postProcessSyncIfRequired();
            }
        }

        @Override
		protected List<HomeListItem> fetchData(SQLiteDatabase readOnlyDb) {
            /*
             * read all tags and feeds
             */
            final List<Tag> tags = getHideRead() ?
                    labelAdapter.readUnreadUserLabels(readOnlyDb) : labelAdapter.readUserLabels(readOnlyDb);
            final List<Feed> feeds = getHideRead() ? 
                    feedAdapter.readUnreadWithoutTag(readOnlyDb) : feedAdapter.readWithoutTag(readOnlyDb);
            if (tags.isEmpty() && feeds.isEmpty()) {
                return new ArrayList<HomeListItem>(0);
            }

            /*
             * filter out special tags
             */
			final SortedSet<SortedElement> sortedElements = new TreeSet<SortedElement>(
                    getApp().getSettings().sortByDragAndDropOrder() ?
                            new DragAndDropOrderComparator() : new AlphabeticalComparator());
            sortedElements.addAll(TagFilter.filterSpecialTags(getApp().getSettings(), tags));

            /*
             * add all root feeds
             */
            sortedElements.addAll(feeds);

            /*
             * add special tags
             */
            final List<Tag> specialTags = getSpecialTags(readOnlyDb);
            final List<HomeListItem> items =
                    new ArrayList<HomeListItem>(2 + sortedElements.size() + specialTags.size());
            if (getApp().getSettings().shouldShowSyncStatus()) {
                items.add(new HomeListItem(HomeListAdapter.VIEW_TYPE_SYNC_STATUS, -5));
            }
            items.addAll(createItemList(specialTags));
            if (getApp().getSettings().shouldShowStarredTag() ||
                    getApp().getSettings().shouldShowReadListTag() ||
                    getApp().getSettings().shouldShowAllItemsTag()) {
                items.add(new HomeListItem(HomeListAdapter.VIEW_TYPE_SUBSCRIPTIONS_HEADER, -6));
            }
            items.addAll(createItemList(sortedElements));

			return items;
		}

		@Override
		protected boolean updateView(List<HomeListItem> items) {
            listAdapter.changeCursor(new HomeListItemCursor(items));
			return !items.isEmpty();
		}		
	}
}