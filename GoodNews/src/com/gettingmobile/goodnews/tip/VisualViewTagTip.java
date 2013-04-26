package com.gettingmobile.goodnews.tip;

import android.app.Activity;
import android.view.View;
import com.gettingmobile.goodnews.R;

public final class VisualViewTagTip extends AbstractVisualTip {
    private final Object viewTag;
    private final int childViewId;

    public VisualViewTagTip(String id, int textId, int textViewGravity, Object viewTag, int childViewId) {
        super(id, textId, textViewGravity);
        this.viewTag = viewTag;
        this.childViewId = childViewId;
    }

    public VisualViewTagTip(String id, int textId, int textViewGravity, Object viewTag) {
        this(id, textId, textViewGravity, viewTag, 0);
    }

    @Override
    public View findView(Activity activity) {
        final View rootView = activity.findViewById(R.id.root);
        if (rootView == null)
            return null;

        final View view = rootView.findViewWithTag(viewTag);
        return view != null && childViewId != 0 ? view.findViewById(childViewId) : view;
    }
}
