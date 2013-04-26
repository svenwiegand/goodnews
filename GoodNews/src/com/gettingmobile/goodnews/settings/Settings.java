package com.gettingmobile.goodnews.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.webkit.WebSettings;
import com.actionbarsherlock.view.MenuItem;
import com.gettingmobile.android.app.settings.AbstractSettings;
import com.gettingmobile.goodnews.R;
import com.gettingmobile.goodnews.download.OfflineContentType;
import com.gettingmobile.goodnews.download.OfflineStrategy;
import com.gettingmobile.goodnews.storage.StorageProvider;
import com.gettingmobile.goodnews.storage.StorageProviderFactory;
import com.gettingmobile.goodnews.tip.TipStatusStorage;
import com.gettingmobile.google.reader.ElementId;
import com.gettingmobile.google.reader.Item;
import com.gettingmobile.google.reader.ItemContentTreatment;
import com.gettingmobile.google.reader.ItemTeaserSource;
import com.gettingmobile.google.reader.db.SortOrder;
import com.gettingmobile.google.reader.sync.SyncSettings;
import com.gettingmobile.net.mobilizer.MobilizerImplementation;
import com.gettingmobile.net.mobilizer.UrlMobilizer;
import com.gettingmobile.net.mobilizer.UrlMobilizerFactory;

import java.text.MessageFormat;
import java.util.Calendar;

public final class Settings extends AbstractSettings implements SyncSettings, TipStatusStorage {
    public static final String GLOBAL = "GLOBAL";
    private static final int MAX_UNREAD_KEEPING = 10000;
    private static final String ITEM_VIEW_ACTION_BASE = "pref_item_view_action_";
    private StorageProvider databaseStorageProvider = null;
    private StorageProvider contentStorageProvider = null;
    private UrlMobilizerFactory mobilizerFactory = new UrlMobilizerFactory();

	public Settings(Context context) {
        super(context);

        /*
         * set default values
         */
        Log.d(LOG_TAG, "initializing default values");
        PreferenceManager.setDefaultValues(context, R.xml.pref_sync, true);
        PreferenceManager.setDefaultValues(context, R.xml.pref_view_tag_list, true);
        PreferenceManager.setDefaultValues(context, R.xml.pref_view_feed_list, true);
        PreferenceManager.setDefaultValues(context, R.xml.pref_view_item_list, true);
        PreferenceManager.setDefaultValues(context, R.xml.pref_view_item_view, true);
        PreferenceManager.setDefaultValues(context, R.xml.pref_news_reading, true);
        PreferenceManager.setDefaultValues(context, R.xml.pref_storage, true);
        PreferenceManager.setDefaultValues(context, R.xml.pref_other, true);
        PreferenceManager.setDefaultValues(context, R.xml.pref_app, true);

        initContentStorageProviderSetting();

        /*
         * init usage statistics
         */
        initInstallTime();
	}

    /*
     * usage statistics
     */

    public Calendar getInstallTime() {
        return getTimestamp("usage_install_time");
    }

    public void initInstallTime() {
        if (getInstallTime() == null) {
            setTimestampToNow("usage_install_time");
        }
    }

    public int getCurrentVersionCode() {
        return getInt("version_code_current");
    }

    public void setCurrentVersionCode(int versionCode) {
        final int oldVersionCode = getCurrentVersionCode();
        setInt("version_code_current", versionCode);
        if (versionCode > oldVersionCode) {
            onUpdate(oldVersionCode, versionCode);
        }
    }

    public int getPreviousVersionCode() {
        return getInt("version_code_previous");
    }

    public void setPreviousVersionCode(int versionCode) {
        setInt("version_code_previous", versionCode);
    }
    
    private void onUpdate(int oldVersionCode, int newVersionCode) {
        Log.i(LOG_TAG, "Updating from version " + oldVersionCode + " to " + newVersionCode);
        
        if (oldVersionCode < 207 || (oldVersionCode < 200 && oldVersionCode < 128)) {
            updateValues(".*mobilizer", "(GETTINGMOBILE|REAT_IT_LATER)", MobilizerImplementation.READABILITY.name());
        }
    }

    /*
     * tips
     */

    @Override
    public boolean wasTipShown(String tipId) {
        return getBoolean("tip_" + tipId);
    }

    @Override
    public void setTipShown(String tipId) {
        setBoolean("tip_" + tipId, true);
    }

    /*
     * changelog
     */
    
    public boolean shouldShowChangelogAutomatically() {
        return getBoolean("changelog_show_automatically", true);
    }

    public void setShouldShowChangelogAutomatically(boolean show) {
        setBoolean("changelog_show_automatically", show);
    }

	/*
	 * authentication
	 */

    public void setAccountType(String accountType) {
        setString("account_type", accountType);
    }

    public String getAccountType() {
        return getString("account_type");
    }

	public String getAccountName() {
		return getString("account_name");
	}
	
	public void setAccountName(String accountName) {
		final SharedPreferences.Editor editor = prefs.edit();
		editor.putString("account_name", accountName);
		editor.commit();
	}
	
	public String getUserName() {
		return getString("user_name");
	}
	
	public void setUserName(String userName) {
		final SharedPreferences.Editor editor = prefs.edit();
		editor.putString("user_name", userName);
		editor.commit();
	}
	
	public String getPassword() {
		return getString("password");
	}
	
	public void setPassword(String password) {
		final SharedPreferences.Editor editor = prefs.edit();
		editor.putString("password", password);
		editor.commit();
	}
		
	public String getAuthToken() {
		return getString("auth_token");
	}
	
	public void setAuthToken(String authToken) {
		final SharedPreferences.Editor editor = prefs.edit();
		editor.putString("auth_token", authToken);
		editor.commit();		
	}

    /*
     * database handling
     */

    public void setDatabasesSwapped(boolean swapped) {
        setBoolean("databases_swapped", swapped);
    }

    public boolean areDatabasesSwapped() {
        return getBoolean("databases_swapped");
    }

    /*
     * cleanup
     */

    public Calendar getMostRecentCleanupTimestamp() {
        return getTimestamp("cleanup_most_recent_timestamp");
    }

    public void setMostRecentCleanupTimestamp() {
        setTimestampToNow("cleanup_most_recent_timestamp");
    }
    
	/*
	 * synchronization
	 */
	
	@Override
	public boolean getPushImmediately() {
		return getBoolean("sync_push_immediately");
	}

    public int getContinousSyncInterval() {
        return getIntFromString("sync_continous", 0) * 60 * 1000;
    }
    
    public Calendar getNextContinousSyncTimestamp() {
        final int intervalMillis = getContinousSyncInterval();
        if (intervalMillis <= 0)
            return null;
        
        final Calendar c = getLastSuccessfulPullTimestamp();
        if (c != null) {
            c.add(Calendar.MILLISECOND, intervalMillis);
            return c;
        } else {
            return Calendar.getInstance();
        }
    }
    
    public long getNextContinousSyncTimestampInMillis() {
        final Calendar c = getNextContinousSyncTimestamp();
        return c != null ? c.getTimeInMillis() : 0;
    }

    public boolean requiresWifiForContinousSync() {
        return getBoolean("sync_continous_wifi_only");
    }

    public boolean shouldNotifyOnNewUnreadItems() {
        return getBoolean("sync_notify_unread");
    }

    public String getLabelReadListName() {
		return getString("sync_labels_readlist");
	}
	
	@Override
	public ElementId getLabelReadListId() {
		return ElementId.createUserLabel(getLabelReadListName());
	}

    @Override
	public int getMaxUnreadSync() {
		return getIntFromString("sync_unread_max");
	}

    private boolean syncNewUnreadOnly() {
        return getBoolean("sync_new_unread_only");
    }

    @Override
    public Calendar getMinUnreadTimestamp() {
        Calendar c = getTimestamp("sync_min_unread_timestamp");
        if (!syncNewUnreadOnly() || c == null) {
            c = Calendar.getInstance();
            c.setTimeInMillis(0);
        }
        return c;
    }

    @Override
    public void setMinUnreadTimestamp(Calendar timestamp) {
        final Calendar prevSetting = getMinUnreadTimestamp();
        if (prevSetting == null || timestamp.after(prevSetting)) {
            setTimestamp("sync_min_unread_timestamp", timestamp);
        }
    }

    public void resetMinUnreadTimestamp() {
        setTimestamp("sync_min_unread_timestamp", 0);
    }

    @Override
    public int getMaxUnreadKeeping() {
        return getBoolean("sync_unread_keeping_restrict") ? getMaxUnreadSync() : MAX_UNREAD_KEEPING;
    }

    @Override
	public int getMaxTaggedSync() {
		return getIntFromString("sync_tagged_max");
	}

    @Override
    public int getDaysToCache() {
        return getIntFromString("sync_cache_days");
    }

    @Override
    public int getDaysToCleanupUnread() {
        return getIntFromString("sync_cleanup_unread_days");
    }

    @Override
    public boolean shouldSyncTag(ElementId tagId) {
        return getBoolean(tagId.getId() + "_sync", true);
    }

    public Calendar getLastSuccessfulPullTimestamp() {
        return getTimestamp("sync_timestamp_last_successful_pull");
    }

    @Override
    public void updateLastSuccessfulPullTimestamp() {
        setTimestampToNow("sync_timestamp_last_successful_pull");
    }

    public Calendar getLastSuccessfulPushTimestamp() {
        return getTimestamp("sync_timestamp_last_successful_push");
    }

    @Override
    public void updateLastSuccessfulPushTimestamp() {
        setTimestampToNow("sync_timestamp_last_successful_push");
    }

    public long getLastFailedSyncTimestampInMillis() {
        final Calendar c = getLastFailedSyncTimestamp();
        return c != null ? c.getTimeInMillis() : 0;
    }
    
    public Calendar getLastFailedSyncTimestamp() {
        return getTimestamp("sync_timestamp_last_failed");
    }

    @Override
    public void updateLastFailedSyncTimestamp() {
        setTimestampToNow("sync_timestamp_last_failed");
    }

    public int getFetchOldMaxAge() {
        return prefs.getInt("fetch_old_max_age", 31);
    }
    
    public void setFetchOldMaxAge(int maxDays) {
        setInt("fetch_old_max_age", maxDays);
    }
    
    public int getFetchOldMaxCount() {
        return prefs.getInt("fetch_old_max_count", 50);
    }

    public void setFetchOldMaxCount(int maxCount) {
        setInt("fetch_old_max_count", maxCount);
    }

    /*
     * item specific settings
     */

    @Override
    public boolean shouldIgnoreUnread(ElementId feedId) {
        return feedId != null && getBoolean(feedId.getId() + "_ignore_unread");
    }
    
    @Override
    public boolean autoListFeedArticles(ElementId feedId) {
        /*
         * the global setting "articles_autolist" does not exist -- we use it here to be able to use the
         * get...Override() method.
         */
        return getBooleanOverride((feedId != null ? feedId.getId() : null) + "_autolist", "articles_autolist", true);
    }

    @Override
    public ItemContentTreatment getFeedSummaryTreatment(ElementId feedId) {
        return feedId != null ?
                getValue(ItemContentTreatment.class, feedId.getId() + "_summary_treatment", ItemContentTreatment.TREAT_AS_SUMMARY) :
                ItemContentTreatment.TREAT_AS_SUMMARY;
    }

    @Override
    public ItemContentTreatment getFeedContentTreatment(ElementId feedId) {
        return feedId != null ?
                getValue(ItemContentTreatment.class, feedId.getId() + "_content_treatment", ItemContentTreatment.TREAT_AS_CONTENT) :
                ItemContentTreatment.TREAT_AS_CONTENT;
    }

    /*
     * tag list
     */

    public boolean folderClickOpensItemList() {
        return getIntFromString("folder_click_action") != 0;
    }

    public boolean sortByDragAndDropOrder() {
        return getIntFromString("sort_strategy") != 0;
    }

    public boolean shouldShowSyncStatus() {
        return getBoolean("taglist_show_syncstatus");
    }

    public boolean shouldShowStarredTag() {
        return getBoolean("taglist_show_starred");
    }

    public boolean shouldShowReadListTag() {
        return getBoolean("taglist_show_readlist");
    }

    public boolean shouldShowAllItemsTag() {
        return getBoolean("taglist_show_allitems");
    }

	/*
	 * list
	 */

    @Override
    public int getTeaserWordCount() {
        return getIntFromString("teaser_word_count");
    }

    @Override
    public ItemTeaserSource getFeedTeaserSource(ElementId feedId) {
        return getValue(ItemTeaserSource.class, feedId + "_teaser_source", ItemTeaserSource.PREFER_SUMMARY);
    }

    @Override
    public int getFeedTeaserStartChar(ElementId feedId) {
        return getIntFromString(feedId + "_teaser_start_char");
    }

    @Override
    public boolean shouldFixDuplicateItems() {
        return getBoolean("sync_fix_duplicate_items", true);
    }

    public boolean hideRead() {
		return getBoolean("hide_read");
	}
	
	public void setHideRead(boolean hideRead) {
        setBoolean("hide_read", hideRead);
	}
	
	public boolean hideReadInLabelItemList() {
		return getBoolean("hide_read_label_item_list");
	}
	
	public void setHideReadInLabelItemList(boolean hideRead) {
        setBoolean("hide_read_label_item_list", hideRead);
	}

    public boolean groupByFeedsInFolders() {
        return getBoolean("group_folder_item_list");
    }

    public void setGroupByFeedsInFolders(boolean group) {
        setBoolean("group_folder_item_list", group);
    }

    public boolean groupByFeedsInLabels() {
        return getBoolean("group_label_item_list");
    }

    public void setGroupByFeedsInLabels(boolean group) {
        setBoolean("group_label_item_list", group);
    }

	public SortOrder getItemSortOrder() {
		return SortOrder.valueOf(getString("item_list_sort_order"));
	}
	
	public void setItemSortOrder(SortOrder sortOrder) {
		final SharedPreferences.Editor editor = prefs.edit();
		editor.putString("item_list_sort_order", sortOrder.name());
		editor.commit();				
	}

    public boolean offlineIndicatorTogglesReadState() {
        return getBoolean("item_list_offline_indicator_toggles_read_state");
    }

    public void setOfflineIndicatorTogglesReadState(boolean checked) {
        setBoolean("item_list_offline_indicator_toggles_read_state", checked);
    }

    public WebSettings.TextSize getItemListTitleTextSize() {
        return getValue(WebSettings.TextSize.class, "item_list_title_text_size");
    }
	
	/*
	 * implicit labeling
	 */
	
	public boolean markReadOnScrollOver() {
		return getBoolean("labeling_mark_read_scrollover");
	}
	
	public boolean markReadOnView() {
		return getBoolean("labeling_mark_read_on_view");
	}
	
	public boolean markReadOnTag() {
		return getBoolean("labeling_mark_read_on_tag");		
	}
	
	public boolean removeFromReadListOnView() {
		return getBoolean("labeling_remove_readlist_on_view");
	}
	
	/*
	 * extended control
	 */
	
	public boolean useVolumeKeysForItemNavigation() {
		return getBoolean("control_item_navigation_volumekeys");
	}

    public boolean useVolumeKeysForArticleScrolling() {
        return getBoolean("control_item_scroll_volumekeys");
    }

    public boolean articleTurnOverRequiresBorderSwipe() {
        return getBoolean("item_swipe_border");
    }

    public boolean keepItemScreenOn() {
        return getBoolean("item_keep_screen_on");
    }

	/*
	 * item display
	 */

    public boolean isArticleFullScreenEnabled() {
        return getBoolean("item_view_full_screen");
    }

    public void setArticleFullScreenEnabled(boolean enabled) {
        setBoolean("item_view_full_screen", enabled);
    }


    public int getItemActionButtonCount() {
        final String[] keys = { "share", "labels", "browser", "load", "browser_back", "prev", "next" };
        int count = 0;
        for (String key : keys) {
            if (getActionButtonVisibility(key) == ActionButtonVisibility.ALWAYS)
                ++count;
        }
        return count;
    }

    public ActionButtonVisibility getActionButtonVisibility(String key) {
        return getBoolean(ITEM_VIEW_ACTION_BASE + key) ? ActionButtonVisibility.ALWAYS : ActionButtonVisibility.NEVER;
    }

    public int getActionButtonVisibilityFlags(String key) {
        switch (getActionButtonVisibility(key)) {
            case IF_ROOM:
                return MenuItem.SHOW_AS_ACTION_IF_ROOM;
            case ALWAYS:
                return MenuItem.SHOW_AS_ACTION_ALWAYS;
            default:
                return MenuItem.SHOW_AS_ACTION_NEVER;
        }
    }

    public ItemTitleClickAction getItemTitleClickAction() {
        return getValue(ItemTitleClickAction.class, "item_view_title_click_action", ItemTitleClickAction.LABELS);
    }

    public WebSettings.TextSize getItemViewTextSize() {
        return getValue(WebSettings.TextSize.class, "item_view_text_size");
    }

	private ItemDisplayType getDisplayType(String key) {
		final String value = getString(key);
		return ItemDisplayType.valueOf(value);
	}
	
	public ItemDisplayType getDisplayTypeForBlankItems() {
		return getDisplayType("item_view_none");
	}
	
	public ItemDisplayType getDisplayTypeForItemsWithSummary() {
		return getDisplayType("item_view_summary");
	}
	
	public ItemDisplayType getDisplayTypeForItemsWithContent() {
		return getDisplayType("item_view_content");
	}
	
	public ItemDisplayType getItemDisplayType(Item item) {
		ItemDisplayType displayType = getFeedItemDisplayType(item);

        if (displayType == null) {
            /*
             * there are no feed specific settings for this item -- use global settings
             */
            if (item.hasContent()) {
                displayType = getDisplayTypeForItemsWithContent();
                if (displayType == ItemDisplayType.SUMMARY && !item.hasSummary()) {
                    displayType = ItemDisplayType.CONTENT;
                }
            } else if (item.hasSummary()) {
                displayType = getDisplayTypeForItemsWithSummary();
            } else {
                displayType = getDisplayTypeForBlankItems();
            }
        }

        /*
         * ensure that display type matches article capabilities
         */
		if ((displayType == ItemDisplayType.BROWSER || displayType == ItemDisplayType.LOAD) && item.getAlternate() == null) {
            displayType = ItemDisplayType.CONTENT;
		}
        if (displayType == ItemDisplayType.CONTENT && !item.hasContent()) {
            displayType = ItemDisplayType.SUMMARY;
        }
        if (displayType == ItemDisplayType.SUMMARY && !item.hasSummary()) {
            displayType = ItemDisplayType.NOTHING;
        }
		
		return displayType;
	}

	/*
	 * UI Others
	 */

	public boolean automaticallyCloseReadItemList() {
		return getBoolean("ui_other_close_empty_item_list");
	}

    public Theme getTheme() {
        return getValue(Theme.class, "theme", Theme.WHITE);
    }
    
    public void setTheme(Theme theme) {
        setValue("theme", theme);
    }

    public boolean shouldUnsplitActionBar() {
        return getBoolean("actionbar_unsplit");
    }

    /*
     * Notifications
     */

    public boolean showSyncFinishedNotificationInLists() {
        return getBoolean("notifications_list_sync_finished");
    }

    public void setShowSyncFinishedNotificationInLists(boolean enable) {
        setBoolean("notifications_list_sync_finished", enable);
    }

	/*
	 * Confirmations
	 */
	
	public boolean confirmMarkAllReadInTagList() {
		return getBoolean("confirmations_mark_read_in_tag_list");
	}
	
	public boolean confirmMarkAllReadInFeedList() {
		return getBoolean("confirmations_mark_read_in_feed_list");
	}
	
	public boolean confirmMarkAllReadInItemList() {
		return getBoolean("confirmations_mark_read_in_item_list");
	}

    /*
     * News Reading
     */

    public MobilizerImplementation getMobilizerImplementation(ElementId feedId) {
        return getValueOverride(MobilizerImplementation.class, feedId.getId() + "_mobilizer", "mobilizer",
                MobilizerImplementation.NONE);
    }

    public UrlMobilizer getUrlMobilizer(ElementId feedId) {
        return mobilizerFactory.getMobilizer(getMobilizerImplementation(feedId));
    }

    public boolean scaleImages(ElementId feedId) {
        return getBooleanOverride((feedId != null ? feedId.getId() : null) + "_scale_images", "scale_images");
    }

    protected ItemDisplayType getFeedItemDisplayType(Item item) {
        final ElementId feedId = item.getFeedId();
        if (feedId == null)
            return null;

        final String name = getString(feedId.getId() + "_item_view");
        return name != null && !GLOBAL.equals(name) ? ItemDisplayType.valueOf(name) : null;
    }

    public OfflineContentType getOfflineContentType(ElementId feedId) {
        return getValueOverride(OfflineContentType.class, feedId.getId() + "_offline_content", "offline_content",
                OfflineContentType.TEXT);
    }

    public OfflineStrategy getOfflineStrategy() {
        return getValue(OfflineStrategy.class, "offline_strategy", OfflineStrategy.MANUAL);
    }

    public boolean offlineDownloadRequiresWifi() {
        return getBoolean("offline_wifi");
    }

    /*
     * storage stuff
     */

    @Override
    public boolean cancelSyncOnLowDeviceStorage() {
        return getBoolean("sync_cancel_on_low_device_storage");
    }

    private StorageProvider getStorageProvider(String key, StorageProvider current, StorageProvider.Storage defaultStorage) {
        StorageProvider.Storage requestedStorage = getValue(StorageProvider.Storage.class, key, defaultStorage);
        if (current == null || (requestedStorage != null && current.getType() != requestedStorage)) {
            current = StorageProviderFactory.createStorageProvider(context, requestedStorage);
        }
        return current;
    }

    private StorageProvider getStorageProvider(String key, StorageProvider current) {
        return getStorageProvider(key, current, StorageProvider.Storage.INTERNAL);
    }
    
    private void initContentStorageProviderSetting() {
        final StorageProvider.Storage storage = getValue(StorageProvider.Storage.class, "content_storage_provider", null);
        if (storage == null) {
            setValue("content_storage_provider", 
                    getValue(StorageProvider.Storage.class, "database_storage_provider", StorageProvider.Storage.EXTERNAL));
        }
    }

    @Override
    public StorageProvider getContentStorageProvider() {
        contentStorageProvider = getStorageProvider("content_storage_provider", contentStorageProvider, null);
        return contentStorageProvider;
    }

    @Override
    public StorageProvider getDatabaseStorageProvider() {
        databaseStorageProvider = getStorageProvider("database_storage_provider", databaseStorageProvider);
        return databaseStorageProvider;
    }

    public void setDatabaseStorageProvider(StorageProvider storageProvider) {
        setValue("database_storage_provider", storageProvider.getType());
    }

    @Override
    public boolean storeContentInFiles() {
        return !getBoolean("enforce_content_database_storage", false);
    }

    /*
     * other stuff
     */

    /**
     * Returns the content format for sharing links. Argument 0 is the link and argument 1 the title.
     */
    public MessageFormat getShareContentFormat() {
        return new MessageFormat(getString("share_content"));
    }

    public boolean checkForInternetConnection() {
        return getBoolean("check_for_internet_connection");
    }
    
    public String getUserEMailAddress() {
        return getString("user_email_address");
    }
    
    public void setUserEMailAddress(String emailAddress) {
        setString("user_email_address", emailAddress);
    }

    /*
     * info & support stuff
     */

    public boolean autoShowUsageTips() {
        return getBoolean("tips_auto_show");
    }

    public void setAutoShowUsageTips(boolean show) {
        setBoolean("tips_auto_show", show);
    }
}
