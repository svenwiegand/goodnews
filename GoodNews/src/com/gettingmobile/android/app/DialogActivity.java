package com.gettingmobile.android.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public final class DialogActivity extends Activity implements DialogInterface.OnDismissListener {
    private static final String LOG_TAG = "goodnews.DialogActivity";
    public static final String EXTRA_DIALOG_TITLE = IntentConstants.EXTRA_BASE + "DIALOG_TITLE";
    public static final String EXTRA_DIALOG_MESSAGE = IntentConstants.EXTRA_BASE + "DIALOG_MESSAGE";
    public static final String EXTRA_DIALOG_POSITIVE_BUTTON_TEXT = IntentConstants.EXTRA_BASE + "DIALOG_POSITIVE_BUTTON_TEXT";
    public static final String EXTRA_DIALOG_POSITIVE_BUTTON_INTENT = IntentConstants.EXTRA_BASE + "DIALOG_POSITIVE_BUTTON_INTENT";
    public static final String EXTRA_DIALOG_NEGATIVE_BUTTON_TEXT = IntentConstants.EXTRA_BASE + "DIALOG_NEGATIVE_BUTTON_TEXT";
    public static final String EXTRA_DIALOG_NEGATIVE_BUTTON_INTENT = IntentConstants.EXTRA_BASE + "DIALOG_NEGATIVE_BUTTON_INTENT";
    public static final String EXTRA_DIALOG_NEUTRAL_BUTTON_TEXT = IntentConstants.EXTRA_BASE + "DIALOG_NEUTRAL_BUTTON_TEXT";
    public static final String EXTRA_DIALOG_NEUTRAL_BUTTON_INTENT = IntentConstants.EXTRA_BASE + "DIALOG_NEUTRAL_BUTTON_INTENT";

    public static Intent createIntent(Context context, CharSequence title, CharSequence message,
                                      CharSequence positiveButtonText, PendingIntent positiveButtonIntent,
                                      CharSequence negativeButtonText, PendingIntent negativeButtonIntent,
                                      CharSequence neutralButtonText, PendingIntent neutralButtonIntent) {
        final Intent intent = new Intent(context, DialogActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        final Bundle extras = new Bundle();
        extras.putCharSequence(EXTRA_DIALOG_TITLE, title);
        extras.putCharSequence(EXTRA_DIALOG_MESSAGE, message);
        extras.putCharSequence(EXTRA_DIALOG_POSITIVE_BUTTON_TEXT, positiveButtonText);
        extras.putParcelable(EXTRA_DIALOG_POSITIVE_BUTTON_INTENT, positiveButtonIntent);
        extras.putCharSequence(EXTRA_DIALOG_NEGATIVE_BUTTON_TEXT, negativeButtonText);
        extras.putParcelable(EXTRA_DIALOG_NEGATIVE_BUTTON_INTENT, negativeButtonIntent);
        extras.putCharSequence(EXTRA_DIALOG_NEUTRAL_BUTTON_TEXT, neutralButtonText);
        extras.putParcelable(EXTRA_DIALOG_NEUTRAL_BUTTON_INTENT, neutralButtonIntent);
        intent.putExtras(extras);
        return intent;
    }

    public static Intent createIntent(Context context, int title, int message,
                                      int positiveButtonText, PendingIntent positiveButtonIntent,
                                      int negativeButtonText, PendingIntent negativeButtonIntent,
                                      int neutralButtonText, PendingIntent neutralButtonIntent) {
        return createIntent(context, context.getText(title), context.getText(message),
                (positiveButtonText != 0 ? context.getText(positiveButtonText) : null), positiveButtonIntent,
                (negativeButtonText != 0 ? context.getText(negativeButtonText) : null), negativeButtonIntent,
                (neutralButtonText != 0 ? context.getText(neutralButtonText) : null), neutralButtonIntent);
    }

    public static PendingIntent createPendingIntent(Context context, Intent intent) {
        return PendingIntent.getActivity(context, 0, intent, 0);
    }

    @Override
    protected void onResume() {
        super.onResume();

        final Bundle extras = getIntent().getExtras();
        if (extras == null) {
            finish();
        } else {
            final CharSequence title = extras.getCharSequence(EXTRA_DIALOG_TITLE);
            final CharSequence message = extras.getCharSequence(EXTRA_DIALOG_MESSAGE);
            if (title == null || message == null) {
                finish();
            } else {
                final CharSequence positiveButtonText = extras.getCharSequence(EXTRA_DIALOG_POSITIVE_BUTTON_TEXT);
                final PendingIntent positiveButtonIntent = (PendingIntent) extras.getParcelable(EXTRA_DIALOG_POSITIVE_BUTTON_INTENT);
                final CharSequence negativeButtonText = extras.getCharSequence(EXTRA_DIALOG_NEGATIVE_BUTTON_TEXT);
                final PendingIntent negativeButtonIntent = (PendingIntent) extras.getParcelable(EXTRA_DIALOG_NEGATIVE_BUTTON_INTENT);
                final CharSequence neutralButtonText = extras.getCharSequence(EXTRA_DIALOG_NEUTRAL_BUTTON_TEXT);
                final PendingIntent neutralButtonIntent = (PendingIntent) extras.getParcelable(EXTRA_DIALOG_NEUTRAL_BUTTON_INTENT);
                showDialog(title, message,
                        positiveButtonText, positiveButtonIntent,
                        negativeButtonText, negativeButtonIntent,
                        neutralButtonText, neutralButtonIntent);
            }
        }
    }

    protected void showDialog(CharSequence title, CharSequence message,
                              CharSequence positiveButtonText, final PendingIntent positiveButtonIntent,
                              CharSequence negativeButtonText, final PendingIntent negativeButtonIntent,
                              CharSequence neutralButtonText, final PendingIntent neutralButtonIntent) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this).
                setTitle(title).
                setMessage(message);
        if (positiveButtonText != null) {
            builder.setPositiveButton(positiveButtonText, new OnButtonClickListener(positiveButtonIntent));
        }
        if (negativeButtonText != null) {
            builder.setNegativeButton(negativeButtonText, new OnButtonClickListener(negativeButtonIntent));
        }
        if (neutralButtonText != null) {
            builder.setNeutralButton(neutralButtonText, new OnButtonClickListener(neutralButtonIntent));
        }
        builder.show().setOnDismissListener(this);
    }

    @Override
    public void onDismiss(DialogInterface dialogInterface) {
        finish();
    }

    /*
     * inner classes
     */

    final class OnButtonClickListener implements DialogInterface.OnClickListener {
        private final PendingIntent intent;

        OnButtonClickListener(PendingIntent intent) {
            this.intent = intent;
        }

        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            try {
                if (intent != null) {
                    try {
                        intent.send();
                    } catch (PendingIntent.CanceledException ex) {
                        Log.e(LOG_TAG, "button intent failed", ex);
                    }
                }
            } finally {
                dialogInterface.dismiss();
            }
        }
    }
}
