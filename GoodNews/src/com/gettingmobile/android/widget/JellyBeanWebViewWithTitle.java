package com.gettingmobile.android.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebViewClassic.TitleBarDelegate;

final class JellyBeanWebViewWithTitle extends WebViewWithTitle implements TitleBarDelegate {
    private static final String LOG_TAG = "goodnews.JellyBeanWebViewWithTitle";

    public JellyBeanWebViewWithTitle(Context context) {
        super(context);
    }

    public JellyBeanWebViewWithTitle(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public JellyBeanWebViewWithTitle(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private void scrollToTop() {
        scrollTo(0, 0);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        Log.d(LOG_TAG, "onScrollChanged(" + l + ", " + t + ", " + oldl + ", " + oldt + ")");
        if (mTitleBar != null && oldt == 0 && t == mTitleBar.getHeight()) {
            Log.d(LOG_TAG, "automatic title bar scrolling? Calling scrollToTop");
            scrollToTop();
        }
        super.onScrollChanged(l, t, oldl, oldt);
    }

    /**
     * <i>Makes sure that the title bar view gets touch events</i>
     * <p>
     * {@inheritDoc}
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if(mTitleBar != null) {
            final int sy = getScrollY();
            final int visTitleHeight = getVisibleTitleHeightCompat();
            final float x = event.getX();
            float y = event.getY();

            switch(event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    if(y <= visTitleHeight) {
                        mTouchInTitleBar = true;
                    }
                    break;

                case MotionEvent.ACTION_MOVE:
                    mTouchMove = true;
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    mTouchMove = false;
                    break;

                default:
            }

            if(mTouchInTitleBar) {
                y += sy;
                event.setLocation(x, y);

                return mTitleBar.dispatchTouchEvent(event);
            }
        }

        return super.dispatchTouchEvent(event);
    }

    @Override
    public int getTitleHeight() {
        final int titleHeight = mTitleBar != null ? mTitleBar.getHeight() : 0;
        Log.i("goodnews.JellyBeanWebViewWithTitle", "titleHeight = " + titleHeight);
        return titleHeight;
    }

    @Override
    public void onSetEmbeddedTitleBar(View title) {
    }

}
