package com.gettingmobile.goodnews.settings;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import com.gettingmobile.android.app.actions.Action;
import com.gettingmobile.android.app.actions.ActionContext;
import com.gettingmobile.goodnews.Application;

public final class SettingsFragment extends PreferenceFragment implements
        ActionContext<Application>, SettingsManager, PreferenceActionController.PreferenceProvider {
    protected static final String ARG_SETTINGS_HANDLER = "settingsHandler";
    protected final PreferenceActionController actionController = new PreferenceActionController(this, this);
    private SettingsHandler settingsHandler = null;

    /*
     * helpers
     */

    private SettingsHandler createSettingsHandler() {
        try {
            final Class<?> settingsHandlerClass = Class.forName(
                    SettingsHandler.class.getPackage().getName() + "." + getArguments().getString(ARG_SETTINGS_HANDLER));
            return (SettingsHandler) settingsHandlerClass.newInstance();
        } catch (Exception ex) {
            throw new RuntimeException("Failed to create SettingsHandler", ex);
        }
    }

    /*
     * life cycle
     */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        settingsHandler = createSettingsHandler();
        for (int resId : settingsHandler.getPreferenceResourceIds()) {
            addPreferencesFromResource(resId);
        }

        /*
         * We are using a trick for all the view settings to provide them all as preference screen in the global
         * settings, they are each wrapped into a preference screen which we would like to omit if they are used
         * standalone, which is the case here. So we take the single preference entry and -- if it is a preference
         * screen -- set it as the preference root.
         */
        final PreferenceScreen currentRoot = getPreferenceScreen();
        if (currentRoot.getPreferenceCount() == 1) {
            final Preference singlePref = currentRoot.getPreference(0);
            if (singlePref instanceof PreferenceScreen) {
                setPreferenceScreen((PreferenceScreen) singlePref);
            }
        }

        settingsHandler.setup(this);
        updateActionPreferenceStatus();
    }

    @Override
    public void onDestroy() {
        settingsHandler.cleanup();
        super.onDestroy();
    }

    /*
     * event handling
     */

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        return actionController.fireAction(preference.getKey()) || super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    /*
     * interface implementation
     */

    private SettingsActivity getSettingsActivity() {
        return (SettingsActivity) getActivity();
    }

    @Override
    public Application getApp() {
        return getSettingsActivity().getApp();
    }

    @Override
    public void showWaitDialog() {
        getSettingsActivity().showWaitDialog();
    }

    @Override
    public void dismissWaitDialog() {
        getSettingsActivity().dismissWaitDialog();
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
                final Activity activity = getActivity();
                if (activity != null && !activity.isFinishing()) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            actionController.updateActionPreferenceStatus();
                        }
                    });
                }
                return null;
            }
        }.execute();
    }
}
