package com.gettingmobile.goodnews.settings;

public abstract class SettingsHandler {
    public abstract int[] getPreferenceResourceIds();
    
    public void setup(SettingsManager m) {
        // nothing to do by default
    }

    public void cleanup() {
        // nothing to do by default
    }
}
