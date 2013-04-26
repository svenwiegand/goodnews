package com.gettingmobile.goodnews.home;

import android.view.View;
import android.widget.TextView;
import com.gettingmobile.android.widget.SimpleListItemViewType;
import com.gettingmobile.goodnews.Activity;
import com.gettingmobile.goodnews.Application;
import com.gettingmobile.goodnews.R;
import com.gettingmobile.goodnews.tip.TipDialogHandler;
import com.gettingmobile.goodnews.util.ItemTimestampFormat;

import java.util.Calendar;

final class SyncStatusHeaderViewType extends SimpleListItemViewType {
    private final Activity activity;
    private final Application app;
    private final ItemTimestampFormat format;

    public SyncStatusHeaderViewType(Activity activity) {
        super(HomeListAdapter.VIEW_TYPE_SYNC_STATUS, true, R.layout.sync_status);
        this.activity = activity;
        this.app = activity.getApp();
        format = new ItemTimestampFormat(activity, false);
    }

    @Override
    public void bindView(View view, Object item) {
        super.bindView(view, item);
        updateView(view);
    }

    public void updateView(View view) {
        if (view == null)
            return;
        
        /*
         * set pull time
         */
        final Calendar lastSuccessfulPullTime = app.getSettings().getLastSuccessfulPullTimestamp();
        if (lastSuccessfulPullTime != null) {
            ((TextView) view.findViewById(R.id.pull_sync_status)).setText(
                    format.format(lastSuccessfulPullTime.getTime()));
        }

        /*
         * set push time
         */
        final TextView pushTimeView = (TextView) view.findViewById(R.id.push_sync_status);
        final Calendar lastSuccessfulPushTime = app.getSettings().getLastSuccessfulPushTimestamp();
        if (lastSuccessfulPushTime != null) {
            pushTimeView.setText(format.format(lastSuccessfulPushTime.getTime()));
        }

        final boolean requiresPush = app.getSyncService() != null && app.getSyncService().getService() != null &&
                app.getSyncService().getService().isPushRequired();
        pushTimeView.setCompoundDrawablesWithIntrinsicBounds(null, null, app.getThemeUtil().getThemeDrawable(pushTimeView,
                requiresPush ? R.attr.pushStatusRequiredIcon : R.attr.pushStatusIcon), null);

        /*
         * show tip on click
         */
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TipDialogHandler.start(activity, app.getTipManager(), "sync_status");
            }
        });
    }
}
