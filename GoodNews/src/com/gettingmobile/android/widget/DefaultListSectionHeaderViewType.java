package com.gettingmobile.android.widget;

import android.view.View;
import android.widget.TextView;
import com.gettingmobile.goodnews.R;

public class DefaultListSectionHeaderViewType extends SimpleListItemViewType {
    public DefaultListSectionHeaderViewType(int id, boolean enabled) {
        super(id, enabled, R.layout.list_section_header);
    }

    @Override
    public void bindView(View view, Object item) {
        final String text = getItemText(item);
        final TextView textView = (TextView) view;
        textView.setText(text != null ? text : "");
    }

    protected String getItemText(Object item) {
        return item.toString();
    }
}
