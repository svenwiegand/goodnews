package com.gettingmobile.goodnews.itemview;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import com.gettingmobile.goodnews.settings.Settings;

/**
 * Enhances the ViewPager to always switch the page when a drag starts on the horizontal border but no when else.
 */
public final class ItemViewContentPager extends ViewPager {
    private static final String LOG_TAG = "goodnews.ItemViewContentPager";
    private static final int BORDER_WIDTH_DP = 50;
    private final int borderWidth;
    private Settings settings;

    public ItemViewContentPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        borderWidth = (int) (BORDER_WIDTH_DP * context.getResources().getDisplayMetrics().density);
        Log.i(LOG_TAG, "borderWidth = " + borderWidth);
    }

    public void setSettings(Settings settings) {
        this.settings = settings;
    }

    @Override
    protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
        // we simply ignore the concrete child view.
        // 1) if the touch down happens in the border area we always return false here to make the pager scroll.
        // 2) if the touch down happens outside the border area we always return true to make the pager *not* scroll.
        return settings.articleTurnOverRequiresBorderSwipe() ? !isInBorder(x) : super.canScroll(v, checkV, dx, x, y);
    }

    protected boolean isInBorder(int x) {
        final int width = getWidth();
        final boolean inBorder = x <= borderWidth || x >= (width - borderWidth);
        Log.i(LOG_TAG, "isInBorder(" + x + ") for borderWidth=" + borderWidth + " and width=" + width + ": " + inBorder);
        return inBorder;
    }
}