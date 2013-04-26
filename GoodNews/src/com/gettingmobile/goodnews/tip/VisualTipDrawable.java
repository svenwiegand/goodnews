package com.gettingmobile.goodnews.tip;

import android.content.res.Resources;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;

class VisualTipDrawable extends Drawable {
    private static int DIM_COLOR = Color.argb(0x7f, 0x00, 0x00, 0x00);
    private static int TIP_COLOR = Color.rgb(0xcc, 0x00, 0x00);
    private final float deflation;
    private final Paint markPaint;
    private final Paint dimPaint;
    private final View view;
    private View markedView = null;
    private RectF markRect = null;

    public VisualTipDrawable(View view, Resources resources) {
        this.view = view;

        final float strokeWidth = 3 * resources.getDisplayMetrics().density;
        deflation = strokeWidth / 2;

        markPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        markPaint.setColor(TIP_COLOR);
        markPaint.setStyle(Paint.Style.STROKE);
        markPaint.setStrokeWidth(strokeWidth);
        markPaint.setStrokeCap(Paint.Cap.ROUND);
        markPaint.setStrokeJoin(Paint.Join.ROUND);
        
        dimPaint = new Paint();
        dimPaint.setStyle(Paint.Style.FILL);
        dimPaint.setColor(DIM_COLOR);
    }

    public void setViewMark(View view) {
        if (view != null) {
            markedView = view;
            updateViewMark();
        } else {
            markRect = null;
        }
        invalidateSelf();
    }

    public void updateViewMark() {
        try {
            if (markedView != null && markedView.isShown() && view != null && view.isShown()) {
                final int[] viewLoc = new int[2];
                view.getLocationOnScreen(viewLoc);

                final int[] markLoc = new int[2];
                markedView.getLocationOnScreen(markLoc);

                final int x = markLoc[0] - viewLoc[0];
                final int y = markLoc[1] - viewLoc[1];
                markRect = new RectF(x + deflation, y + deflation,
                        x + markedView.getWidth() - deflation, y + markedView.getHeight() - deflation);
            }
        } catch (NullPointerException ex) {
            /*
             * on some platforms getLocationOnScreen generates a NullPointerException under some circumstances
             * and I cannot get that fixed
             */
            Log.e(VisualTipDrawable.class.getSimpleName(), "updateViewMark failed", ex);
        }
    }

    @Override
    public void draw(Canvas canvas) {
        /*
         * draw the background dim
         */
        canvas.drawRect(0, 0, getBounds().width(), getBounds().height(), dimPaint);

        if (markRect != null) {
            /*
             * erase the dim from the mark view's rectangle
             */
            canvas.save(Canvas.CLIP_SAVE_FLAG);
            canvas.clipRect(markRect);
            canvas.drawColor(0, PorterDuff.Mode.CLEAR);
            canvas.restore();

            /*
             * draw the mark
             */
            canvas.drawRect(markRect, markPaint);
        }
    }

    @Override
    public void setAlpha(int i) {
        // ignore
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        // ignore
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        updateViewMark();
    }
}
