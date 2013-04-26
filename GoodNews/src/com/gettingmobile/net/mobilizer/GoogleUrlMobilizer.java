package com.gettingmobile.net.mobilizer;

import android.net.Uri;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GoogleUrlMobilizer implements UrlMobilizer {
    private static final String BASE_URL = "http://www.google.com/gwt/x?u=";
    private static final Pattern URL_FINDER = Pattern.compile("http://www\\.google\\.com/gwt/x\\?u=(.*)");

    @Override
    public String mobilize(String url, boolean scaleImages) {
        return BASE_URL + Uri.encode(url);
    }

    @Override
    public String unmobilize(String url) {
        final Matcher m = URL_FINDER.matcher(url);
        return m.matches() ? Uri.decode(m.group(1)) : url;
    }
}
