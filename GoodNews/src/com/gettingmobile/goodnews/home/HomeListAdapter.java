package com.gettingmobile.goodnews.home;

import com.gettingmobile.android.widget.ListItemCursorAdapter;
import com.gettingmobile.android.widget.StaticTextListSectionHeaderViewType;
import com.gettingmobile.goodnews.R;

final class HomeListAdapter extends ListItemCursorAdapter {
    public static final int VIEW_TYPE_SYNC_STATUS = VIEW_TYPE_CUSTOM_FIRST_ID;
    public static final int VIEW_TYPE_SUBSCRIPTIONS_HEADER = VIEW_TYPE_SYNC_STATUS + 1;

    public HomeListAdapter(HomeActivity activity) {
        super(activity);
    }

    public void init(HomeActivity activity) {
        registerViewType(new HomeRowViewType(activity.getApp(), activity));
        registerViewType(new SyncStatusHeaderViewType(activity));
        registerViewType(new StaticTextListSectionHeaderViewType(
                VIEW_TYPE_SUBSCRIPTIONS_HEADER, false, R.string.subscriptions));
    }
}
