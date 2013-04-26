package com.gettingmobile.goodnews.tip;

import android.view.Gravity;

abstract class AbstractVisualTip extends AbstractTip implements VisualTip {
    protected final int textId;
    protected final int textViewGravity;

    protected AbstractVisualTip(String id, int textId, int textViewGravity) {
        super(id, FLAG_VISUAL | FLAG_AUTOMATIC);
        this.textId = textId;
        this.textViewGravity = textViewGravity;
    }

    protected AbstractVisualTip(String id, int textId) {
        this(id, textId, Gravity.CENTER);
    }

    @Override
    public int getTextResourceId() {
        return textId;
    }

    @Override
    public int getTextViewGravity() {
        return textViewGravity;
    }
}
