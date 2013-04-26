package com.gettingmobile.util;

public class StringUtils {
    public static String explode(Iterable<?> items, String separator, String itemPrefix, String itemPostfix) {
        final StringBuilder s = new StringBuilder();
        for (Object item : items) {
            if (separator != null && s.length() > 0) {
                s.append(separator);
            }
            if (itemPrefix != null) {
                s.append(itemPrefix);
            }
            s.append(item);
            if (itemPostfix != null) {
                s.append(itemPostfix);
            }
        }
        return s.toString();
    }

    public static String explode(Iterable<?> items, String separator, String itemWrapper) {
        return explode(items, separator, itemWrapper, itemWrapper);
    }

    public static String explode(Iterable<?> items, String separator) {
        return explode(items, separator, null, null);
    }
}
