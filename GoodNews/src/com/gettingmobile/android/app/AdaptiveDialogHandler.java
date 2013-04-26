package com.gettingmobile.android.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.gettingmobile.goodnews.util.ThemeUtil;
import com.google.inject.Binder;
import com.google.inject.Injector;
import com.google.inject.Module;
import roboguice.RoboGuice;

public abstract class AdaptiveDialogHandler {
    protected static final String EXTRA_KEY_BASE = "com.gettingmobile.goodnews.";
    protected static final String EXTRA_KEY_HANDLER_CLASS = EXTRA_KEY_BASE + "HANDLER_CLASS";
    protected static final String EXTRA_KEY_HANDLER_EXTRAS = EXTRA_KEY_BASE + "HANDLER_EXTRAS";
    protected final int layourId;
    private AdaptiveDialogInterface dialog;

    protected static void start(Activity activity, Class<? extends AdaptiveDialogHandler> dialogHandlerClass, Bundle extras) {
        final Class<? extends Activity> activityClass = ThemeUtil.isTablet(activity) ?
                AdaptiveDialogTabletActivity.class : AdaptiveDialogPhoneActivity.class;
        final Intent intent = new Intent(activity, activityClass);
        intent.putExtra(EXTRA_KEY_HANDLER_CLASS, dialogHandlerClass);
        if (extras != null) {
            intent.putExtra(EXTRA_KEY_HANDLER_EXTRAS, extras);
        }
        activity.startActivity(intent);
    }
    
    protected static void start(Activity activity, Class<? extends AdaptiveDialogHandler> dialogHandlerClass) {
        start(activity, dialogHandlerClass, null);
    }
    
    protected static AdaptiveDialogHandler createDialogHandlerFromIntent(Context context, Intent intent) {
        final Bundle handlerExtras = intent.getBundleExtra(EXTRA_KEY_HANDLER_EXTRAS);
        final Injector injector = handlerExtras == null ? RoboGuice.getInjector(context) :
                RoboGuice.getInjector(context).createChildInjector(new Module() {
                    @Override
                    public void configure(Binder binder) {
                        binder.bind(Bundle.class).toInstance(handlerExtras);
                    }
                });

        //noinspection unchecked
        return injector.getInstance((Class<? extends AdaptiveDialogHandler>)
                intent.getExtras().getSerializable(EXTRA_KEY_HANDLER_CLASS));
    }

    protected AdaptiveDialogHandler(int layoutId) {
        this.layourId = layoutId;
    }
    
    public int getLayoutId() {
        return layourId;
    }

    public AdaptiveDialogInterface getDialog() {
        return dialog;
    }

    public void setDialog(AdaptiveDialogInterface dialog) {
        this.dialog = dialog;
    }

    @SuppressWarnings("UnusedParameters")
    protected void onCreate(View view) {
        // do nothing by default
    }

    protected void onStart() {
        // do nothing by default
    }

    @SuppressWarnings("UnusedParameters")
    protected void onSaveInstanceState(Bundle outState) {
        // do nothing by default
    }

    @SuppressWarnings("UnusedDeclaration")
    protected void onRestoreInstanceState (Bundle savedInstanceState) {
        // do nothing by default
    }

    protected void onRightButtonClicked() {
        dialog.dismiss();
    }

    protected void onLeftButtonClicked() {
        dialog.dismiss();
    }

    protected void onMiddleButtonClicked() {
        dialog.dismiss();
    }
}
