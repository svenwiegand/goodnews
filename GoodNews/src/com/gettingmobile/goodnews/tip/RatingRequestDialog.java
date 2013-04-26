package com.gettingmobile.goodnews.tip;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import com.gettingmobile.android.app.actions.ActionContext;
import com.gettingmobile.android.app.actions.MarketDetailsAction;
import com.gettingmobile.goodnews.Application;
import com.gettingmobile.goodnews.R;
import com.gettingmobile.goodnews.settings.Settings;

import java.util.Calendar;

public class RatingRequestDialog {
    private static final String KEY_TIMESTAMP = "rating_request_next_time";
    private static final String KEY_REQUEST_AGAIN = "rating_request_again";
    private static final int DAYS = 5;
    private static boolean check = true;

    public static void showIfApplicable(ActionContext<? extends Application> context) {
        if (check) {
            final Settings settings = context.getApp().getSettings();
            final Calendar nextShowTime = settings.getTimestamp(KEY_TIMESTAMP);
            if (nextShowTime == null) {
                /*
                 * the app has been newly installed and we are called for the first time. So let's initialize
                 */
                rescheduleRequest(context.getApp());
            } else {
                /*
                 * check whether we should display
                 */
                if (settings.getBoolean(KEY_REQUEST_AGAIN)) {
                    if (nextShowTime.before(Calendar.getInstance())) {
                        showDialog(context);
                    }
                } else {
                    check = false;
                }
            }
        }
    }

    protected static void rescheduleRequest(Application app) {
        app.getSettings().setTimestampFromNow(KEY_TIMESTAMP, Calendar.DAY_OF_MONTH, DAYS);
        app.getSettings().setBoolean(KEY_REQUEST_AGAIN, true);
    }

    protected static void disableRequests(Application app) {
        app.getSettings().setBoolean(KEY_REQUEST_AGAIN, false);
        check = false;
    }

    protected static void showDialog(ActionContext<? extends Application> context) {
        /*
         * build the layout
         */
        final LayoutInflater layoutInflater = (LayoutInflater)
                context.getApp().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View view = layoutInflater.inflate(R.layout.rating_request, null);

        /*
         * build the dialog
         */
        final AlertDialog dlg = new AlertDialog.Builder(context.getActivity())
            .setTitle(R.string.rating_request_title)
            .setIcon(R.drawable.icon)
            .setView(view)
            .create();

        /*
         * register the listeners
         */
        final View.OnClickListener clickListener = new ButtonClickListener(context, dlg);
        view.findViewById(R.id.rating_request_rate).setOnClickListener(clickListener);
        view.findViewById(R.id.rating_request_later).setOnClickListener(clickListener);
        view.findViewById(R.id.rating_request_no).setOnClickListener(clickListener);

        /*
         * show the dialog
         */
        dlg.show();
    }

    /*
     * inner classes
     */

    static class ButtonClickListener implements View.OnClickListener {
        private final ActionContext<? extends Application> context;
        private final Dialog dlg;

        public ButtonClickListener(ActionContext<? extends Application> context, Dialog dlg) {
            this.context = context;
            this.dlg = dlg;
        }

        @Override
        public void onClick(View view) {
            dlg.dismiss();
            if (view.getId() == R.id.rating_request_rate) {
                new MarketDetailsAction(context.getApp().getPackageName()).onFired(context);
                disableRequests(context.getApp());
            } else if (view.getId() == R.id.rating_request_later) {
                rescheduleRequest(context.getApp());
            } else if (view.getId() == R.id.rating_request_no) {
                disableRequests(context.getApp());
            }
        }
    }
}
