package com.gettingmobile.goodnews.itemview;

import android.webkit.WebSettings;
import android.webkit.WebView;

class ItemWebViewInitializer {
    public void initWebView(WebView webView) {
        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        webView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setJavaScriptEnabled(true);
    }
}
