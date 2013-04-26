package com.gettingmobile.net.mobilizer;

public class NullUrlMobilizer implements UrlMobilizer {
    @Override
    public String mobilize(String url, boolean scaleImages) {
        return url;
    }

    @Override
    public String unmobilize(String url) {
        return url;
    }
}
