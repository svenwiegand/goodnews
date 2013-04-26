package com.gettingmobile.goodnews.util;

import android.content.Context;
import com.gettingmobile.goodnews.R;
import com.gettingmobile.google.reader.ElementId;
import com.gettingmobile.google.reader.Item;
import com.gettingmobile.text.HtmlUtil;

public class ItemTextUtil {
    public static String getItemTitle(Context context, Item item) {
        return item.getTitle() != null && item.getTitle().length() > 0 ? item.getTitle() : context.getString(R.string.no_title);
    }
    
    public static String getUnformattedItemTitle(Context context, Item item) {
        return HtmlUtil.removeTags(getItemTitle(context, item));
    }

    /**
     * Returns the localized name for the specified item's feed if it references a special (google reader integrated)
     * feed or null otherwise.
     * @param context the context to load text resources from.
     * @param item the item to determine the feed text for.
     * @return the localized name for the specified item's feed if it references a special (google reader integrated)
     * feed or null otherwise.
     */
    public static String getItemSpecialFeedTitle(Context context, Item item) {
        final ElementId feedId = item.getFeedId();
        if (feedId == null)
            return null;

        switch (feedId.getType()) {
            case SOURCE:
                return context.getString(R.string.tag_post);
            default:
                return null;
        }
    }
}
