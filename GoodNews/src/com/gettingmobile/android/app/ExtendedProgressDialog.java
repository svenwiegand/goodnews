package com.gettingmobile.android.app;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.KeyEvent;
import com.gettingmobile.goodnews.R;

/**
 * Equals to the standard ProgressDialog, but prevents it from being canceled by the search button.
 */
public class ExtendedProgressDialog extends ProgressDialog {
    public ExtendedProgressDialog(Context context, boolean cancelable) {
        super(context);
        setCancelable(cancelable);
        if (cancelable) {
            setButton(DialogInterface.BUTTON_NEGATIVE, context.getText(R.string.cancel), new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    cancel();
                }
            });
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_SEARCH:
                return true;
            default:
                return super.dispatchKeyEvent(event);
        }
    }
}
