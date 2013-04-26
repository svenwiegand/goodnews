package com.gettingmobile.android.app.actions;

import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.util.Log;
import com.gettingmobile.android.app.DialogFactory;
import com.gettingmobile.goodnews.R;
import com.gettingmobile.goodnews.SimpleActivityAccessor;

public class ActionContextActivityAccessor<T extends Application, U extends Activity>
        extends SimpleActivityAccessor<U> implements ActionContext<T> {
    private static final String LOG_TAG = "goodnews.ActionContextActivityAccessor";
    private static final int DIALOG_WAIT = -1;

    public ActionContextActivityAccessor(U activity) {
        super(activity);
    }

    public Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_WAIT:
                return DialogFactory.createProgressDialog(getActivity(), true, 0, R.string.wait, false);
            default:
                return null;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public T getApp() {
        return (T) getActivity().getApplication();
    }

    @Override
    public U getActivity() {
        return super.getActivity();
    }

    @Override
    public void showWaitDialog() {
        getActivity().showDialog(DIALOG_WAIT);
    }

    @Override
    public void dismissWaitDialog() {
        dismissDialogSafely(DIALOG_WAIT);
    }

    /**
     * Safe version of {@code dismissDialog} that will <em>not</em> throw an exception if called for a dialog that is 
     * not shown, because this situation may occur for dialogs which are closed via a callback exactly in the moment
     * after a recreation of the activity (e.g. after a screen rotation). 
     * @param id the ID of the dialog to be dismissed.
     */    
    public void dismissDialogSafely(int id) {
        try {
            getActivity().dismissDialog(id);
        } catch (IllegalArgumentException ex) {
            Log.w(LOG_TAG, "Failed to dismiss dialog.", ex);
        }        
    }
}
