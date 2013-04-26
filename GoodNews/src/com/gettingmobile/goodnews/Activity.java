package com.gettingmobile.goodnews;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.gettingmobile.android.app.actions.*;
import com.gettingmobile.android.app.settings.AbstractSettings;
import com.gettingmobile.android.app.settings.OnSettingChangeListener;
import com.gettingmobile.goodnews.home.HomeActivity;
import com.gettingmobile.goodnews.settings.SettingsHandler;
import com.gettingmobile.goodnews.settings.SettingsIntentFactory;
import com.gettingmobile.goodnews.settings.ViewSettingsActivity;
import com.gettingmobile.goodnews.tip.TipGroupAction;
import com.gettingmobile.goodnews.tip.TipManager;
import com.gettingmobile.goodnews.util.ThemeUtil;
import com.gettingmobile.google.reader.*;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockFragmentActivity;
import com.google.inject.Inject;

public abstract class Activity extends RoboSherlockFragmentActivity
		implements ActionController<Integer>, ActionContext<Application>, View.OnClickListener, OnCheckedChangeListener {
	public static final String EXTRA_KEY_BASE = "com.gettingmobile.goodnews.";
	public static final String EXTRA_KEY_ELEMENT_ID = EXTRA_KEY_BASE + "ELEMENT_ID";
	public static final String EXTRA_KEY_ELEMENT_KEY = EXTRA_KEY_BASE + "ELEMENT_KEY";
	public static final String EXTRA_KEY_ELEMENT_TITLE = EXTRA_KEY_BASE + "ELEMENT_TITLE";
	public static final String EXTRA_KEY_ELEMENT_IS_STATE_OR_LABEL = EXTRA_KEY_BASE + "ELEMENT_IS_STATE_OR_LABEL";
    public static final String EXTRA_KEY_LAUNCHED_BY_SHORTCUT = EXTRA_KEY_BASE + "LAUNCHED_BY_SHORTCUT";

    protected static final String LOG_TAG = "goodnews.Application";
    private ActionContextActivityAccessor<Application, Activity> accessor = null;

	protected static final int PANEL_CONTENT_LOADING = 0;
	protected static final int PANEL_NO_CONTENT = 1;
	protected static final int PANEL_CONTENT = 2;

    private boolean recreated = false;
	private ViewOperation<?> viewLoader = null;
    private final OnViewOperationFinishedListener viewLoadedListener = new OnViewLoadedListener();
	private ViewOperation<?> viewUpdater = null;
    private final OnViewOperationFinishedListener viewUpdatedListener = new OnViewUpdatedListener();
    private boolean windowSettingsChanged = false;

    private boolean hasContent = false;
	private View panelContentLoading = null;
	private View panelNoContent = null;
	private View panelContent = null;
	
    private Menu optionsMenus = null;
	private boolean firstResume = true;

    private final BusyActionController<Integer> actionController = new BusyActionController<Integer>(this);
    private final int tipGroupId;

    private final OnSettingChangeListener windowSettingsChangeListener = new OnSettingChangeListener() {
        @Override
        public void onSettingChanged(AbstractSettings settings, SharedPreferences sharedPreferences, String s) {
            onWindowSettingsChanged();
        }
    };

    @Inject
    protected SettingsIntentFactory settingsIntentFactory = null;

	public Activity() {
        this(TipManager.NO_TIPS);
	}

    public Activity(int tipGroupId) {
        this.tipGroupId = tipGroupId;
    }
	
	public final void setViewLoader(ViewOperation<?> viewLoader) {
		this.viewLoader = viewLoader;
	}
	
	public final void setViewUpdater(ViewOperation<?> viewUpdater) {
		this.viewUpdater = viewUpdater;
	}

    public boolean hasContent() {
        return hasContent;
    }

    @Override
    public android.app.Activity getActivity() {
        return accessor != null ? accessor.getActivity() : this;
    }

    @Override
	public Application getApp() {
		return (Application) getApplication();
	}

    public boolean isLargeScreen() {
        final int size = getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
        return size >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    public ThemeUtil getThemeUtil() {
        return getApp().getThemeUtil();
    }
	
	protected SQLiteDatabase getDb() {
		return getApp().getDbHelper().getDatabase();
	}

    protected SQLiteDatabase getReadOnlyDb() {
        return getApp().getDbHelper().getReadOnlyDatabase();
    }
	
	public ActivityAccessor<Activity> getAccessor() {
		return accessor;
	}

    protected boolean showActionBarIcon() {
        return true;
    }

    protected boolean showActionBarIconAsUp() {
        return true;
    }

    protected boolean isRecreated() {
        return recreated;
    }

    /*
     * life cycle management
     */
    
    protected void onCreate() {
        // nothing to be done by default
    }
    
    protected void onPostCreateRestoreSavedInstanceState(Bundle savedInstanceState) {
        // nothing to be done by default
    }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreate(" + savedInstanceState + ")");

        super.onCreate(savedInstanceState);

        initWindowSettings();

        /*
         * allow creation for sub classes
         */
        onCreate();

        /*
         * initialize action bar
         */
        getSupportActionBar().setDisplayOptions(
                showActionBarIcon() || getIntentLaunchedByShortcut() ? ActionBar.DISPLAY_SHOW_HOME : 0,
                ActionBar.DISPLAY_SHOW_HOME);
        getSupportActionBar().setDisplayOptions(
                showActionBarIconAsUp() ? ActionBar.DISPLAY_HOME_AS_UP : 0,
                ActionBar.DISPLAY_HOME_AS_UP);

        onRegisterActions();
        if (savedInstanceState != null) {
            onPostCreateRestoreSavedInstanceState(savedInstanceState);
        }

		panelContentLoading = findViewById(R.id.content_loading);
		panelNoContent = findViewById(R.id.no_content);
        if (panelNoContent != null) {
            panelNoContent.setOnClickListener(this);
        }
		panelContent = findViewById(R.id.content);

		/*
		 * handle configuration changes
		 */
        getApp().getSettings().registerChangeListener("theme", windowSettingsChangeListener);
        getApp().getSettings().registerChangeListener("actionbar_unsplit", windowSettingsChangeListener);
        
		final Object nonConfigurationInstance = getLastCustomNonConfigurationInstance();
		if (nonConfigurationInstance != null) {
            recreated = true;
			handleNonConfigurationInstance(nonConfigurationInstance);
		} else {
            recreated = false;
			accessor = new ActionContextActivityAccessor<Application, Activity>(this);
		}

		loadView();
	}

    protected boolean shouldUpdateOnResume() {
        return true;
    }

    @Override
	protected void onResume() {
        if (windowSettingsChanged) {
            applyWindowSettingsChanges();
        }
        
        invalidateOptionsMenu();

		super.onResume();

		if (firstResume) {
			firstResume = false;
		} else if (shouldUpdateOnResume()) {
			updateView();
		}
	}

    @Override
    protected void onDestroy() {
        getApp().getSettings().unregisterChangeListener("theme", windowSettingsChangeListener);

        super.onDestroy();
    }
    
    /*
     * theme handling
     */
        
    protected void onWindowSettingsChanged() {
        windowSettingsChanged = true;
    }
    
    protected void applyWindowSettingsChanges() {
        final Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    private void initWindowSettings() {
        switch (getApp().getSettings().getTheme()) {
            case BLACK:
                setTheme(R.style.Theme_GoodNews_Black);
                break;
            case DARK:
                setTheme(R.style.Theme_GoodNews_Dark);
                break;
            case LIGHT:
                setTheme(R.style.Theme_GoodNews_Light);
                break;
            default:
                setTheme(R.style.Theme_GoodNews_White);
        }

        if (!getApp().getSettings().shouldUnsplitActionBar()) {
            getSherlock().setUiOptions(ActivityInfo.UIOPTION_SPLIT_ACTION_BAR_WHEN_NARROW,
                    ActivityInfo.UIOPTION_SPLIT_ACTION_BAR_WHEN_NARROW);
        }
    }

    /*
     * action handling
     */

    protected void onRegisterActions() {
        if (tipGroupId != TipManager.NO_TIPS) {
            registerAction(R.id.menu_tip, new TipGroupAction(tipGroupId, false));
        }
    }

    @Override
    public void registerAction(Integer key, Action<?> action) {
        actionController.registerAction(key, action);
        initAction(key, action);
    }

    @Override
    public <A extends Action<?>> A registerAction(Integer key, Class<A> actionClass) {
        final A action = actionController.registerAction(key, actionClass);
        initAction(key, action);
        return action;
    }

    @SuppressWarnings("unchecked")
    private void initAction(final Integer key, Action<?> action) {
        if (action instanceof BusyAction<?>) {
            final BusyAction<?> ba = (BusyAction<?>) action;
            ba.setListener(new BusyActionListener() {
                @Override
                public void onActionStarted(BusyAction<?> action) {
                    invalidateOptionsMenu();
                }

                @Override
                public void onActionStopped(BusyAction<?> action) {
                    invalidateOptionsMenu();
                }
            });
            markActionBusy(key, ba.getState((ActionContext) this) == BusyAction.BUSY);
        }
    } 

    @Override
    public void unregisterAction(Integer key) {
        actionController.unregisterAction(key);
    }
    
    public boolean fireAction(Integer key) {
        return actionController.fireAction(key);
    }

    protected void markActionBusy(final Integer key, boolean busy) {
        if (optionsMenus != null) {
            final MenuItem item = optionsMenus.findItem(key);
            if (item != null) {
                if (busy) {
                    if (item.getActionView() == null) {
                        item.setActionView(R.layout.action_busy);
                        final Action<?> action = actionController.getAction(key);
                        if (action instanceof BusyAction) {
                            item.getActionView().setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    actionController.stopAction(key);
                                }
                            });
                        }
                    }
                } else {
                    item.setActionView(null);
                }
            }
        }
    }

	protected abstract int getOptionsMenuResourceId();
    protected abstract Class<? extends SettingsHandler> getViewSettingsHandlerClass();

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
        optionsMenus = menu;
        getSupportMenuInflater().inflate(getOptionsMenuResourceId(), menu);
		onInitOptionsMenu(menu);
		return true;
	}
	
	protected void onInitOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_view_preferences).setIntent(
                ViewSettingsActivity.createIntent(this, getViewSettingsHandlerClass()));
		menu.findItem(R.id.menu_preferences).setIntent(settingsIntentFactory.createStandardIntent());
	}

    @Override
    @SuppressWarnings("unchecked")
    public boolean onPrepareOptionsMenu(Menu menu) {
        /*
         * handle actions automatically
         */
        for (int i = 0; i < menu.size(); ++i) {
            final MenuItem item = menu.getItem(i);
            final Action<?> action = actionController.getAction(item.getItemId());
            if (action != null) {
                switch (action.getState((ActionContext) this)) {
                    case Action.ENABLED:
                        item.setVisible(true);
                        item.setEnabled(true);
                        break;
                    case Action.DISABLED:
                        item.setVisible(true);
                        item.setEnabled(false);
                        break;
                    case Action.GONE:
                        item.setVisible(false);
                        item.setEnabled(false);
                        break;
                    case BusyAction.BUSY:
                        item.setVisible(true);
                        item.setEnabled(true);
                    default:
                        // no action -- do not touch
                }
                if (action instanceof BusyAction) {
                    markActionBusy(item.getItemId(),
                            (action.getState((ActionContext) this) == BusyAction.BUSY));
                }
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    private void goHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    protected void goBack() {
        if (getIntentLaunchedByShortcut())
            goHome();
        else
            finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (!fireAction(item.getItemId())) {
            if (item.getItemId() == android.R.id.home) {
                goBack();
            } else {
                return super.onOptionsItemSelected(item);
            }
        }
        return true;
    }

    @Override
	public void onClick(View view) {
		fireAction(view.getId());
	}
	
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		// do nothing by default
	}
    
    protected void onPreFetchData() {
        // do nothing by default
    }

	public void loadView() {
		final String title = getIntentElementTitle();
		if (title != null) {
			setTitle(getIntentElementTitle());
		}

		if (viewLoader != null) {
			viewLoader.run(viewLoadedListener);
		}
	}

    protected boolean onViewLoaded() {
        showUnshownTipsIfApplicable();
        return true;
    }
	
	public void updateView() {
		if (viewUpdater != null) {
			viewUpdater.run(viewUpdatedListener);
		}
	}

    protected boolean onViewUpdated() {
        showUnshownTipsIfApplicable();
        return true;
    }

    public void showUnshownTipsIfApplicable() {
        TipGroupAction.showTips(getApp(), this, getApp().getTipManager(), tipGroupId, true);
    }

    protected void onShowedContentLoadingPanel() {
        // nothing by default
    }

    protected void onShowedNoContentPanel() {
        // nothing by default
    }

    protected void onShowedContentPanel() {
        // nothing by default
    }

	protected void showPanel(int panel) {
		if (panelContentLoading != null) {
			panelContentLoading.setVisibility(panel == PANEL_CONTENT_LOADING ? View.VISIBLE : View.GONE);
		}
		if (panelNoContent != null) {
			panelNoContent.setVisibility(panel == PANEL_NO_CONTENT ? View.VISIBLE : View.GONE);
		}
		if (panelContent != null) {
			panelContent.setVisibility(panel == PANEL_CONTENT ? View.VISIBLE : View.GONE);
		}

        /*
         * callbacks
         */
        switch (panel) {
            case PANEL_CONTENT_LOADING:
                onShowedContentLoadingPanel();
                break;
            case PANEL_NO_CONTENT:
                onShowedNoContentPanel();
                break;
            case PANEL_CONTENT:
                onShowedContentPanel();
                break;
        }
	}

    public static Intent createIntent(Class<? extends Activity> activityClass, Context context, Element element) {
        final Intent intent = new Intent(context, activityClass);
        intent.putExtra(EXTRA_KEY_ELEMENT_ID, element.getId().getId());
        intent.putExtra(EXTRA_KEY_ELEMENT_KEY, element.getKey());
        intent.putExtra(EXTRA_KEY_ELEMENT_TITLE, element.getTitle());
        return intent;
    }
	
	private Intent createIntent(Class<? extends Activity> activityClass, Element element) {
        return createIntent(activityClass, this, element);
	}
	
	protected void startActivity(Class<? extends Activity> activityClass, Element element) {
		startActivity(createIntent(activityClass, element));
	}

    public static Intent createIntent(Class<? extends Activity> activityClass, Context context, Tag tag) {
        final Intent intent = createIntent(activityClass, context, (Element) tag);
        intent.putExtra(EXTRA_KEY_ELEMENT_IS_STATE_OR_LABEL,
                !tag.isFeedFolder() && !ItemState.READING_LIST.getId().equals(tag.getId()));
        return intent;
    }
	
	protected void startActivity(Class<? extends Activity> activityClass, Tag tag) {
		startActivity(createIntent(activityClass, this, tag));		
	}

	public ElementId getIntentElementId() {
		return new ElementId(getIntent().getExtras().getString(EXTRA_KEY_ELEMENT_ID));
	}

	protected long getIntentElementKey() {
		return getIntent().getExtras().getLong(EXTRA_KEY_ELEMENT_KEY, -1);		
	}
    
    protected void setIntentElementKey(long elementKey) {
        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            extras.putLong(EXTRA_KEY_ELEMENT_KEY, elementKey);
        }
    }
	
	public String getIntentElementTitle() {
		final Bundle extras = getIntent().getExtras();
		return extras != null ? extras.getString(EXTRA_KEY_ELEMENT_TITLE) : null;
	}
	
	public boolean getIntentElementIsStateOrLabel() {
		return getIntent().getExtras().getBoolean(EXTRA_KEY_ELEMENT_IS_STATE_OR_LABEL, false);
	}

    public boolean getIntentElementIsFolder() {
        return getIntentElementId().getType() == ElementType.LABEL && !getIntentElementIsStateOrLabel();
    }

    public boolean getIntentLaunchedByShortcut() {
        final Bundle extras = getIntent().getExtras();
        return extras != null && extras.getBoolean(EXTRA_KEY_LAUNCHED_BY_SHORTCUT, false);
    }
	
	/*
	 * dialog handling
	 */

    protected void showDialogOrIgnore(int id) {
        try {
            showDialog(id);
        } catch (WindowManager.BadTokenException ex) {
            Log.e(LOG_TAG, "Failed to show dialog " + id + " due to invalid window token", ex);
        }
    }

	@Override
	protected Dialog onCreateDialog(int id) {
        final Dialog dlg = accessor.onCreateDialog(id);
        return dlg != null ? dlg : super.onCreateDialog(id);
	}

    @Override
	public void showWaitDialog() {
        try {
		    accessor.showWaitDialog();
        } catch (WindowManager.BadTokenException ex) {
            Log.i(LOG_TAG, "Failed to show wait dialog due to invalid window.");
        }
	}

    @Override
	public void dismissWaitDialog() {
		accessor.dismissWaitDialog();
	}

    protected void setNoContentMsg(int msgId) {
        final TextView noContentMsgView = (TextView) findViewById(R.id.no_content_msg);
        noContentMsgView.setText(msgId);
    }

	/*
	 * handle configuration change
	 */

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return this;
    }

	protected void handleNonConfigurationInstance(Object instance) {
        final Activity prevInstance = (Activity) instance;
		accessor = prevInstance.accessor;
		accessor.setActivity(this);
	}

	/*
	 * inner classes
	 */

    public static interface OnViewOperationFinishedListener {
        boolean onViewOperationFinished();
    }
	
	public abstract class ViewOperation<Result> {
		public void run(OnViewOperationFinishedListener listener) {
            //noinspection unchecked
            new ViewOperationTask<Result>(this, listener).execute();
		}

		protected void onPreExecute() {
            Log.d(getClass().getName(), "loading/updating content");
            showPanel(PANEL_CONTENT_LOADING);
		}

		@SuppressWarnings("UnusedParameters")
        protected Result doInBackground(Boolean... ignore) {
            final SQLiteDatabase readOnlyDb = getReadOnlyDb();
            onPreFetchData();
            return fetchData(readOnlyDb);
		}
		
		protected void onPostExecute(Result result, OnViewOperationFinishedListener listener) {
            Log.d(getClass().getName(), "loading/updating done");
			hasContent = updateView(result);
            boolean updatePanel = true;
            if (listener != null) {
                updatePanel = listener.onViewOperationFinished();
            }
            if (updatePanel) {
			    showPanel(hasContent ? PANEL_CONTENT : PANEL_NO_CONTENT);
            }
		}

        protected void onPreFetchData() {
            Activity.this.onPreFetchData();
        }

		protected abstract Result fetchData(SQLiteDatabase readOnlyDb);
		protected abstract boolean updateView(Result result);
	}
	
	class ViewOperationTask<Result> extends AsyncTask<Boolean, Integer, Result> {
		private final ViewOperation<Result> op;
        private final OnViewOperationFinishedListener listener;
		
		public ViewOperationTask(ViewOperation<Result> op, OnViewOperationFinishedListener listener) {
			this.op = op;
            this.listener = listener;
		}

		@Override
		protected void onPreExecute() {
			op.onPreExecute();
		}

		@Override
		protected Result doInBackground(Boolean... params) {
			return op.doInBackground();
		}

		@Override
		protected void onPostExecute(Result result) {
			op.onPostExecute(result, listener);
		}
	}

    class OnViewLoadedListener implements OnViewOperationFinishedListener {
        @Override
        public boolean onViewOperationFinished() {
            return onViewLoaded();
        }
    }

    class OnViewUpdatedListener implements OnViewOperationFinishedListener {
        @Override
        public boolean onViewOperationFinished() {
            return onViewUpdated();
        }
    }

	class LongOperationTask<Param, Result> extends AsyncTask<Param, Integer, Result> {
		private final LongOperation<Param, Result> op;
		
		public LongOperationTask(LongOperation<Param, Result> op) {
			this.op = op;
		}

		@Override
		protected void onPreExecute() {
			op.onPreExecute();
		}

		@Override
		protected Result doInBackground(Param... params) {
			return op.run(params);
		}

		@Override
		protected void onPostExecute(Result result) {
			op.onPostExecute(result);
		}				
	}
	
	public abstract class LongOperation<Param, Result> {
		protected boolean isAsynchronous() {
			return true;
		}
		
		public void execute(Param... params) {
			if (isAsynchronous()) {
				new LongOperationTask<Param, Result>(this).execute(params);
			} else {
				onPreExecute();
				final Result result = run(params);
				onPostExecute(result);
			}
		}
		
		protected abstract Result run(Param... params);
		
		protected void onPreExecute() {
			if (isAsynchronous()) {
				showWaitDialog();
			}
		}

		@SuppressWarnings("UnusedParameters")
        protected void onPostExecute(Result result) {
			if (isAsynchronous()) {
				dismissWaitDialog();
			}
		}		
	}
	
	public abstract class LongDbOperation<Param, Result> extends LongOperation<Param, Result> {

		@Override
		protected Result run(Param... params) {
			return run(getDb(), params);
		}
		
		protected abstract Result run(SQLiteDatabase db, Param... params);
	}
}
