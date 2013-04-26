package com.gettingmobile.goodnews.tip;

import android.app.Activity;

public class AbstractTip implements Tip {
    private final String id;
    private final int flags;

    public AbstractTip(String id, int flags) {
        this.id = id;
        this.flags = flags;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean hasFlags(int flags) {
        return (this.flags & flags) == flags;
    }
}
