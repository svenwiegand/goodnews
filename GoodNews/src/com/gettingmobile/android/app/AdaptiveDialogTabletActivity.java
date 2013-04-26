package com.gettingmobile.android.app;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public final class AdaptiveDialogTabletActivity extends FragmentActivity {
    public AdaptiveDialogTabletActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new AdaptiveDialogTabletFragment().show(getSupportFragmentManager(), null);
    }
}
