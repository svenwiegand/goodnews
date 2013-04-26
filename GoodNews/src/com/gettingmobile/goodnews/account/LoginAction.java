package com.gettingmobile.goodnews.account;

import android.content.DialogInterface;
import com.gettingmobile.android.app.DialogFactory;
import com.gettingmobile.android.app.actions.AbstractAction;
import com.gettingmobile.android.app.actions.ActionContext;
import com.gettingmobile.goodnews.Application;
import com.gettingmobile.goodnews.R;
import com.gettingmobile.google.reader.db.ItemTagChangeDatabaseAdapter;

public class LoginAction extends AbstractAction<Application> {
    @Override
    public boolean onFired(final ActionContext<? extends Application> context) {
        if (ItemTagChangeDatabaseAdapter.hasGlobalChanges(context.getApp().getDbHelper().getDatabase())) {
            DialogFactory.buildYesNoDialog(
                    context.getActivity(), R.string.account_change_title, R.string.account_change_confirmation,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            doLogin(context);
                        }
                    }).show();
        } else {
            doLogin(context);
        }
        return true;
    }

    protected void doLogin(final ActionContext<? extends Application> context) {
        context.getApp().getAccountHandler().promptAccount(context, new LoginCallback() {
            @Override
            public void onLoginStarted() {
                context.showWaitDialog();
            }

            @Override
            public void onLoginFinished(Throwable error) {
                context.dismissWaitDialog();
                if (error != null) {
                    DialogFactory.showErrorDialog(context.getActivity(),
                            R.string.login_title, context.getActivity().getString(R.string.login_failed));
                }
            }
        });
    }
}
