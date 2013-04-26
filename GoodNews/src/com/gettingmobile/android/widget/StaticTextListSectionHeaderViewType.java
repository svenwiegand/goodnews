package com.gettingmobile.android.widget;

import android.view.View;
import android.widget.TextView;
import com.gettingmobile.goodnews.R;

public class StaticTextListSectionHeaderViewType extends SimpleListItemViewType {
    private final int textId;

    public StaticTextListSectionHeaderViewType(int id, boolean enabled, int textId) {
        super(id, enabled, R.layout.list_section_header);
        this.textId = textId;
    }

    @Override
    public void bindView(View view, Object item) {
        ((TextView) view).setText(textId);
    }
}
