package com.gettingmobile.goodnews.itemview;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;
import android.view.*;
import android.widget.Toast;
import com.actionbarsherlock.view.Menu;
import com.gettingmobile.android.app.actions.Action;
import com.gettingmobile.android.app.actions.ActionContext;
import com.gettingmobile.android.app.settings.AbstractSettings;
import com.gettingmobile.android.app.settings.OnSettingChangeListener;
import com.gettingmobile.android.view.NookTouchKeyCodes;
import com.gettingmobile.goodnews.Activity;
import com.gettingmobile.goodnews.Application;
import com.gettingmobile.goodnews.R;
import com.gettingmobile.goodnews.settings.ItemViewSettingsHandler;
import com.gettingmobile.goodnews.settings.SettingsHandler;
import com.gettingmobile.google.reader.Item;

public final class ItemViewActivity extends Activity implements BrowseHistoryListener {
    private static final String LOG_TAG = "goodnews.ItemViewActivity";
	public static final String EXTRA_KEY_ELEMENT_KEY_LIST = EXTRA_KEY_BASE + "ELEMENT_KEY_LIST";
    private static final int NORMAL_SCREEN_VIEW_TIMEOUT = 6000;

    private GestureDetector gestureDetector;
    private int currentItemIndex = -1;
    private final ItemUrlSharer itemUrlSharer;
    private final MessageHandler msgHandler = new MessageHandler();
    private ItemPagerAdapter pagerAdapter = null;
    private ItemViewContentPager pager = null;
    private ItemViewContentFragment currentContentFragment = null;

	public ItemViewActivity() {
        super(R.integer.tip_group_item_view);
        itemUrlSharer = new ActionProviderItemUrlSharer(this);
	}

    //
	// life cycle management
	//
	
	@Override
	public void onCreate() {
        Log.i(LOG_TAG, "starting general initialization");
        super.onCreate();
        requestWindowFeature(com.actionbarsherlock.view.Window.FEATURE_PROGRESS);
		setContentView(R.layout.item_view);
        setTitle(getIntentElementTitle());

        pagerAdapter = new ItemPagerAdapter(getSupportFragmentManager(), getIntentElementKeyList());
        pager = (ItemViewContentPager) findViewById(R.id.content);
        pager.setSettings(getApp().getSettings());
        pager.setAdapter(pagerAdapter);
        showItem(pagerAdapter.getItemIndex(getIntentElementKey()), false);

        //
        // init gesture detector
        //
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                return onTap();
            }
        });
        getApp().getSettings().registerChangeListener("item_view_full_screen", new OnSettingChangeListener() {
            @Override
            public void onSettingChanged(AbstractSettings settings, SharedPreferences sharedPreferences, String s) {
                invalidateOptionsMenu();
            }
        });

		//
		// done
		//
        Log.i(LOG_TAG, "done with onCreate");
	}

    @Override
    protected boolean shouldUpdateOnResume() {
        return false;
    }

    @Override
    protected void onResume() {
        getWindow().setFlags(getApp().getSettings().keepItemScreenOn() ?
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON : 0, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        showFullScreenIfEnabled(true);
        super.onResume();
    }

    //
    // intent handling and helpers
    //

    public static Intent createItemViewIntent(Context ctx, String itemListTitle, long itemKey, long[] itemKeyList) {
        final Intent intent = new Intent(ctx, ItemViewActivity.class);
        if (itemListTitle != null)
            intent.putExtra(EXTRA_KEY_ELEMENT_TITLE, itemListTitle);
        intent.putExtra(EXTRA_KEY_ELEMENT_KEY, itemKey);
        if (itemKeyList == null) {
            itemKeyList = new long[] { itemKey };
        }
        intent.putExtra(EXTRA_KEY_ELEMENT_KEY_LIST, itemKeyList);
        return intent;
    }
	
	public static void startItemViewActivity(Activity ctx, String itemListTitle, long itemKey, long[] itemKeyList) {
        Log.i(LOG_TAG, "Triggering item view for item " + itemKey);
		ctx.startActivity(createItemViewIntent(ctx, itemListTitle, itemKey, itemKeyList));
	}
	
	protected long[] getIntentElementKeyList() {
		final long[] keys =  getIntent().getExtras().getLongArray(EXTRA_KEY_ELEMENT_KEY_LIST);
        return keys != null ? keys : new long[0];
	}
	
	protected boolean showItem(int itemIndex, boolean smoothScroll) {
        if (itemIndex >= 0 && itemIndex < pagerAdapter.getCount()) {
            pager.setCurrentItem(itemIndex, smoothScroll);
            setCurrentItemIndex(itemIndex);
            return true;
        } else {
            return false;
        }
	}

    protected boolean hasPrevItem() {
        return currentItemIndex > 0;
    }
	
	protected void showPrevItem() {
        showFullScreenIfEnabled();
        if (!showItem(currentItemIndex - 1, true))
            noMoreItems();
	}

    protected boolean hasNextItem() {
        return currentItemIndex < pagerAdapter.getCount();
    }

	protected void showNextItem() {
        showFullScreenIfEnabled();
        if (!showItem(currentItemIndex + 1, true))
            noMoreItems();
	}

    protected void noMoreItems() {
        Toast.makeText(this, R.string.toast_no_more_items, Toast.LENGTH_SHORT).show();
    }

	//
	// event handling
	//

    protected boolean useVolumeKeysForItemNavigation() {
        return getApp().getSettings().useVolumeKeysForItemNavigation();
    }

    private boolean handleVolumeKeys(KeyEvent event) {
        if (useVolumeKeysForItemNavigation()) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_VOLUME_UP:
                    if (event.getAction() == KeyEvent.ACTION_DOWN)
                        showPrevItem();
                    return true;
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                    if (event.getAction() == KeyEvent.ACTION_DOWN)
                        showNextItem();
                    return true;
            }
        }
        return false;
    }

    private boolean handleStandardKeyOverriding(KeyEvent event) {
        return (currentContentFragment != null && currentContentFragment.handleStandardKeyOverriding(event)) || handleVolumeKeys(event);
    }

    private boolean handleNookTouchKeys(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {
                case NookTouchKeyCodes.LEFT_NEXT_PAGE:
                case NookTouchKeyCodes.RIGHT_NEXT_PAGE:
                    showNextItem();
                    return true;
                case NookTouchKeyCodes.LEFT_PREV_PAGE:
                case NookTouchKeyCodes.RIGHT_PREV_PAGE:
                    showPrevItem();
                    return true;
            }
        }
        return false;
    }

    private boolean handleAdditionalKeys(KeyEvent event) {
        return (currentContentFragment != null && currentContentFragment.handleAdditionalKeys(event)) || handleNookTouchKeys(event);
    }

    @Override
	public boolean dispatchKeyEvent(KeyEvent event) {
        return handleStandardKeyOverriding(event) || super.dispatchKeyEvent(event) || handleAdditionalKeys(event);
	}

    protected Rect getViewScreenRect(View view) {
        final int[] viewPos = new int[2];
        view.getLocationOnScreen(viewPos);
        return new Rect(viewPos[0], viewPos[1], viewPos[0] + view.getWidth(), viewPos[1] + view.getHeight());
    }

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
        gestureDetector.onTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
	}
    
    private boolean onTap() {
        if (isFullScreenMode()) {
            msgHandler.sendShowFullScreenModeIfEnabled(false);
        } else {
            msgHandler.sendShowFullScreenModeIfEnabled(true);
        }
        return true;
    }
	
	//
	// menu handling
	//

    @Override
    protected void onRegisterActions() {
        super.onRegisterActions();

        registerAction(R.id.menu_item_share, new ItemUrlSharerAction(itemUrlSharer));
        registerAction(R.id.menu_item_labels, new EditTagsAction());
        registerAction(R.id.menu_item_browser, new OpenInBrowserAction());
        registerAction(R.id.menu_item_load, new LoadContentAction());
        registerAction(R.id.menu_item_prev, new ShowPrevItemAction());
        registerAction(R.id.menu_item_next, new ShowNextItemAction());
        registerAction(R.id.menu_full_screen, new FullScreenAction());
        registerAction(R.id.menu_full_screen_disable, new DisableFullScreenAction());

        registerAction(R.id.menu_feed_preferences, ItemFeedSettingsAction.class);
    }

    @Override
	protected int getOptionsMenuResourceId() {
		return R.menu.item_options;
	}

    @Override
    protected Class<? extends SettingsHandler> getViewSettingsHandlerClass() {
        return ItemViewSettingsHandler.class;
    }

    @Override
	public boolean onPrepareOptionsMenu(Menu menu) {
        itemUrlSharer.onPrepareMenu(menu.findItem(R.id.menu_item_share));
        prepareActionItem(menu, R.id.menu_item_share, "share");
        prepareActionItem(menu, R.id.menu_item_labels, "labels");
        prepareActionItem(menu, R.id.menu_item_browser, "browser");
        prepareActionItem(menu, R.id.menu_item_load, "load");
        prepareActionItem(menu, R.id.menu_item_prev, "prev");
        prepareActionItem(menu, R.id.menu_item_next, "next");
        prepareActionItem(menu, R.id.menu_full_screen, "full_screen");
        prepareActionItem(menu, R.id.menu_full_screen_disable, "full_screen");
		return super.onPrepareOptionsMenu(menu);
	}	

    //
    // controls
    //

    private void prepareActionItem(Menu menu, int resId, String settingsKey) {
        menu.findItem(resId).setShowAsAction(getApp().getSettings().getActionButtonVisibilityFlags(settingsKey));
    }

    protected boolean isFullScreenMode() {
        return !getSupportActionBar().isShowing();
    }

    private void showFullScreenIfEnabled() {
        showFullScreenIfEnabled(true);
    }

    private void showFullScreenIfEnabled(boolean fullScreen) {
        if (getApp().getSettings().isArticleFullScreenEnabled()) {
            showFullScreen(fullScreen);

            //
            // schedule full screen after some delay
            //
            if (!fullScreen) {
                msgHandler.sendDelayedShowFullScreenModeIfEnabled(true);
            }
        } else {
            showFullScreen(false);
        }
    }

    private void showFullScreen(boolean fullScreen) {
        if (fullScreen) {
            getSupportActionBar().hide();
        } else {
            getSupportActionBar().show();
        }
    }

    //
    // actions
    //
    
    private abstract class ContentFragmentAction implements Action<Application> {
        @Override
        public int getState(ActionContext<? extends Application> context) {
            return currentContentFragment != null ? getAction(currentContentFragment).getState(context) : DISABLED;
        }

        @Override
        public boolean onFired(ActionContext<? extends Application> context) {
            return currentContentFragment != null && getAction(currentContentFragment).onFired(context);
        }
        
        protected abstract Action<Application> getAction(ItemViewContentFragment fragment);
    }

    private final class EditTagsAction extends ContentFragmentAction {
        @Override
        protected Action<Application> getAction(ItemViewContentFragment fragment) {
            return fragment.editTagsAction;
        }
    }

    private final class LoadContentAction extends ContentFragmentAction {
        @Override
        protected Action<Application> getAction(ItemViewContentFragment fragment) {
            return fragment.loadContentAction;
        }
    }

    private final class OpenInBrowserAction extends ContentFragmentAction {
        @Override
        protected Action<Application> getAction(ItemViewContentFragment fragment) {
            return fragment.openInBrowserAction;
        }
    }

    final class ShowNextItemAction implements Action<Application> {
        @Override
        public int getState(ActionContext<? extends Application> actionContext) {
            return hasNextItem() ? ENABLED : DISABLED;
        }

        @Override
        public boolean onFired(ActionContext<? extends Application> actionContext) {
            showNextItem();
            return true;
        }
    }

    final class ShowPrevItemAction implements Action<Application> {
        @Override
        public int getState(ActionContext<? extends Application> actionContext) {
            return hasPrevItem() ? ENABLED : DISABLED;
        }

        @Override
        public boolean onFired(ActionContext<? extends Application> actionContext) {
            showPrevItem();
            return true;
        }
    }

    final class FullScreenAction implements Action<Application> {
        @Override
        public int getState(ActionContext<? extends Application> context) {
            return context.getApp().getSettings().isArticleFullScreenEnabled() ? GONE : ENABLED;
        }

        @Override
        public boolean onFired(ActionContext<? extends Application> context) {
            getApp().getSettings().setArticleFullScreenEnabled(true);
            showFullScreenIfEnabled(true);
            return true;
        }
    }

    final class DisableFullScreenAction implements Action<Application> {
        @Override
        public int getState(ActionContext<? extends Application> context) {
            return context.getApp().getSettings().isArticleFullScreenEnabled() ? ENABLED : GONE;
        }

        @Override
        public boolean onFired(ActionContext<? extends Application> context) {
            getApp().getSettings().setArticleFullScreenEnabled(false);
            showFullScreenIfEnabled(false);
            return true;
        }
    }

    //
    // event handling
    //

    final class MessageHandler extends Handler {
        private static final int WHAT = 1;

        protected Message createMessage(boolean visible) {
            return obtainMessage(WHAT, visible ? 1 : 0, 0);
        }

        public void sendShowFullScreenModeIfEnabled(boolean enable) {
            if (getApp().getSettings().isArticleFullScreenEnabled()) {
                removeMessages(WHAT);
                sendMessage(createMessage(enable));
            }
        }

        public void sendDelayedShowFullScreenModeIfEnabled(boolean enable) {
            if (getApp().getSettings().isArticleFullScreenEnabled()) {
                removeMessages(WHAT);
                sendMessageDelayed(createMessage(enable), NORMAL_SCREEN_VIEW_TIMEOUT);
            }
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == WHAT) {
                showFullScreenIfEnabled(msg.arg1 > 0);
            }
            super.handleMessage(msg);
        }
    }

    @Override
    public void onBrowseHistoryChanged(BrowseHistory history) {
        invalidateOptionsMenu();
        showUnshownTipsIfApplicable();
    }

    //
    // item handling
    //

    public Item getItem() {
        return currentContentFragment != null ? currentContentFragment.getItem() : null;
    }
    
    public void updateLabels() {
        currentContentFragment.updateLabels();
    }

    private void setCurrentItemIndex(int itemIndex) {
        if (itemIndex != currentItemIndex) {
            currentItemIndex = itemIndex;
            setIntentElementKey(pagerAdapter.itemKeys[itemIndex]);
            getSupportActionBar().setSubtitle(Integer.toString(itemIndex + 1) + " / " + pagerAdapter.getCount());
        }
    }

    private void setCurrentContentFragment(ItemViewContentFragment currentContentFragment) {
        if (this.currentContentFragment != currentContentFragment) {
            if (this.currentContentFragment != null) {
                this.currentContentFragment.unsetCurrent();
            }
            this.currentContentFragment = currentContentFragment;
            if (currentContentFragment != null) {
                currentContentFragment.setCurrent(itemUrlSharer);
            } else {
            }
            invalidateOptionsMenu();
        }
    }

    private class ItemPagerAdapter extends FragmentStatePagerAdapter {
        protected final long[] itemKeys;

        public ItemPagerAdapter(FragmentManager fragmentManager, long[] itemKeys) {
            super(fragmentManager);
            this.itemKeys = itemKeys;
        }
        
        public int getItemIndex(long key) {
            for (int i = 0; i < itemKeys.length; ++i) {
                if (itemKeys[i] == key)
                    return i;
            }
            return -1;
        }

        @Override
        public Fragment getItem(int position) {
            return ItemViewContentFragment.newInstance(itemKeys[position]);
        }

        @Override
        public int getCount() {
            return itemKeys.length;
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);

            Log.d(LOG_TAG, "setPrimaryItem " + position);
            setCurrentItemIndex(position);
            setCurrentContentFragment((ItemViewContentFragment) object);
        }
    }
}