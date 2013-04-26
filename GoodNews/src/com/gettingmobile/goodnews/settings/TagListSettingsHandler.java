package com.gettingmobile.goodnews.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import com.gettingmobile.android.app.DialogFactory;
import com.gettingmobile.android.app.actions.GenerateTeaserAction;
import com.gettingmobile.android.util.ApiLevel;
import com.gettingmobile.goodnews.R;

public final class TagListSettingsHandler extends ViewSettingsHandler {
    @Override
    protected int getPrefResourceId() {
        return R.xml.pref_view_tag_list;
    }
}
