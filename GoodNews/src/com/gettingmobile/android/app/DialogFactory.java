package com.gettingmobile.android.app;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;
import com.gettingmobile.goodnews.R;

public class DialogFactory {
    private final static String LOG_TAG = "goodnews.DialogFactory";

    public static AlertDialog enableDialogClickLinks(AlertDialog dlg) {
        final TextView textView = (TextView) dlg.findViewById(android.R.id.message);
        if (textView != null) {
            textView.setMovementMethod(LinkMovementMethod.getInstance());
        }
        return dlg;
    }

    public static SpannableString getLinkifiedText(Context context, int textId) {
        return getLinkifiedText(context.getText(textId));
    }

    public static SpannableString getLinkifiedText(CharSequence text) {
        final SpannableString msg = new SpannableString(text);
        Linkify.addLinks(msg, Linkify.ALL);
        return msg;
    }

    /*
     * confirmation dialogs
     */

    public static AlertDialog.Builder buildConfirmationDialog(Context context, int titleId, int msgId) {
        return new AlertDialog.Builder(context)
            .setTitle(titleId)
            .setMessage(getLinkifiedText(context, msgId));
    }

    public static void showConfirmationDialog(Context context, int titleId, int msgId, int positiveBtnTextId) {
        try {
            enableDialogClickLinks(
                    buildConfirmationDialog(context, titleId, msgId).setPositiveButton(positiveBtnTextId, null).show());
        } catch (WindowManager.BadTokenException ex) {
            Log.e(LOG_TAG, "Failed to show confirmation dialog due to invalid window token", ex);
        }
    }

    public static AlertDialog.Builder buildYesNoDialog(
            Context context, int titleId, int msgId, DialogInterface.OnClickListener yesListener) {
        return new AlertDialog.Builder(context)
            .setTitle(titleId)
            .setMessage(getLinkifiedText(context, msgId))
            .setNegativeButton(R.string.no, null)
            .setPositiveButton(R.string.yes, yesListener);
    }

    public static void showYesNoDialog(
            Context context, int titleId, int msgId, DialogInterface.OnClickListener yesListener) {
        enableDialogClickLinks(buildYesNoDialog(context, titleId, msgId, yesListener).show());
    }

    public static AlertDialog.Builder buildContinueDialog(
            Context context, int titleId, int msgId, DialogInterface.OnClickListener continueListener) {
        return new AlertDialog.Builder(context)
            .setTitle(titleId)
            .setMessage(getLinkifiedText(context, msgId))
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.cont, continueListener);
    }

    /*
     * progress dialog
     */

    public static ProgressDialog createProgressDialog(
            Context context, boolean spinner, int titleId, int msgId, boolean cancelable) {
        final ProgressDialog dlg = new ExtendedProgressDialog(context, cancelable);
        dlg.setProgressStyle(spinner ? ProgressDialog.STYLE_SPINNER : ProgressDialog.STYLE_HORIZONTAL);
        dlg.setIndeterminate(true);
        if (titleId != 0) {
            dlg.setTitle(titleId);
        }
        if (msgId != 0) {
            dlg.setMessage(context.getString(msgId));
        }

        if (!spinner) {
            dlg.setProgressDrawable(context.getResources().getDrawable(R.drawable.progress_bar));
        }
        return dlg;
    }

	public static ProgressDialog createIndeterminateProgressDialog(
            Context context, int titleId, int msgId, boolean cancelable) {
        return createProgressDialog(context, true, titleId, msgId, cancelable);
	}

    public static ProgressDialog createProgressDialog(Context context, int titleId, int msgId, boolean cancelable) {
        return createProgressDialog(context, false, titleId, msgId, cancelable);
    }

    /*
     * error dialogs
     */

    public static AlertDialog.Builder buildErrorDialog(Context context, int titleId, CharSequence msg) {
        return new AlertDialog.Builder(context)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setTitle(titleId)
            .setMessage(getLinkifiedText(msg));
    }

    public static void showErrorDialog(
            Context context, int titleId, CharSequence msg, DialogInterface.OnClickListener listener) {
        try {
            enableDialogClickLinks(buildErrorDialog(context, titleId, msg)
                .setNeutralButton(R.string.ok, listener)
                .show());
        } catch (WindowManager.BadTokenException ex) {
            Log.e(LOG_TAG, "Failed to show error dialog due to invalid window token", ex);
        }
    }

    public static void showErrorDialog(Context context, int titleId, CharSequence msg) {
        showErrorDialog(context, titleId, msg, null);
    }

    /**
     * Safe version of {@code Dialog.dismiss} that will <em>not</em> throw an exception if called for a dialog that is
     * not shown, because this situation may occur for dialogs which are closed via a callback exactly in the moment
     * after a recreation of the activity (e.g. after a screen rotation).
     * @param dialog the dialog to be dismissed.
     */
    public static void dismissDialogSafely(DialogInterface dialog) {
        try {
            dialog.dismiss();
        } catch (IllegalArgumentException ex) {
            // ignore
        }
    }
}
