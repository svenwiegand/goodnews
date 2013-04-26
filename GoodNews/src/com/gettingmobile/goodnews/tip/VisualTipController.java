package com.gettingmobile.goodnews.tip;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.gettingmobile.goodnews.Application;
import com.gettingmobile.goodnews.R;

import java.util.ArrayList;
import java.util.List;

public final class VisualTipController {
    private final Activity activity;
    private final TipManager tipManager;
    private final List<Tip> tips;
    private final OnDismissListener dismissListener;
    private int currentTip;
    private VisualTipDrawable drawable = null;
    private View tipView = null;
    private View textViewGroup = null;
    private TextView textView = null;

    public VisualTipController(Application app, Activity activity, TipManager tipManager, int tipGroupId,
                               boolean unshownOnly, OnDismissListener dismissListener) {
        this.activity = activity;
        this.tipManager = tipManager;
        this.tips = (unshownOnly && !app.getSettings().autoShowUsageTips()) ?
                new ArrayList<Tip>(0) : tipManager.getGroup(tipGroupId, unshownOnly, Tip.FLAG_VISUAL);
        currentTip = 0;

        this.dismissListener = dismissListener;
    }

    public boolean hasTipsToShow() {
        return !tips.isEmpty();
    }

    public void show() {
        if (hasTipsToShow()) {
            doShow();
        } else {
            dismiss();
        }
    }

    public void showDelayed() {
        if (!hasTipsToShow()) {
            dismiss();
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    doShow();
                }
            }, 500);
        }
    }

    private void doShow() {
        if (tips.isEmpty() || activity.isFinishing()) {
            dismiss();
            return;
        }

        if (activity.getWindow().findViewById(R.id.tip_root) != null) {
            /*
             * there is already an inflated tip view in this activity
             */
            dismiss();
            return;
        }

        final ViewGroup rootFrame = (ViewGroup) activity.findViewById(R.id.root);
        tipView = activity.getLayoutInflater().inflate(R.layout.visual_tip, null);
        WindowManager.LayoutParams layout = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.FILL_PARENT, WindowManager.LayoutParams.FILL_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION,
                0, PixelFormat.TRANSLUCENT);
        try {
            activity.getWindowManager().addView(tipView, layout);
        } catch (WindowManager.BadTokenException ex) {
            dismiss();
            return;
        }

        drawable = new VisualTipDrawable(tipView, activity.getResources());

        rootFrame.setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener() {
            @Override
            public void onChildViewAdded(View view, View view1) {
                drawable.updateViewMark();
            }

            @Override
            public void onChildViewRemoved(View view, View view1) {
                drawable.updateViewMark();
            }
        });

        tipView.setBackgroundDrawable(drawable);
        tipView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNextTip();
            }
        });
        tipView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                    dismiss();
                    return true;
                } else {
                    return false;
                }
            }
        });

        textViewGroup = tipView.findViewById(R.id.tip_text_group);
        textView = (TextView) textViewGroup.findViewById(R.id.tip_text);
        showNextTip();
    }

    public void showNextTip() {
        if (currentTip < tips.size()) {
            final VisualTip tip = (VisualTip) tips.get(currentTip++);

            final View markView = tip.findView(activity);
            if (markView != null) {
                tipManager.setTipShown(tip);
                drawable.setViewMark(tip.findView(activity));

                final FrameLayout.LayoutParams p = (FrameLayout.LayoutParams) textViewGroup.getLayoutParams();
                p.gravity = tip.getTextViewGravity();
                textViewGroup.setLayoutParams(p);
                textView.setText(tip.getTextResourceId());
            } else {
                showNextTip();
            }
        } else {
            dismiss();
        }
    }

    public void dismiss() {
        if (tipView != null && !activity.isFinishing()) {
            try {
                activity.getWindowManager().removeView(tipView);
            } catch(RuntimeException ex) {
                // view doesn't exist anymore ... simply ignore
            }
        }
        if (dismissListener != null) {
            dismissListener.onVisualTipsDismissed();
        }
    }

    /*
     * inner classes
     */

    public interface OnDismissListener {
        void onVisualTipsDismissed();
    }
}
