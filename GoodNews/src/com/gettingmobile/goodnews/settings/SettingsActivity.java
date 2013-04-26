package com.gettingmobile.goodnews.settings;

import android.os.Bundle;
import com.gettingmobile.goodnews.R;

import java.util.List;

public final class SettingsActivity extends FullSettingsActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setTitle(R.string.preferences);
    }

    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.preference_headers, target);
    }
}
