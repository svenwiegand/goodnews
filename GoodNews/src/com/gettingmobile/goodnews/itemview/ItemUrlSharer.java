package com.gettingmobile.goodnews.itemview;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import com.actionbarsherlock.view.MenuItem;
import com.gettingmobile.goodnews.Application;
import com.gettingmobile.text.HtmlUtil;

import java.text.MessageFormat;

public abstract class ItemUrlSharer {
    protected static final String LOG_TAG = "goodnews.ItemUrlSharer";
    protected final Activity activity;
    protected final Intent intent;

    public ItemUrlSharer(Activity activity) {
        this.activity = activity;

        intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
    }

    private MessageFormat getContentFormat() {
        return ((Application) activity.getApplication()).getSettings().getShareContentFormat();
    }

    public void setItemInfo(String url, String title) {
        title = title != null ? HtmlUtil.removeTags(title) : null;
        final boolean hasTitle = title != null && title.length() > 0;
        Log.d(LOG_TAG, "setItemInfo: url=" + url + "; title=" + title);
        intent.putExtra(Intent.EXTRA_SUBJECT, hasTitle ? title : null);
        intent.putExtra(Intent.EXTRA_TEXT, hasTitle ? getContentFormat().format(new Object[] {url, title}) : url);
        onIntentChanged();
    }
    
    public void onPrepareMenu(MenuItem shareItem) {
        shareItem.setEnabled(hasUrl());
    }

    public abstract boolean handleAction();

    protected void onIntentChanged() {
        // nothing by default
    }
    
    private boolean hasIntentString(String key) {
        final String field = intent.getExtras() != null ? intent.getExtras().getString(key) : null;
        return field != null && field.length() > 0;
    }
    
    protected boolean hasTitle() {
        return hasIntentString(Intent.EXTRA_SUBJECT);
    }

    protected boolean hasUrl() {
        return hasIntentString(Intent.EXTRA_TEXT);
    }
}
