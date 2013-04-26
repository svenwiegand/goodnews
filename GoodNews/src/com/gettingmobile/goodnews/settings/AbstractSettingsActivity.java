package com.gettingmobile.goodnews.settings;

import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import com.gettingmobile.android.app.actions.Action;
import com.gettingmobile.android.app.actions.ActionContext;
import com.gettingmobile.android.app.actions.ActionContextActivityAccessor;
import com.gettingmobile.goodnews.Application;
import roboguice.activity.RoboPreferenceActivity;

abstract class AbstractSettingsActivity extends RoboPreferenceActivity
        implements ActionContext<Application>, SettingsManager, PreferenceActionController.PreferenceProvider {
    protected final PreferenceActionController actionController = new PreferenceActionController(this, this);
    private ActionContextActivityAccessor<Application, AbstractSettingsActivity> accessor = null;

    public AbstractSettingsActivity() {
    }

    /*
     * lifecycle management
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
         * handle activity configuration changes
         */
        final Object nonConfigurationInstance = getLastNonConfigurationInstance();
        if (nonConfigurationInstance != null) {
            final AbstractSettingsActivity prevInstance = (AbstractSettingsActivity) nonConfigurationInstance;
            accessor = prevInstance.accessor;
            accessor.setActivity(this);
        } else {
            accessor = new ActionContextActivityAccessor<Application, AbstractSettingsActivity>(this);
        }
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return this;
    }

    /*
     * interface implementations
     */

    @Override
    public android.app.Activity getActivity() {
        return accessor.getActivity();
    }

    @Override
    public Application getApp() {
		return (Application) getApplication();
	}

    @Override
    public ActionContext<Application> getActionContext() {
        return this;
    }

    @Override
    public void registerChangeListener(String key, Preference.OnPreferenceChangeListener l) {
        final Preference pref = findPreference(key);
        pref.setOnPreferenceChangeListener(l);
    }

    @Override
    public void registerAction(String key, Action<?> action) {
        actionController.registerAction(key, action);
    }

    @Override
    public <A extends Action<?>> A registerAction(String key, Class<A> actionClass) {
        return actionController.registerAction(key, actionClass);
    }

    @Override
    public void unregisterAction(String key) {
        actionController.unregisterAction(key);
    }

    @Override
    public void updateActionPreferenceStatus() {
        //noinspection unchecked
        new AsyncTask<Object, Object, Object>() {
            @Override
            protected Object doInBackground(Object... objects) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        actionController.updateActionPreferenceStatus();
                    }
                });
                return null;
            }
        }.execute();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        return actionController.fireAction(preference.getKey()) ||
                super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    /*
     * dialog handling
     */

    @Override
    protected Dialog onCreateDialog(int id) {
        final Dialog dlg = accessor.onCreateDialog(id);
        return dlg != null ? dlg : super.onCreateDialog(id);
    }

    @Override
    public void showWaitDialog() {
        accessor.showWaitDialog();
    }

    @Override
    public void dismissWaitDialog() {
        accessor.dismissWaitDialog();
    }
}
