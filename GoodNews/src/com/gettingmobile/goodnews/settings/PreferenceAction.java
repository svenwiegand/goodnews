package com.gettingmobile.goodnews.settings;

import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.util.Log;
import com.gettingmobile.android.app.DialogFactory;
import com.gettingmobile.android.app.actions.AbstractAction;
import com.gettingmobile.android.app.actions.ActionContext;
import com.gettingmobile.goodnews.Application;

import java.text.MessageFormat;

public abstract class PreferenceAction extends AbstractAction<Application> {
    protected final int confirmationTitleId;
    protected final int confirmationMsgId;
    protected final int errorTitleId;
    protected final int errorMsgId;

    public PreferenceAction(int confirmationTitleId, int confirmationMsgId, int errorTitleId, int errorMsgId) {
        this.confirmationTitleId = confirmationTitleId;
        this.confirmationMsgId = confirmationMsgId;
        this.errorTitleId = errorTitleId;
        this.errorMsgId = errorMsgId;
    }

    public PreferenceAction(int confirmationTitleId, int confirmationMsgId) {
        this(confirmationTitleId, confirmationMsgId, 0, 0);
    }

    public PreferenceAction(int confirmationTitleId, int confirmationMsgId, int errorMsgId) {
        this(confirmationTitleId, confirmationMsgId, confirmationTitleId, errorMsgId);
    }

    public PreferenceAction() {
        this(0, 0);
    }

    @Override
    public boolean onFired(final ActionContext<? extends Application> applicationActionContext) {
        if (confirmationTitleId != 0 && confirmationMsgId != 0) {
            showConfirmationDialog(applicationActionContext);
        } else {
            perform(applicationActionContext);
        }
        return true;
    }

    protected void showConfirmationDialog(final ActionContext<? extends Application> context) {
        DialogFactory.buildYesNoDialog(
                context.getActivity(), confirmationTitleId,
                confirmationMsgId, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        perform(context);
                    }
                }
        ).show();
    }

    protected void perform(final ActionContext<? extends Application> context) {
        context.showWaitDialog();
        //noinspection unchecked
        new AsyncTask<Object, Object, Throwable>() {
            @Override
            protected Throwable doInBackground(Object... objects) {
                try {
                    asyncPerform(context);
                    return null;
                } catch (Throwable error) {
                    return error;
                }
            }

            @Override
            protected void onPostExecute(Throwable error) {
                context.dismissWaitDialog();
                if (error == null) {
                    onSuccess(context);
                } else {
                    onError(context, error);
                }
                super.onPostExecute(error);
            }

            @Override
            protected void onCancelled() {
                context.dismissWaitDialog();
                super.onCancelled();
            }
        }.execute();
    }

    protected void asyncPerform(final ActionContext<? extends Application> context) throws Throwable {
        // nothing to be done by default
    }

    protected void onSuccess(ActionContext<? extends Application> context) {
        // nothing to be done by default
    }

    protected void onError(ActionContext<? extends Application> context, Throwable error) {
        showErrorDialog(context, error);
    }

    protected String getErrorMsg(ActionContext<? extends Application> context, Throwable error) {
        return MessageFormat.format(context.getActivity().getString(errorMsgId), error.getLocalizedMessage());
    }

    protected void showErrorDialog(final ActionContext<? extends Application> context, Throwable error) {
        try {
            final String errorMsg = getErrorMsg(context, error);
            Log.e(getClass().getSimpleName(), errorMsg, error);
            if (!context.getActivity().isFinishing()) {
                DialogFactory.showErrorDialog(
                        context.getActivity(), errorTitleId, errorMsg);
            }
        } catch (Resources.NotFoundException exception) {
            Log.e(getClass().getSimpleName(), "Operation failed! (no error message displayed)", error);
        }
    }
}
