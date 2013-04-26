package com.gettingmobile.goodnews.tip;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ScrollView;
import com.gettingmobile.android.app.AdaptiveDialogHandler;
import com.gettingmobile.goodnews.Application;
import com.gettingmobile.goodnews.R;
import com.google.inject.Inject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TipDialogHandler extends AdaptiveDialogHandler implements CompoundButton.OnCheckedChangeListener {
    public static final String EXTRA_TIP_GROUP = "com.gettingmobile.goodnews.intent.extra.TIP_GROUP";
    public static final String EXTRA_TIP_UNSHOWN_ONLY = "com.gettingmobile.goodnews.intent.extra.TIP_UNSHOWN";
    public static final String EXTRA_TIP_ID = "com.gettingmobile.goodnews.intent.extra.TIP_ID";
    private final String tipId;
    private final int tipGroupId;
    private final boolean unshownOnly;
    private List<Tip> tips = null;
    private int currentTipIndex = -1;
    private ScrollView container = null;
    private WebView contentView = null;
    private CheckBox additionalCheckBox = null;
    private View autoShowCheckBoxContainer = null;
    private Animation prevAnimation = null;
    private Animation nextAnimation = null;
    
    public static boolean start(Application app, Activity activity, TipManager tipManager, int tipGroupId, boolean unshownOnly) {
        if (unshownOnly && !app.getSettings().autoShowUsageTips())
            return false;
        
        if (unshownOnly && tipManager != null) {
            final List<Tip> tips = tipManager.getGroup(tipGroupId, unshownOnly, Tip.FLAG_FULL_SCREEN);
            for (Iterator<Tip> it = tips.iterator(); it.hasNext(); ) {
                final FullScreenTip tip = (FullScreenTip) it.next();
                if (!tip.canShowNow(activity)) {
                    it.remove();
                }
            }
            if (tips.isEmpty()) {
                return false;
            }
        }

        final Bundle extras = new Bundle();
        extras.putInt(EXTRA_TIP_GROUP, tipGroupId);
        extras.putBoolean(EXTRA_TIP_UNSHOWN_ONLY, unshownOnly);
        start(activity, TipDialogHandler.class, extras);
        return true;
    }
    
    public static void start(Activity activity, TipManager tipManager, String tipId) {
        final Tip tip = tipManager.getTip(tipId);
        if (tip != null && tip instanceof FullScreenTip && ((FullScreenTip) tip).canShowNow(activity)) {
            final Bundle extras = new Bundle();
            extras.putString(EXTRA_TIP_ID, tipId);
            start(activity, TipDialogHandler.class, extras);
        }        
    }
    
    @Inject
    public TipDialogHandler(Bundle extras) {
        super(R.layout.tip);
        tipId = extras.getString(EXTRA_TIP_ID);
        tipGroupId = extras.getInt(EXTRA_TIP_GROUP);
        unshownOnly = extras.getBoolean(EXTRA_TIP_UNSHOWN_ONLY);
    }
    
    private void buildTipList() {
        if (tipId != null) {
            tips = new ArrayList<Tip>(1);
            tips.add(getDialog().getApp().getTipManager().getTip(tipId));
        } else {
            if (tipGroupId == TipManager.TIP_UNGROUPED) {
                tips = getDialog().getApp().getTipManager().getTips(Tip.FLAG_FULL_SCREEN);
            } else {
                tips = getDialog().getApp().getTipManager().getGroup(tipGroupId, unshownOnly, Tip.FLAG_FULL_SCREEN);
            }
        }

    }

    @Override
    protected void onCreate(View view) {
        buildTipList();

        getDialog().getRightButton().setText(R.string.next);
        getDialog().getRightButton().setVisibility(View.VISIBLE);
        getDialog().getLeftButton().setText(R.string.prev);
        getDialog().getLeftButton().setVisibility(View.VISIBLE);
        getDialog().getMiddleButton().setText(R.string.close);
        getDialog().getMiddleButton().setVisibility(View.VISIBLE);

        prevAnimation = AnimationUtils.loadAnimation(getDialog().getApp(), R.anim.slide_right);
        nextAnimation = AnimationUtils.loadAnimation(getDialog().getApp(), R.anim.slide_left);

        container = (ScrollView) view.findViewById(R.id.container);
        contentView = (WebView) container.findViewById(R.id.content);
        contentView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onReceivedTitle(WebView view, String title) {
                getDialog().setTitle(title);
            }
        });
        contentView.setBackgroundColor(0x00000000);
        
        initCheckBox(container, R.id.auto_show_tips).setChecked(getDialog().getApp().getSettings().autoShowUsageTips());
        additionalCheckBox = initCheckBox(container, R.id.additional_tip_check_box);
        autoShowCheckBoxContainer = view.findViewById(R.id.auto_show_tips_group);
    }

    @Override
    protected void onStart() {
        boolean showNextPrev = tips.size() > 1;
        getDialog().getRightButton().setVisibility(showNextPrev ? View.VISIBLE : View.GONE);
        getDialog().getLeftButton().setVisibility(showNextPrev ? View.VISIBLE : View.GONE);
        
        showFirstTip();
    }

    /*
    * button handling
    */
    
    private CheckBox initCheckBox(View container, int checkBoxId) {
        final CheckBox checkBox = (CheckBox) container.findViewById(checkBoxId);
        checkBox.setOnCheckedChangeListener(this);
        final float scale = getDialog().getApp().getResources().getDisplayMetrics().density;
        checkBox.setPadding(checkBox.getPaddingLeft() + (int)(6 * scale), checkBox.getPaddingTop(), 
                checkBox.getPaddingRight(), checkBox.getPaddingBottom());
        return checkBox;
    }

    @Override
    protected void onRightButtonClicked() {
        showNextTip();
    }

    @Override
    protected void onLeftButtonClicked() {
        showPrevTip();
    }

    /*
     * show tip
     */

    protected void showFirstTip() {
        if (!tips.isEmpty()) {
            showTip(0);
        } else {
            getDialog().dismiss();
        }
    }

    protected void showNextTip() {
        if (currentTipIndex >= 0 && currentTipIndex < (tips.size() - 1)) {
            container.startAnimation(nextAnimation);
            showTip(currentTipIndex + 1);
        }
    }

    protected void showPrevTip() {
        if (currentTipIndex > 0) {
            container.startAnimation(prevAnimation);
            showTip(currentTipIndex - 1);
        }
    }

    protected void showTip(int tipIndex) {
        if (tips == null || tipIndex < 0 || tipIndex >= tips.size())
            return;

        final FullScreenTip tip = (FullScreenTip) tips.get(tipIndex);
        currentTipIndex = tipIndex;

        /*
         * set tip content
         */
        contentView.scrollTo(0, 0);
        container.scrollTo(0, 0);
        contentView.loadUrl("file:///android_asset/tips/" + getDialog().getApp().getString(R.string.language_code) +
                "/" + tip.getId() + ".xhtml");

        /*
         * update button visibility
         */
        getDialog().getLeftButton().setEnabled(tipIndex > 0);
        getDialog().getRightButton().setEnabled(tipIndex < (tips.size() - 1));
        getDialog().getMiddleButton().setEnabled(!unshownOnly || tipIndex == (tips.size() - 1));
        
        /*
         * handle tip checkbox
         */
        final int additionalCheckBoxTitleId = tip.getAdditionalCheckBoxTitleId();
        if (additionalCheckBoxTitleId != 0) {
            additionalCheckBox.setText(additionalCheckBoxTitleId);
            additionalCheckBox.setChecked(tip.getAdditionalCheckBoxState());
        }
        additionalCheckBox.setVisibility(additionalCheckBoxTitleId != 0 ? View.VISIBLE : View.GONE);
        
        /*
         * handle show auto check box
         */
        autoShowCheckBoxContainer.setVisibility(unshownOnly || tip.forceAutomaticTipCheckBox() ? View.VISIBLE : View.GONE);

        /*
         * mark tip as shown
         */
        getDialog().getApp().getTipManager().setTipShown(tip);
    }

    /*
     * action handling
     */

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
        switch (compoundButton.getId()) {
            case R.id.auto_show_tips:
                getDialog().getApp().getSettings().setAutoShowUsageTips(checked);
                break;
            case R.id.additional_tip_check_box:
                if (tips != null && currentTipIndex >= 0 && currentTipIndex < tips.size()) {
                    ((FullScreenTip) tips.get(currentTipIndex)).onAdditionalCheckBoxStateChanged(
                            getDialog().getApp(), checked);
                }
                break;
            default:
                // will not happen
        }
    }
}
