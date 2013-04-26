package com.gettingmobile.goodnews.settings;

import android.preference.Preference;
import android.preference.PreferenceGroup;
import com.gettingmobile.android.app.DialogFactory;
import com.gettingmobile.android.util.ApiLevel;
import com.gettingmobile.goodnews.R;

public final class ItemViewSettingsHandler extends ViewSettingsHandler {
    private static final int ITEM_ACTION_COUNT_WARNING_LEVEL = 4;

    @Override
    protected int getPrefResourceId() {
        return R.xml.pref_view_item_view;
    }

    @Override
    public void setup(final SettingsManager m) {
        super.setup(m);

        if (ApiLevel.isAtLeast(11)) {
            final PreferenceGroup itemActionsGroup = (PreferenceGroup) m.findPreference("item_view_actions");
            if (itemActionsGroup != null) {
                final Preference.OnPreferenceChangeListener onItemActionsChangeListener = new Preference.OnPreferenceChangeListener() {
                    private final int startCount = m.getApp().getSettings().getItemActionButtonCount();

                    @Override
                    public boolean onPreferenceChange(Preference preference, Object o) {
                        if (startCount < ITEM_ACTION_COUNT_WARNING_LEVEL &&
                                (m.getApp().getSettings().getItemActionButtonCount() + 1) == ITEM_ACTION_COUNT_WARNING_LEVEL) {
                            DialogFactory.showConfirmationDialog(m.getActionContext().getActivity(),
                                    R.string.pref_item_view_actions, R.string.pref_item_view_actions_warning,
                                    R.string.ok);
                        }
                        return true;
                    }
                };
                for (int i = 0; i < itemActionsGroup.getPreferenceCount(); ++i) {
                    itemActionsGroup.getPreference(i).setOnPreferenceChangeListener(onItemActionsChangeListener);
                }
            }
        }
    }
}
