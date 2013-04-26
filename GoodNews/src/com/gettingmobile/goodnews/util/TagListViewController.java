package com.gettingmobile.goodnews.util;

import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
import android.widget.TextView;
import com.gettingmobile.google.reader.ElementId;
import com.gettingmobile.google.reader.Tag;

import java.util.Collection;

public class TagListViewController {
    private static final char NBSP = '\u00A0';
    private static final char NBMI = '\u2011';

    public static void setTags(TextView view, Collection<ElementId> tags, int bgColor) {
        if (!tags.isEmpty()) {
            final SpannableStringBuilder spannable = new SpannableStringBuilder();
            
            for (ElementId tag : tags) {
                if (spannable.length() > 0) {
                    spannable.append(' ');
                }
    
                final int startIndex = spannable.length();
                spannable.append(NBSP + Tag.getTitleById(tag).replace(' ', NBSP).replace('-', NBMI) + NBSP);
                spannable.setSpan(new BackgroundColorSpan(bgColor), startIndex, spannable.length(), 0);
            }
            view.setText(spannable);
        } else {
            view.setText("");
        }
    }
}
