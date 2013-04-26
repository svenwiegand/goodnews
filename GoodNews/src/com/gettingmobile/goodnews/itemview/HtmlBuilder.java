package com.gettingmobile.goodnews.itemview;

import java.util.regex.Pattern;

class HtmlBuilder {
    private static final Pattern HTML_START_PATTERN = Pattern.compile("<\\s*html\\s*>", Pattern.CASE_INSENSITIVE);

    public static String build(String title, String content, String color, String bgColor, boolean scaleImages) {
        if (HTML_START_PATTERN.matcher(content).find()) {
            /*
             * this is already HTML wrapped, so return as is
             */
            return content;
        }

        final StringBuilder out = new StringBuilder("<html><head>");
        if (title != null) {
            out.append("<title>" + title + "</title>");
        }
        out.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"file:///android_asset/itemview/itemview.css\" />");
        if (scaleImages) {
            out.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"file:///android_asset/itemview/scale-images.css\" />");
        }
        out.append("</head><body style=\"color:" + color + ";background-color:" + bgColor  + ";\"><div id=\"body\">").
                append(content).
                append("</div></body></html>");
        return out.toString();
    }

}
