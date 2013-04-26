package com.gettingmobile.goodnews.settings.storagestatistics;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import com.gettingmobile.android.app.AdaptiveDialogHandler;
import com.gettingmobile.goodnews.R;
import com.google.inject.Inject;

public final class StorageStatisticsDialogHandler extends AdaptiveDialogHandler {
    protected static final String EXTRA_KEY_STATISTICS = EXTRA_KEY_BASE + "STATISTICS";
    private final StorageStatistics statistics;

    public static void start(Activity activity, StorageStatistics statistics) {
        final Bundle extras = new Bundle();
        extras.putSerializable(EXTRA_KEY_STATISTICS, statistics);
        start(activity, StorageStatisticsDialogHandler.class, extras);
    }

    @Inject
    public StorageStatisticsDialogHandler(Bundle extras) {
        super(R.layout.storage_statistics);
        statistics = (StorageStatistics) extras.getSerializable(EXTRA_KEY_STATISTICS);
    }

    @Override
    protected void onCreate(View view) {
        /*
         * init dialog
         */
        getDialog().setTitle(R.string.pref_storage_statistics);
        getDialog().getMiddleButton().setText(R.string.close);
        getDialog().getMiddleButton().setVisibility(View.VISIBLE);

        /*
         * init changelog view
         */
        final WebView contentView = (WebView) view.findViewById(R.id.content);
        contentView.getSettings().setJavaScriptEnabled(true);
        contentView.addJavascriptInterface(statistics, "statistics");
        contentView.loadUrl("file:///android_asset/statistics/storage-statistics-" +
                getDialog().getApp().getString(R.string.language_code) + ".xhtml");
        contentView.setBackgroundColor(0x00000000);
    }
}
