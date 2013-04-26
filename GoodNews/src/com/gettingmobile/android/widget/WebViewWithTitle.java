package com.gettingmobile.android.widget;

/*
 * Copyright (C) 2012 Thomas Werner
 * Portions Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Picture;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.FrameLayout;
import com.gettingmobile.android.util.ApiLevel;

import java.lang.reflect.Method;

/**
 * WebView derivative with custom setEmbeddedTitleBar implementation for Android
 * versions that do not support that feature anymore.
 * <p>
 * <b>Usage</b>
 * <p>
 * Call {@link #setTitle(View)} for setting a view as embedded
 * title bar on top of the displayed WebView page.
 */
public class WebViewWithTitle extends WebView {
    private static final String LOG_TAG = "goodnews.WebViewWithTitle";
    private LifecycleDispatcher lifecycleDispatcher = null;
    View mTitleBar;
    int mTitleBarOffs;
    boolean mTouchInTitleBar;
    boolean mTouchMove;
    private Rect mClipBounds = new Rect();
    private Matrix mMatrix = new Matrix();
    private Method mNativeGetVisibleTitleHeightMethod;
    private OnScrollChangedListener scrollListener = null;
    private int initialScrollX = 0;
    private int initialScrollY = 0;

    public WebViewWithTitle(Context context) {
        super(context);
        init();
    }

    public WebViewWithTitle(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WebViewWithTitle(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    /**
     * Gets the correct WebViewWithTitle instance for the current API level.
     */
    public static WebViewWithTitle createInstance(Context context) {
        if(Build.VERSION.SDK_INT >= 16) {
            return new JellyBeanWebViewWithTitle(context);
        } else {
            return new WebViewWithTitle(context);
        }
    }

    //
    // provide scroll listener functionality
    //

    public void setOnScrollChangedListener(OnScrollChangedListener listener) {
        scrollListener = listener;
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        fireScrollEvent();
    }

    private void fireScrollEvent() {
        if (scrollListener != null) {
            scrollListener.onScrollChanged(getScrollX(), getScrollY());
        }
    }

    //
    // handle initial scrolling
    //

    private final PictureListener initialScrollHandler = new PictureListener() {
        @Override
        public void onNewPicture(WebView webView, Picture picture) {
            if ((initialScrollY > 0 || initialScrollX > 0) && initialScrollY < getContentHeight()) {
                scrollTo(initialScrollX, initialScrollY);
                initialScrollX = initialScrollY = 0;
            }
        }
    };

    public void setInitialScroll(int x, int y) {
        initialScrollX = x;
        initialScrollY = y;
        //noinspection deprecation
        setPictureListener(initialScrollHandler);
    }

    /**
     * <i>Corrects the visual displacement caused by the title bar view.</i>
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
            } else {
                if(Build.VERSION.SDK_INT < 16) {
                    if(!mTouchMove) {
                        mTitleBarOffs = getVisibleTitleHeightCompat();
                    }

                    y -= mTitleBarOffs;
                    if(y < 0) y = 0;
                    event.setLocation(x, y);
                }

                return super.dispatchTouchEvent(event);
            }
        } else {
            return super.dispatchTouchEvent(event);
        }
    }

    /**
     * Sets a {@link View} as an embedded title bar to appear on top of the
     * WebView page.
     * <p>
     * This method tries to call the hidden API method setEmbeddedTitleBar if
     * present. On failure the custom implementation provided by this class will
     * be used instead.
     *
     * @param v The view to set or null for removing the title bar view.
     */
    public void setTitle(View v) {
        try {
            Method nativeMethod = getClass().getMethod("setEmbeddedTitleBar",
                    View.class);
            nativeMethod.invoke(this, v);
        } catch(Exception e) {
            Log.d(LOG_TAG,
                    "Native setEmbeddedTitleBar not available. Starting workaround");
            setEmbeddedTitleBarJellyBean(v);
        }
    }

    public void setTitle(int resId) {
        setTitle(inflate(getContext(), resId, null));
    }

    @Override
    protected int computeVerticalScrollExtent() {
        if(mTitleBar == null) return super.computeVerticalScrollExtent();
        return getViewHeightWithTitle() - getVisibleTitleHeightCompat();
    }

    @Override
    protected int computeVerticalScrollOffset() {
        if(mTitleBar == null) return super.computeVerticalScrollOffset();
        return Math.max(getScrollY() - getTitleHeight(), 0);
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        canvas.save();

        if(child == mTitleBar) {
            mTitleBar.offsetLeftAndRight(getScrollX() - mTitleBar.getLeft());

            if(Build.VERSION.SDK_INT < 16) {
                //noinspection deprecation
                mMatrix.set(canvas.getMatrix());
                mMatrix.postTranslate(0, -getScrollY());
                canvas.setMatrix(mMatrix);
            }
        }

        boolean result = super.drawChild(canvas, child, drawingTime);
        canvas.restore();

        return result;
    }

    /**
     * Gets the currently visible height of the title bar view if set.
     *
     * @return Visible height of title bar view or 0 if not set.
     */
    protected int getVisibleTitleHeightCompat() {
        if(mTitleBar == null && mNativeGetVisibleTitleHeightMethod != null) {
            try {
                return (Integer) mNativeGetVisibleTitleHeightMethod
                        .invoke(this);
            } catch(Exception e) {
                // ignore
            }
        }

        return Math.max(getTitleHeight() - Math.max(0, getScrollY()), 0);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(Build.VERSION.SDK_INT >= 16) {
            super.onDraw(canvas);
            return;
        }

        canvas.save();

        if(mTitleBar != null) {
            final int sy = getScrollY();
            final int sx = getScrollX();
            mClipBounds.top = sy;
            mClipBounds.left = sx;
            mClipBounds.right = mClipBounds.left + getWidth();
            mClipBounds.bottom = mClipBounds.top + getHeight();
            canvas.clipRect(mClipBounds);
            //noinspection deprecation
            mMatrix.set(canvas.getMatrix());
            int titleBarOffs = getVisibleTitleHeightCompat();
            if(titleBarOffs < 0) titleBarOffs = 0;
            mMatrix.postTranslate(0, titleBarOffs);
            canvas.setMatrix(mMatrix);
        }

        super.onDraw(canvas);
        canvas.restore();
    }

    /**
     * Overrides a hidden method by replicating the behavior of the original
     * WebView class from Android 2.3.4.
     * <p>
     * The worst that could happen is that this method never gets called, which
     * isn't too bad because this does not harm the functionality of this class.
     */
    @SuppressWarnings("UnusedDeclaration")
    protected void onDrawVerticalScrollBar(Canvas canvas, Drawable scrollBar,
            int l, int t, int r, int b) {
        int sy = getScrollY();

        if(sy < 0) {
            t -= sy;
        }
        scrollBar.setBounds(l, t + getVisibleTitleHeightCompat(), r, b);
        scrollBar.draw(canvas);
    }

    private int getTitleHeight() {
        if(mTitleBar != null) return mTitleBar.getHeight();
        return 0;
    }

    private int getViewHeightWithTitle() {
        int height = getHeight();
        if(isHorizontalScrollBarEnabled() && !overlayHorizontalScrollbar()) {
            height -= getHorizontalScrollbarHeight();
        }
        return height;
    }

    private void init() {
        lifecycleDispatcher = ApiLevel.isAtLeast(11) ? new HoneycombLifecycleDispatcher() : new OldLifecycleDispatcher();
        try {
            mNativeGetVisibleTitleHeightMethod = WebView.class
                    .getDeclaredMethod("getVisibleTitleHeight");
        } catch(NoSuchMethodException e) {
            Log.w(LOG_TAG,
                    "Could not retrieve native hidden getVisibleTitleHeight method");
        }
    }

    /**
     * The hidden method setEmbeddedTitleBar has been removed from Jelly Bean.
     * This method replicates the functionality.
     */
    private void setEmbeddedTitleBarJellyBean(View v) {
        if(mTitleBar == v) return;

        if(mTitleBar != null) {
            removeView(mTitleBar);
        }

        if(null != v) {
            LayoutParams vParams = new LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 0, 0);

            if(Build.VERSION.SDK_INT >= 16) {
                TouchBlockView tbv = new TouchBlockView(getContext());
                FrameLayout.LayoutParams tbvParams = new FrameLayout.LayoutParams(
                        LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                tbv.addView(v, tbvParams);
                addView(tbv, vParams);
                v = tbv;
            } else {
                addView(v, vParams);
            }
        }

        mTitleBar = v;
    }

    public void pause() {
        lifecycleDispatcher.pause();
    }

    public void resume() {
        lifecycleDispatcher.resume();
    }

    // inner classes
    private interface LifecycleDispatcher {
        void pause();
        void resume();
    }

    private final class OldLifecycleDispatcher implements LifecycleDispatcher {
        private final Method onPause;
        private final Method onResume;

        private OldLifecycleDispatcher() {
            onPause = getLifecycleMethod("onPause");
            onResume = getLifecycleMethod("onResume");
        }

        private Method getLifecycleMethod(String methodName) {
            try {
                return WebViewWithTitle.class.getMethod(methodName, (Class[]) null);
            } catch (Exception ex) {
                Log.d(LOG_TAG, "lifecycle method not available: " + methodName);
                return null;
            }
        }

        private void callLifecycleMethodIfNotNull(Method method) {
            if (method != null) {
                try {
                    method.invoke(WebViewWithTitle.this);
                } catch (Exception e) {
                    Log.w(LOG_TAG, "failed to call lifecycle method: " + method.getName());
                }
            }
        }

        @Override
        public void pause() {
            callLifecycleMethodIfNotNull(onPause);
        }

        @Override
        public void resume() {
            callLifecycleMethodIfNotNull(onResume);
        }
    }

    private final class HoneycombLifecycleDispatcher implements LifecycleDispatcher {
        @Override
        public void pause() {
            onPause();
        }

        @Override
        public void resume() {
            onResume();
        }
    }
    /**
     * Internally used view wrapper for suppressing unwanted touch events on the
     * title bar view when WebView contents is being touched.
     */
    private class TouchBlockView extends FrameLayout {
        public TouchBlockView(Context context) {
            super(context);
        }

        @Override
        public boolean dispatchTouchEvent(MotionEvent ev) {
            if(!mTouchInTitleBar) {
                return false;
            } else {
                switch(ev.getActionMasked()) {
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        mTouchInTitleBar = false;
                        break;

                    default:

                }

                return super.dispatchTouchEvent(ev);
            }
        }
    }
}