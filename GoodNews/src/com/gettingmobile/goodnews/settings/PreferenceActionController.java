package com.gettingmobile.goodnews.settings;

import android.preference.Preference;
import com.gettingmobile.android.app.actions.Action;
import com.gettingmobile.android.app.actions.ActionContext;
import com.gettingmobile.android.app.actions.SimpleActionController;

import java.util.Map;

public class PreferenceActionController extends SimpleActionController<String> {
    private final PreferenceProvider preferenceProvider;

    public PreferenceActionController(ActionContext<?> context, PreferenceProvider preferenceProvider) {
        super(context);
        this.preferenceProvider = preferenceProvider;
    }

    public void updateActionPreferenceStatus() {
        for (Map.Entry<String, Action<?>> entry : actions.entrySet()) {
            final Preference pref = preferenceProvider.findPreference(entry.getKey());
            if (pref != null) {
                @SuppressWarnings("unchecked")
                final int state = entry.getValue().getState((ActionContext) context);
                pref.setEnabled(state != Action.GONE && state != Action.DISABLED);
            }
        }
    }

    /*
     * inner classes
     */

    public interface PreferenceProvider {
        Preference findPreference(CharSequence key);
    }
}
