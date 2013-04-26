package com.gettingmobile.net.mobilizer;


import android.net.Uri;
import com.gettingmobile.Security.Hash;
import com.gettingmobile.io.Base64;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class ReadabilityUrlMobilizer implements UrlMobilizer {
    private static final String API_TOKEN = "357947acad048e6d49321bfc21680bdc982225e3";
    private static final String BASE_URL = "http://getting-mobile.appspot.com/api/1/Readability/mobilize/";
    private static final Pattern URL_FINDER = Pattern.compile("http://getting-mobile\\.appspot\\.com/api/1/Readability/mobilize/(.*?)\\?.*");

    @Override
    public String mobilize(String url, boolean scaleImages) {
        final StringBuilder u = new StringBuilder(BASE_URL);
        u.append(Uri.encode(url)).append('?');
        if (scaleImages)
            u.append("scaleImages=true&");
        u.append("signature=").append(Uri.encode(Base64.encode(Hash.getInstance().create(url, API_TOKEN))));
        return u.toString();
    }

    @Override
    public String unmobilize(String url) {
        final Matcher m = URL_FINDER.matcher(url);
        return m.matches() ? Uri.decode(m.group(1)) : url;
    }
}
