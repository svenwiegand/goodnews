package com.gettingmobile.net.mobilizer;

public interface UrlMobilizer {
    String mobilize(String url, boolean scaleImages);
    String unmobilize(String url);
}
