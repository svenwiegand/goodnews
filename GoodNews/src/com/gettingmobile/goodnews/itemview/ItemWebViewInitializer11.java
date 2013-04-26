package com.gettingmobile.goodnews.itemview;

import android.webkit.WebView;

class ItemWebViewInitializer11 extends ItemWebViewInitializer {
    @Override
    public void initWebView(WebView webView) {
        super.initWebView(webView);
        webView.getSettings().setDisplayZoomControls(false);
    }
}
