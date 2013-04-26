package com.gettingmobile.android.view;

import android.view.KeyEvent;

public final class CompatibleKeyEventHelper {
    private static final int FLAG_LONG_PRESS = 0x00000080;
    public static boolean isLongPress(KeyEvent event) {
        return (event.getFlags() & FLAG_LONG_PRESS) != 0;
    }
}
