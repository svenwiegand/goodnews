package com.gettingmobile.goodnews.settings;

import android.preference.Preference;

import java.lang.reflect.Method;

public final class PreferenceUpdater {
    public static void updatePreferenceView(Preference preference) {
        try {
            final Class<? extends Preference> c = preference.getClass();
            final Method m = c.getMethod("notifyChanged");
            m.invoke(preference);
        } catch (Exception ex) {
            // this isn't important, so that we can safely ignore it
        }
    }
}
