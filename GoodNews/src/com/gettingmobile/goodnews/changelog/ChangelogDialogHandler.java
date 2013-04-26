package com.gettingmobile.goodnews.changelog;

import android.app.Activity;
import android.view.View;
import android.webkit.WebView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import com.gettingmobile.android.app.AdaptiveDialogHandler;
import com.gettingmobile.goodnews.R;

public final class ChangelogDialogHandler extends AdaptiveDialogHandler
        implements CompoundButton.OnCheckedChangeListener {
    public static void start(Activity activity) {
        start(activity, ChangelogDialogHandler.class);
    }
    
    public ChangelogDialogHandler() {
        super(R.layout.changelog);
    }

    @Override
    protected void onCreate(View view) {
        /*
         * init dialog
         */
        getDialog().setTitle(R.string.changelog_title);
        getDialog().getMiddleButton().setText(R.string.close);
        getDialog().getMiddleButton().setVisibility(View.VISIBLE);
        
        /*
         * init checkbox
         */
        final CheckBox autoOptionView = ((CheckBox) view.findViewById(R.id.changelog_auto_option));
        autoOptionView.setChecked(getDialog().getApp().getSettings().shouldShowChangelogAutomatically());
        autoOptionView.setOnCheckedChangeListener(this);

        /*
         * init changelog view
         */
        final WebView contentView = (WebView) view.findViewById(R.id.content);
        contentView.getSettings().setJavaScriptEnabled(true);
        contentView.addJavascriptInterface(
                "r" + Integer.toString(getDialog().getApp().getSettings().getPreviousVersionCode()),
                "prevVersionCode");
        contentView.loadUrl("file:///android_asset/changelog/changelog-" +
                getDialog().getApp().getString(R.string.language_code) + ".xhtml");
        contentView.setBackgroundColor(0x00000000);
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (compoundButton.getId() == R.id.changelog_auto_option) {
            getDialog().getApp().getSettings().setShouldShowChangelogAutomatically(b);
        }
    }
}
