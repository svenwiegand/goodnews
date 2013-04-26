package com.gettingmobile.goodnews;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.view.Gravity;
import com.gettingmobile.android.app.DialogFactory;
import com.gettingmobile.android.content.ApplicationContextUtil;
import com.gettingmobile.goodnews.account.AccountHandler;
import com.gettingmobile.goodnews.account.AccountHandlerFactory;
import com.gettingmobile.goodnews.backup.BackupManager;
import com.gettingmobile.goodnews.download.ContentDownloadService;
import com.gettingmobile.goodnews.home.HomeActivity;
import com.gettingmobile.goodnews.settings.ProxyFileConfiguration;
import com.gettingmobile.goodnews.settings.Settings;
import com.gettingmobile.goodnews.sync.SyncService;
import com.gettingmobile.goodnews.sync.SyncServiceProxy;
import com.gettingmobile.goodnews.tip.StandardFullScreenTip;
import com.gettingmobile.goodnews.tip.TipManager;
import com.gettingmobile.goodnews.tip.VisualActionTip;
import com.gettingmobile.goodnews.tip.VisualViewTagTip;
import com.gettingmobile.goodnews.util.ThemeUtil;
import com.gettingmobile.google.Authenticator;
import com.gettingmobile.google.StaticAuthenticator;
import com.gettingmobile.google.reader.ItemState;
import com.gettingmobile.google.reader.db.DatabaseHelper;
import com.gettingmobile.google.reader.sync.SyncContext;
import com.gettingmobile.rest.RequestHandler;
import com.google.inject.util.Modules;
import roboguice.RoboGuice;

public class Application extends android.app.Application implements SyncContext {
    private static final String LOG_TAG = "goodnews.Application";
    private RequestHandler requestHandler = null;
    private final StaticAuthenticator authenticator = new StaticAuthenticator();
    private AccountHandler accountHandler = null;
    private ThemeUtil themeUtil = null;
    private DatabaseHelper dbHelper = null;
    private final SyncServiceProxy syncService = new SyncServiceProxy();
    private Settings settings = null;
    private TipManager tipManager = null;

    public Application() {
        super();
        Log.i(LOG_TAG, "Constructed application");
    }

    protected void registerTips() {
        /*
         * welcome
         */
        tipManager.addTip(R.integer.tip_group_welcome, new StandardFullScreenTip("welcome", true));
        tipManager.addTip(R.integer.tip_group_welcome, new StandardFullScreenTip("tips", true));
        tipManager.addTip(R.integer.tip_group_welcome, new StandardFullScreenTip("customizing", true));

        /*
         * home screen
         */
        tipManager.addTip(R.integer.tip_group_tag_list, new VisualActionTip("v_action_tip", R.string.vtip_action_tooltip, R.id.menu_mark_read));
        tipManager.addTip(R.integer.tip_group_tag_list, new VisualActionTip("v_action_sync", R.string.vtip_sync, R.id.menu_sync_full));
        tipManager.addTip(R.integer.tip_group_tag_list, new VisualViewTagTip("v_folder", R.string.vtip_folder, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, ItemState.READING_LIST.getId()));
        tipManager.addTip(R.integer.tip_group_tag_list, new VisualViewTagTip("v_folder_button", R.string.vtip_folder_button, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, ItemState.READING_LIST.getId(), R.id.folder_edge_button));
        tipManager.addTip(R.integer.tip_group_tag_list, new StandardFullScreenTip("welcome", false));
        tipManager.addTip(R.integer.tip_group_tag_list, new StandardFullScreenTip("tips", false) {
            @Override
            public boolean forceAutomaticTipCheckBox() {
                return true;
            }
        });
        tipManager.addTip(R.integer.tip_group_tag_list, new StandardFullScreenTip("customizing", false));
        tipManager.addTip(R.integer.tip_group_tag_list, new StandardFullScreenTip("sync", false));
        tipManager.addTip(R.integer.tip_group_tag_list, new StandardFullScreenTip("home", true) {
            @Override
            public boolean canShowNow(Activity activity) {
                return ((HomeActivity) activity).hasContent();
            }
        });
        tipManager.addTip(R.integer.tip_group_tag_list, new StandardFullScreenTip("sync_status", false));

        /*
         * item list
         */
        tipManager.addTip(R.integer.tip_group_item_list, new VisualActionTip("v_action_hide_read", R.string.vtip_hide, R.id.menu_show_new, R.id.menu_show_all));
        tipManager.addTip(R.integer.tip_group_item_list, new VisualActionTip("v_action_read", R.string.vtip_read, R.id.menu_mark_read));
        tipManager.addTip(R.integer.tip_group_item_list, new StandardFullScreenTip("items", true));
        tipManager.addTip(R.integer.tip_group_item_list, new StandardFullScreenTip("offline_indicator", false,
                R.string.tip_offline_indicator_mark_read, getSettings().offlineIndicatorTogglesReadState()) {
            @Override
            public void onAdditionalCheckBoxStateChanged(Application app, boolean checked) {
                super.onAdditionalCheckBoxStateChanged(app, checked);
                getSettings().setOfflineIndicatorTogglesReadState(checked);
            }
        });

        /*
         * article view
         */
        tipManager.addTip(R.integer.tip_group_item_view, new VisualActionTip("v_tip_browser", R.string.vtip_browser, R.id.menu_item_browser));
        tipManager.addTip(R.integer.tip_group_item_view, new VisualActionTip("v_tip_load_content", R.string.vtip_load_content, R.id.menu_item_load));
        tipManager.addTip(R.integer.tip_group_item_view, new StandardFullScreenTip("item_action_bar", true));
        tipManager.addTip(R.integer.tip_group_item_view, new StandardFullScreenTip("navigation", true));
        tipManager.addTip(R.integer.tip_group_item_view, new StandardFullScreenTip("mobilizer", true));
    }

    /*
     * lifecycle management
     */

    @Override
    public void onCreate() {
        Log.i(LOG_TAG, "onCreate");
        super.onCreate();

        RoboGuice.setBaseApplicationInjector(this, RoboGuice.DEFAULT_STAGE,
                Modules.override(RoboGuice.newDefaultRoboModule(this)).with(new InjectionModule(this)));

        /*
         * init settings
         */
        Log.i(LOG_TAG, "Initializing settings");
        settings = new Settings(this);
        ProxyFileConfiguration.init(this);
        BackupManager.init(this);

        /*
         * init database
         */
        Log.i(LOG_TAG, "Initializing database");
        dbHelper = DatabaseHelper.create(this, settings);

        /*
         * initiate request handler
         */
        Log.i(LOG_TAG, "Initializing request handler");
        requestHandler = new RequestHandler();
        requestHandler.start();

        /*
         * init other stuff
         */
        Log.i(LOG_TAG, "Initializing other stuff");
        themeUtil = new ThemeUtil(this);
        syncService.startService(this);
        accountHandler = AccountHandlerFactory.getInstance().createAccountHandler(this);

        /*
         * init tips
         */
        tipManager = new TipManager(settings);
        registerTips();
    }

    @Override
    public void onTerminate() {
        syncService.stopService(this);
        requestHandler.shutdown();
        dbHelper.close();
        super.onTerminate();
    }

    /*
     * generic helpers
     */

    public PackageInfo getPackageInfo() {
        return ApplicationContextUtil.getPackageInfo(this);
    }

    public ApplicationInfo getApplicationInfo() {
        return ApplicationContextUtil.getApplicationInfo(this);
    }

    public String getApplicationName() {
        return ApplicationContextUtil.getApplicationName(this);
    }

    public ThemeUtil getThemeUtil() {
        return themeUtil;
    }

    public RequestHandler getRequestHandler() {
        return requestHandler;
    }

    public SyncServiceProxy getSyncService() {
        return syncService;
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public Settings getSettings() {
        return settings;
    }

    public void authenticate(String authToken) {
        authenticator.setAuthToken(authToken);
        authenticator.setEditToken(null);
        getSettings().setAuthToken(authToken);
    }

    public boolean isLoggedIn() {
        return authenticator.hasValidAuthToken();
    }

    @Override
    public Authenticator getAuthenticator() {
        return authenticator;
    }

    public AccountHandler getAccountHandler() {
        return accountHandler;
    }

    /*
    * database handling
    */

    @Override
    public DatabaseHelper getDbHelper() {
        return dbHelper;
    }

    public void onItemTagsChanged() {
        final SyncService svc = getSyncService().getService();
        if (svc != null) {
            svc.scheduleImmediatePushSyncIfApplicable();
        }

        ContentDownloadService.startAfterTagChangeIfApplicable(this);
    }

    /*
     * network handling
     */

    public boolean isInternetAvailable() {
        final ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        final NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null && ni.isConnected();
    }

    public boolean isInternetAvailableAndRequired() {
        return !settings.checkForInternetConnection() || isInternetAvailable();
    }

    public boolean checkInternetAvailableAndRequired(android.app.Activity activity) {
        if (!isInternetAvailableAndRequired()) {
            DialogFactory.showErrorDialog(activity,
                    R.string.error_no_internet_title, getString(R.string.error_no_internet_msg));
            return false;
        } else {
            return true;
        }
    }

    public boolean isWifiConnected() {
        final ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        final NetworkInfo ni = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return ni != null && ni.isConnected();
    }

    /*
     * tips
     */

    public TipManager getTipManager() {
        return tipManager;
    }
}
