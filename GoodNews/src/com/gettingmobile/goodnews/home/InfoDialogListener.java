package com.gettingmobile.goodnews.home;

import android.content.DialogInterface;

public interface InfoDialogListener {
    int DISMISS_TYPE_NEUTRAL = 0;
    int DISMISS_TYPE_POSITIVE = 1;
    int DISMISS_TYPE_NEGATIVE = 2;
    
    void onDismissInfoDialog(DialogInterface dlg, int dismissType);
}
