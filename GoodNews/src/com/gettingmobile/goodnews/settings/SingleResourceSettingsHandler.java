package com.gettingmobile.goodnews.settings;

abstract class SingleResourceSettingsHandler extends SettingsHandler {
    protected abstract int getPrefResourceId();

    @Override
    public int[] getPreferenceResourceIds() {
        return new int[]{getPrefResourceId()};
    }
}
