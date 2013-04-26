package com.gettingmobile.goodnews.itemview;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.*;
import com.gettingmobile.android.app.actions.AbstractAction;
import com.gettingmobile.android.app.actions.Action;
import com.gettingmobile.android.app.actions.ActionContext;
import com.gettingmobile.android.app.actions.MarketAction;
import com.gettingmobile.android.app.settings.AbstractSettings;
import com.gettingmobile.android.app.settings.OnSettingChangeListener;
import com.gettingmobile.android.view.NookTouchKeyCodes;
import com.gettingmobile.android.widget.OnScrollChangedListener;
import com.gettingmobile.android.widget.WebViewWithTitle;
import com.gettingmobile.goodnews.Activity;
import com.gettingmobile.goodnews.Application;
import com.gettingmobile.goodnews.R;
import com.gettingmobile.goodnews.settings.ItemDisplayType;
import com.gettingmobile.goodnews.settings.ItemTitleClickAction;
import com.gettingmobile.goodnews.util.*;
import com.gettingmobile.goodnews.widget.MessageBar;
import com.gettingmobile.google.reader.ElementId;
import com.gettingmobile.google.reader.Item;
import com.gettingmobile.google.reader.ItemState;
import com.gettingmobile.google.reader.db.ItemDatabaseAdapter;
import com.gettingmobile.google.reader.db.ItemTagChangeDatabaseAdapter;
import com.gettingmobile.google.reader.db.ItemTagChangeListener;
import com.gettingmobile.net.mobilizer.MobilizerImplementation;
import com.gettingmobile.net.mobilizer.NullUrlMobilizer;
import com.gettingmobile.net.mobilizer.UrlMobilizer;
import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockFragment;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Set;

public final class ItemViewContentFragment extends RoboSherlockFragment
        implements CompoundButton.OnCheckedChangeListener, View.OnClickListener, ItemTagChangeListener {
    private static final String LOG_TAG = "goodnews.ItemViewFragment";
    private static final String URL_EMPTY = "about:blank";
    protected static final String EXTRA_KEY_ITEM_KEY = Activity.EXTRA_KEY_BASE + "ITEM_KEY";

    private final ItemDatabaseAdapter itemAdapter = new ItemDatabaseAdapter();
    private final ItemTagChangeDatabaseAdapter itemTagChangeAdapter = new ItemTagChangeDatabaseAdapter();

    private long itemKey = -1;
    private Item item = null;
    private boolean current = false;
    private ItemUrlSharer pageUrlSharer = null;
    private String pageUrl = null;
    private String pageTitle = null;
    private final BrowseHistory browseHistory = new BrowseHistory();

    private ProgressBar progressBar = null;
    private Animation progressBarFadeOutAnimation = null;
    private TextView titleTextView = null;
	private TextView feedTitle = null;
	private ToggleButton readButton = null;
	private ToggleButton readListButton = null;
	private ToggleButton starButton = null;
    private MessageBar msgBar = null;
	private TextView labelView = null;
	private TextView authorView = null;
	private TextView timestampView = null;
    private ItemTimestampFormat timestampFormat = null;
	private WebViewWithTitle contentView = null;
    
    private boolean externalContent = false;
    private ContentLoadState contentLoadState = ContentLoadState.FORBIDDEN;
    
    private int defaultFontSize = 0;
    private int defaultFixedFontSize = 0;
    private final OnWebTextSizeChangeListener webTextSizeChangeListener = new OnWebTextSizeChangeListener();

    /*
     * initialization
     */

    static ItemViewContentFragment newInstance(long itemKey) {
        final ItemViewContentFragment f = new ItemViewContentFragment();
        final Bundle args = new Bundle();
        args.putLong(EXTRA_KEY_ITEM_KEY, itemKey);
        f.setArguments(args);
        return f;
    }
    
    public ItemViewContentFragment() {
        itemTagChangeAdapter.addListener(this);
    }

    /*
     * current handling
     */
    
    public void setCurrent(ItemUrlSharer pageUrlSharer) {
        current = true;
        this.pageUrlSharer = pageUrlSharer;
        implicitlyUpdateItemTagsIfApplicable();

        allowContentLoad();
    }

    public void unsetCurrent() {
        current =false;
        pageUrlSharer = null;
    }
    
    public Item getItem() {
        return item;
    }

    /*
	 * life cycle management
	 */

    private static boolean isAlive(Activity activity) {
        return activity != null && !activity.isFinishing();
    }

    private boolean isAlive() {
        return isAlive(getAppActivity());
    }

    @Override
    public void onAttach(android.app.Activity activity) {
        browseHistory.setListener((BrowseHistoryListener) activity);
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            itemKey = getArguments().getLong(EXTRA_KEY_ITEM_KEY);
        }
        new ItemLoader(this, getApp()).load(itemKey);
    }

    private void initProgressBar(ViewGroup root) {
        progressBar = (ProgressBar) root.findViewById(R.id.item_load_progress);
        progressBar.setMax(100);

        progressBarFadeOutAnimation = AnimationUtils.loadAnimation(getAppActivity(), android.R.anim.fade_out);
        progressBarFadeOutAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // not interested
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (isAlive()) {
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // not interested
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(LOG_TAG, "starting general initialization");

        final ViewGroup root = (ViewGroup) inflater.inflate(R.layout.item_view_content_fragment, container, false);

        initProgressBar(root);

        /*
         * init web view
         */
        Log.i(LOG_TAG, "starting web view initialization");
        final LinearLayout contentViewContainer = (LinearLayout) root.findViewById(R.id.item_content_container);
        //noinspection deprecation
        WebView.enablePlatformNotifications();
        contentView = WebViewWithTitle.createInstance(getAppActivity());
        contentViewContainer.addView(contentView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));
        contentView.setTitle(R.layout.item_title);

        registerForContextMenu(contentView);
        ItemWebViewInitializerFactory.createItemWebViewInitializer().initWebView(contentView);
        contentView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                setLoadProgress(progress);
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                if (isAlive()) {
                    setPageInfo(getCurrentPageUrl(), title);
                }
            }
        });
        contentView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (isAlive() && MarketAction.isMarketUrl(url)) {
                    new MarketAction(url).onFired(getAppActivity());
                    return true;
                } else {
                    return false;
                }
            }

            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                if (isAlive()) {
                    msgBar.showWarning(description);
                }
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                Log.d(LOG_TAG, "Loading page with URL " + url);
                if (isAlive()) {
                    applyWebTextSize();
                }
            }
        });
        contentView.setOnTouchListener(new LinkClickDetector());
        contentView.setOnScrollChangedListener(new OnScrollChangedListener() {
            @Override
            public void onScrollChanged(int scrollX, int scrollY) {
                final ContentHistoryEntry historyEntry = (ContentHistoryEntry) browseHistory.getCurrent();
                if (historyEntry != null) {
                    historyEntry.rememberScroll(scrollX, scrollY);
                }
            }
        });
        setContentViewThemeParameters();

        defaultFontSize = contentView.getSettings().getDefaultFontSize();
        defaultFixedFontSize = contentView.getSettings().getDefaultFixedFontSize();
        getApp().getSettings().registerChangeListener("item_view_text_size", webTextSizeChangeListener);

        if (savedInstanceState != null) {
            contentView.restoreState(savedInstanceState);
        }
        clearContent();

		/*
		 * init title
		 */
        final View titleView = root.findViewById(R.id.title);
        titleView.setOnClickListener(this);
        titleTextView = (TextView) root.findViewById(R.id.title_text);
		feedTitle = (TextView) root.findViewById(R.id.feed_title);

		readButton = (ToggleButton) root.findViewById(R.id.button_read);
		readButton.setOnCheckedChangeListener(this);

		readListButton = (ToggleButton) root.findViewById(R.id.button_readlist);
		readListButton.setOnCheckedChangeListener(this);

		starButton = (ToggleButton) root.findViewById(R.id.button_starred);
		starButton.setOnCheckedChangeListener(this);

		labelView = (TextView) root.findViewById(R.id.labels);
		labelView.setOnClickListener(this);

		/*
		 * init other properties
		 */
        msgBar = new MessageBar(getAppActivity());
        timestampFormat = new ItemTimestampFormat(getApp(), true);

		/*
		 * init item information
		 */
		authorView = (TextView) root.findViewById(R.id.author);
		timestampView = (TextView) root.findViewById(R.id.timestamp);

		/*
		 * done
		 */
        Log.i(LOG_TAG, "done with onCreate");
        return root;
	}

    private void setContentViewThemeParameters() {
        contentView.addJavascriptInterface(
                getApp().getThemeUtil().getThemeColorWebString(getActivity(), R.attr.articleBackgroundColor),
                "themeColorBackground");
        contentView.addJavascriptInterface(
                getApp().getThemeUtil().getThemeColorWebString(getActivity(), R.attr.themeTextColorPrimary),
                "themeColorText");

        final String baseTheme;
        switch (getApp().getSettings().getTheme()) {
            case BLACK:
            case DARK:
                baseTheme = "dark";
                break;
            default:
                baseTheme = "light";
        }
        contentView.addJavascriptInterface(baseTheme, "baseTheme");
        contentView.setBackgroundColor(getThemeUtil().getThemeColor(getAppActivity(), R.attr.articleBackgroundColor));
    }

    @Override
    public void onResume() {
        super.onResume();
        contentView.resume();
    }

    @Override
    public void onPause() {
        contentView.pause();
        commitTagChanges(getDb());
        super.onPause();
    }

    @Override
    public void onDestroy() {
        getApp().getSettings().unregisterChangeListener("item_view_text_size", webTextSizeChangeListener);
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d(LOG_TAG, "onSaveInstanceState");

        if (contentView != null) {
            contentView.saveState(outState);
        }
        
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.d(LOG_TAG, "onConfigurationChanged");
        super.onConfigurationChanged(newConfig);
    }
    
    //
    // action handling
    //

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (v == contentView) {
            final WebView.HitTestResult hit = contentView.getHitTestResult();
            if (hit.getType() == WebView.HitTestResult.SRC_ANCHOR_TYPE) {
                final String url = hit.getExtra();
                getActivity().getMenuInflater().inflate(R.menu.link_context, menu);
                menu.setHeaderTitle(url);
                menu.findItem(R.id.menu_link_browser).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        openInBrowser(url);
                        return true;
                    }
                });
                menu.findItem(R.id.menu_link_share).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        final ItemUrlSharer linkSharer = new ClassicItemUrlSharer(getAppActivity());
                        linkSharer.setItemInfo(url, null);
                        linkSharer.handleAction();
                        return true;
                    }
                });
            }
        } else {
            super.onCreateContextMenu(menu, v, menuInfo);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.getId() == R.id.button_read) {
            onMarkRead(isChecked);
        } else if (buttonView.getId() == R.id.button_readlist) {
            onReadList(isChecked);
        } else if (buttonView.getId() == R.id.button_starred) {
            onStar(isChecked);
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.labels) {
            showTagSelectionDialog();
        } else if (view.getId() == R.id.title) {
            onTitleClicked();
        }
    }

    public final Action<Application> editTagsAction = new AbstractAction<Application>() {
        @Override
        public boolean onFired(ActionContext<? extends Application> context) {
            showTagSelectionDialog();
            return true;
        }
    };

    public final Action<Application> openInBrowserAction = new AbstractAction<Application>() {
        @Override
        public boolean onFired(ActionContext<? extends Application> actionContext) {
            final String url = getCurrentPageUrl();
            if (url != null) {
                openInBrowser(url);
            }
            return true;
        }
    };

    public final Action<Application> loadContentAction = new Action<Application>() {
        @Override
        public int getState(ActionContext<? extends Application> actionContext) {
            return !browseHistory.canGoBack() && !browseHistory.isEmpty() &&
                    !(browseHistory.getCurrent() instanceof UrlHistoryEntry) ? ENABLED : GONE;
        }

        @Override
        public boolean onFired(ActionContext<? extends Application> actionContext) {
            loadContent();
            return true;
        }
    };

    private boolean execute(int menuResId) {
        return getAppActivity().fireAction(menuResId);
    }

    private boolean onTitleClicked() {
        final ItemTitleClickAction action = getApp().getSettings().getItemTitleClickAction();
        switch (action) {
            case LABELS:
                return execute(R.id.menu_item_labels);
            case BROWSER:
                return execute(R.id.menu_item_browser);
            case LOAD:
                return execute(R.id.menu_item_load);
            case FULL_SCREEN:
                return execute(R.id.menu_full_screen) || execute(R.id.menu_full_screen_disable);
            default:
                // nothing
                return false;
        }
    }

    private boolean useVolumeKeysForScrolling() {
        return getApp().getSettings().useVolumeKeysForArticleScrolling();
    }

    private boolean handleVolumeKeys(KeyEvent event) {
        if (useVolumeKeysForScrolling()) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                    return (event.getAction() != KeyEvent.ACTION_DOWN) || contentView.pageDown(false);
                case KeyEvent.KEYCODE_VOLUME_UP:
                    return (event.getAction() != KeyEvent.ACTION_DOWN) || contentView.pageUp(false);
            }
        }
        return false;
    }

    private boolean handleBackKey(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && browseHistory.canGoBack()) {
            if (event.getAction() == KeyEvent.ACTION_UP)
                browseHistory.goBack().show();
            return true;
        } else {
            return false;
        }
    }

    public boolean handleStandardKeyOverriding(KeyEvent event) {
        return handleVolumeKeys(event) || handleBackKey(event);
    }

    private boolean handleNookTouchKeys(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {
                case NookTouchKeyCodes.LEFT_NEXT_PAGE:
                case NookTouchKeyCodes.RIGHT_NEXT_PAGE:
                    return contentView.pageDown(false);
                case NookTouchKeyCodes.LEFT_PREV_PAGE:
                case NookTouchKeyCodes.RIGHT_PREV_PAGE:
                    return contentView.pageUp(false);
            }
        }
        return false;
    }

    public boolean handleAdditionalKeys(KeyEvent event) {
        return handleNookTouchKeys(event);
    }

	//
	// label handling
	//
	
	protected void onMarkRead(boolean read) {
        if (item != null) {
            itemTagChangeAdapter.markItemRead(item, read);
            updateLabelControls();
        }
	}
	
	protected void onReadList(boolean readList) {
		changeTag(getApp().getSettings().getLabelReadListId(), readList);
	}

	protected void onStar(boolean star) {
		changeTag(ItemState.STARRED.getId(), star);
	}

	protected void changeTag(ElementId tag, boolean add) {
        if (item != null) {
            itemTagChangeAdapter.changeItemTag(item, tag, add);
        }
	}

    protected void updateLabels() {
        if (item != null) {
            final Set<ElementId> tags = itemAdapter.readItemTags(getReadOnlyDb(), item.getKey());
            item.getTagIds().clear();
            item.getTagIds().addAll(tags);
            updateLabelControls();
        }
    }

	protected void updateLabelControls() {
        if (item != null) {
            readButton.setChecked(item.isRead());
            readListButton.setChecked(item.hasTag(getApp().getSettings().getLabelReadListId()));
            starButton.setChecked(item.hasTag(ItemState.STARRED.getId()));

            TagListViewController.setTags(labelView,
                    TagFilter.filterSpecialTagIds(getApp().getSettings(), item.getTagIds()),
                    getResources().getColor(R.color.highlight));
        }
	}

	private void showTagSelectionDialog() {
        if (item != null) {
		    new TagSelectionDialogBuilder(getActivity(), item, itemTagChangeAdapter).show();
        }
	}

    @Override
    public void onItemReadStateChanged(long itemKey, boolean read) {
        if (item != null && item.getKey() == itemKey) {
            updateLabelControls();
        }
    }

    @Override
    public void onItemTagChanged(long itemKey, ElementId tag, boolean added) {
        if (item != null && item.getKey() == itemKey) {
            updateLabelControls();
        }
    }

    //
    // url sharing
    //
    
    private void setPageInfo(String url, String title) {
        pageUrl = url;
        pageTitle = title;
        updateUrlSharer();
    }
    
    private void updateUrlSharer() {
        if (pageUrlSharer != null && isAlive()) {
            pageUrlSharer.setItemInfo(pageUrl, pageTitle);
        }
    }

    //
    // url handling
    //

    protected UrlMobilizer getUrlMobilizer() {
        return item != null ?
                getApp().getSettings().getUrlMobilizer(item.getFeedId()) :
                new NullUrlMobilizer();
    }

    protected String getItemUrl() {
        return item != null && item.getAlternate() != null && item.getAlternate().getHref().length() > 0 ?
                item.getAlternate().getHref() : null;
    }

    protected String getMobilizedUrl() {
        final String url = getItemUrl();
        return url != null ?
                getUrlMobilizer().mobilize(url, getApp().getSettings().scaleImages(item.getFeedId())) : null;
    }

    protected String getCurrentPageUrl() {
        final String url = contentView.getUrl();
        return url != null && url.length() > 0 && !URL_EMPTY.equalsIgnoreCase(url) ?
            getUrlMobilizer().unmobilize(url) : getItemUrl();
    }

    //
    // browse history
    //

    private abstract class ContentHistoryEntry implements BrowseHistoryEntry {
        private int scrollX = 0;
        private int scrollY = 0;

        public void rememberScroll(int x, int y) {
            scrollX = x;
            scrollY = y;
        }

        @Override
        public void show() {
            contentView.setInitialScroll(scrollX, scrollY);
        }
    }

    final class UrlHistoryEntry extends ContentHistoryEntry {
        private final String url;

        public UrlHistoryEntry(String url) {
            this.url = url;
        }

        @Override
        public void show() {
            super.show();
            setContentUrl(url);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            UrlHistoryEntry that = (UrlHistoryEntry) o;

            return !(url != null ? !url.equals(that.url) : that.url != null);
        }

        @Override
        public String toString() {
            return "UrlHistoryEntry{" +
                    "url='" + url + '\'' +
                    '}';
        }
    }

    final class ItemContentHistoryEntry extends ContentHistoryEntry {
        @Override
        public void show() {
            super.show();
            showContent();
        }

        @Override
        public String toString() {
            return "ItemContentHistoryEntry";
        }
    }

    final class ItemSummaryHistoryEntry extends ContentHistoryEntry {
        @Override
        public void show() {
            super.show();
            showSummary();
        }

        @Override
        public String toString() {
            return "ItemSummaryHistoryEntry";
        }
    }

    final class ShowNothingHistoryEntry extends ContentHistoryEntry {
        @Override
        public void show() {
            showNothing();
        }

        @Override
        public String toString() {
            return "ShowNothingHistoryEntry";
        }
    }
    
    //
    // font size handling
    //
    
    final class OnWebTextSizeChangeListener implements OnSettingChangeListener {
        @Override
        public void onSettingChanged(AbstractSettings settings, SharedPreferences sharedPreferences, String key) {
            applyWebTextSize();
        }
    }

    protected int getFontSize(int defaultSize, WebSettings.TextSize size) {
        final double percentage;
        switch (size) {
            case SMALLEST:
                percentage = 0.8;
                break;
            case SMALLER:
                percentage = 0.9;
                break;
            case LARGER:
                percentage = 1.1;
                break;
            case LARGEST:
                percentage = 1.2;
                break;
            default:
                percentage = 1.0;
        }
        return (int) Math.round(percentage * defaultSize);
    }

    protected void applyWebTextSize(WebSettings.TextSize size) {
        contentView.getSettings().setDefaultFontSize(getFontSize(defaultFontSize, size));
        contentView.getSettings().setDefaultFixedFontSize(getFontSize(defaultFixedFontSize, size));
    }

    protected void applyWebTextSize() {
        final MobilizerImplementation mobilizerImpl = item != null ?
                getApp().getSettings().getMobilizerImplementation(item.getFeedId()) : MobilizerImplementation.NONE;
        applyWebTextSize(externalContent && mobilizerImpl == MobilizerImplementation.NONE ?
                WebSettings.TextSize.NORMAL : getApp().getSettings().getItemViewTextSize());
    }

    //
    // content view handling
    //

	protected void showNothing() {
		setContent("");
	}

	protected void showSummary() {
		setContent(item.getSummary());
	}

	protected void showContent() {
        setContent(item.getContent());
	}

    private void addUrlToHistory(String url) {
        browseHistory.setCurrent(new UrlHistoryEntry(url));
    }

	protected void loadContent() {
        final String url = getMobilizedUrl();
        if (url != null) {
            addUrlToHistory(url);
		    setContentUrl(url);
        }
	}

    protected void openInBrowser(String url) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url)));
        } catch (ActivityNotFoundException ex) {
            Log.e(LOG_TAG, "Failed to open activity in browser", ex);
        }
    }

	protected void setContent(String content) {
        externalContent = false;
        final String baseUrl;
        if (item.hasImages()) {
            baseUrl = "file://" + item.getDirectory(getApp().getSettings().getContentStorageProvider()).getAbsolutePath() + "/";
        } else if (getApp().isInternetAvailableAndRequired()) {
            baseUrl = item.getAlternate() != null ? item.getAlternate().getHref() : null;
        } else {
            baseUrl = null;
        }

        Log.d(LOG_TAG, "Setting baseUrl to " + baseUrl);
        setContent(baseUrl, ItemTextUtil.getItemTitle(getActivity(), item), content,
                getApp().getSettings().scaleImages(item.getFeedId()));
    }
    
    protected void setContent(String baseUrl, String title, String content, boolean scaleImages) {
        final String data = HtmlBuilder.build(
                        title, content,
                        getThemeUtil().getThemeColorWebString(getActivity(), R.attr.themeTextColorPrimary),
                        getThemeUtil().getThemeColorWebString(getActivity(), R.attr.articleBackgroundColor),
                        scaleImages);
        contentView.loadDataWithBaseURL(baseUrl, data, "text/html", "utf-8", null);
    }

	protected void setContentUrl(String url) {
        externalContent = true;
		contentView.loadUrl(url);
	}

    protected void clearContent() {
        setContent(null, null, "", false);
    }

    private class LinkClickDetector extends GestureDetector.SimpleOnGestureListener implements View.OnTouchListener {
        private final GestureDetector clickDetector = new GestureDetector(getAppActivity(), this);
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            clickDetector.onTouchEvent(motionEvent);
            return false;
        }

        public boolean onSingleTapConfirmed(MotionEvent e) {
            final WebView.HitTestResult hit = contentView.getHitTestResult();
            if (hit.getType() == WebView.HitTestResult.SRC_ANCHOR_TYPE) {
                addUrlToHistory(hit.getExtra());
            }
            return false;
        }
    }

    //
    // helper
    //
    
    private Activity getAppActivity() {
        return (Activity) getSherlockActivity();
    }

    private Application getApp() {
        return getAppActivity().getApp();
    }

    private ThemeUtil getThemeUtil() {
        return getAppActivity().getThemeUtil();
    }
    
    private SQLiteDatabase getDb() {
        return getApp().getDbHelper().getDatabase();
    }
    
    private SQLiteDatabase getReadOnlyDb() {
        return getApp().getDbHelper().getReadOnlyDatabase();
    }

    //
    // item loading and persistence
    //
    
    private enum ContentLoadState {
        FORBIDDEN,
        ALLOWED,
        LOADING_OR_LOADED
    }

    private void allowContentLoad() {
        if (contentLoadState == ContentLoadState.FORBIDDEN) {
            contentLoadState = ContentLoadState.ALLOWED;
        }
        loadContentIfApplicable();
    }

    private void loadContentIfApplicable() {
        if (item != null && contentLoadState == ContentLoadState.ALLOWED) {
            final ItemDisplayType itemDisplayType = getApp().getSettings().getItemDisplayType(item);
            Log.d(LOG_TAG, "Item " + item + " has " + (item.hasContent() ? "" : "no ") + "content. ItemDisplayType: " + itemDisplayType);
            setPageInfo(item.getAlternate() != null ? item.getAlternate().getHref() : null, item.getTitle());
            browseHistory.clear();
            contentLoadState = ContentLoadState.LOADING_OR_LOADED;
            switch (itemDisplayType) {
                case SUMMARY:
                    browseHistory.setCurrent(new ItemSummaryHistoryEntry());
                    showSummary();
                    break;
                case CONTENT:
                    browseHistory.setCurrent(new ItemContentHistoryEntry());
                    showContent();
                    break;
                case LOAD:
                    loadContent();
                    break;
                default:
                    browseHistory.setCurrent(new ShowNothingHistoryEntry());
                    showNothing();
            }
        }
    }

    private void implicitlyUpdateItemTagsIfApplicable() {
        if (current && item != null) {
            //
            // mark item as read and remove it from read list
            //
            Log.i(LOG_TAG, "update item tags");
            if (getApp().getSettings().markReadOnView()) {
                itemTagChangeAdapter.markItemRead(item, true);
            }
            if (getApp().getSettings().removeFromReadListOnView()) {
                itemTagChangeAdapter.removeItemTag(item, getApp().getSettings().getLabelReadListId());
            }
        }
    }

    private void commitTagChanges(SQLiteDatabase db) {
        if (itemTagChangeAdapter.commitChanges(db)) {
            getApp().onItemTagsChanged();
        }
    }

    private static final class ItemLoader extends AsyncTask<Long, Integer, Item> {
        private final ItemViewContentFragment fragment;
        private final Application app;

        public ItemLoader(ItemViewContentFragment fragment, Application app) {
            this.fragment = fragment;
            this.app = app;
        }

        public void load(long itemKey) {
            execute(itemKey);
        }
        
        @Override
        protected Item doInBackground(Long... longs) {
            return fetchItem(app.getDbHelper().getReadOnlyDatabase(), longs[0]);
        }

        protected Item fetchItem(SQLiteDatabase readOnlyDb, long itemKey) {
            fragment.commitTagChanges(app.getDbHelper().getDatabase());

            Log.i(LOG_TAG, "fetching item " + itemKey);
            final Item item = fragment.itemAdapter.readFullByKey(readOnlyDb, itemKey);
            Log.i(LOG_TAG, "fetching item " + (item != null ? "succeeded" : "failed"));
            if (item != null) {
                try {
                    item.loadIfRequired(app.getSettings().getContentStorageProvider());
                    item.updateContentFlags();
                    Log.i(LOG_TAG, "item loaded");
                } catch (IOException ex) {
                    Log.e(LOG_TAG, "Failed to load item", ex);

                    final String errorMsg = app.getString(R.string.error_failed_loading_local_content);
                    if (item.hasSummary()) {
                        item.setSummary(errorMsg);
                    }
                    if (item.hasContent()) {
                        item.setContent(errorMsg);
                    }
                }
            }
            return item;
        }

        @Override
        protected void onPostExecute(Item item) {
            fragment.updateView(item);
        }
    }


    private boolean updateView(Item item) {
        Log.i(LOG_TAG, "updating view");
        if (item == null || !isAlive())
            return false;

        ItemViewContentFragment.this.item = item;
        implicitlyUpdateItemTagsIfApplicable();

        //
        // set title
        //
        Log.i(LOG_TAG, "updating view header");

        titleTextView.setText(Html.fromHtml(ItemTextUtil.getItemTitle(getActivity(), item)));

        String feedTitleText = ItemTextUtil.getItemSpecialFeedTitle(getActivity(), item);
        if (feedTitleText == null) {
            feedTitleText = item.getFeedTitle();
        }
        feedTitle.setText(feedTitleText != null ? feedTitleText : "");

        //
        // set author and date
        //
        if (item.getAuthor() != null) {
            authorView.setText(MessageFormat.format(getString(R.string.author_by), item.getAuthor()));
            authorView.setVisibility(View.VISIBLE);
        } else {
            authorView.setText("");
            authorView.setVisibility(View.GONE);
        }

        final Date timestamp = item.getTimestamp();
        timestampView.setText(timestampFormat.format(timestamp));
        timestampView.setVisibility(timestamp != null ? View.VISIBLE : View.INVISIBLE);

        //
        // update button visibility
        //
        readListButton.setVisibility(getApp().getSettings().shouldShowReadListTag() ? View.VISIBLE : View.GONE);
        updateLabelControls();

        //
        // inform listener
        //
        loadContentIfApplicable();
        Log.i(LOG_TAG, "done updating view");

        return true;
    }

    private void setLoadProgress(int progress) {
        if (isAlive()) {
            if (progress < 100)
                progressBar.setVisibility(View.VISIBLE);

            progressBar.setProgress(progress);
            if (progress >= 100)
                progressBar.startAnimation(progressBarFadeOutAnimation);
        }
    }
}
