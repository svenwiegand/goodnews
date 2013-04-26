package com.gettingmobile.text;

public final class HtmlUtil {
    public static final String ELLIPSIS = "&hellip;";
    private static final String TAG_PATTERN = "<[^>]+>";
    private static final String WHITESPACE_PATTERN = "\\s+";
    
    public static String removeTags(String text) {
        return text.replaceAll(TAG_PATTERN, "").replaceAll(WHITESPACE_PATTERN, " ").trim();
    }

    public static String toText(String html) {
        final int bodyStart = html.indexOf("<body");
        if (bodyStart > -1) {
            html = html.substring(bodyStart);
        }
        return removeTags(html);
    }
}
