package com.gettingmobile.goodnews.tip;

import android.app.Activity;

public interface Tip {
    int FLAG_NONE = 0;
    int FLAG_FULL_SCREEN = 1;
    int FLAG_VISUAL = 2;
    int FLAG_AUTOMATIC = 4;

    boolean hasFlags(int flags);
    String getId();
}
