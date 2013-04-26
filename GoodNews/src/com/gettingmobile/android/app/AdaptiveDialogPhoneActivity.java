package com.gettingmobile.android.app;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import com.gettingmobile.goodnews.Application;
import com.gettingmobile.goodnews.R;
import roboguice.RoboGuice;
import roboguice.activity.RoboFragmentActivity;

public class AdaptiveDialogPhoneActivity extends RoboFragmentActivity implements AdaptiveDialogInterface {
    private AdaptiveDialogHandler dlgHandler = null;
    private AdaptiveDialogLayout layout = null;

    /*
     * lifecycle
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        dlgHandler = AdaptiveDialogHandler.createDialogHandlerFromIntent(this, getIntent());
        dlgHandler.setDialog(this);

        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.dialog);
        final ViewGroup view = (ViewGroup) findViewById(R.id.dialog);
        getLayoutInflater().inflate(dlgHandler.getLayoutId(), view, true);
        layout = new AdaptiveDialogLayout((ViewGroup) findViewById(R.id.buttonPanel), dlgHandler);

        RoboGuice.getInjector(this).injectMembers(dlgHandler);
        dlgHandler.onCreate(view);
        if (savedInstanceState != null) {
            dlgHandler.onRestoreInstanceState(
                    savedInstanceState.getBundle(AdaptiveDialogHandler.EXTRA_KEY_HANDLER_EXTRAS));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        dlgHandler.onStart();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        final Bundle handlerBundle = new Bundle();
        dlgHandler.onSaveInstanceState(handlerBundle);
        outState.putBundle(AdaptiveDialogHandler.EXTRA_KEY_HANDLER_EXTRAS, handlerBundle);
        super.onSaveInstanceState(outState);
    }

    /*
     * AdaptiveDialogInterface
     */

    @Override
    public Application getApp() {
        return (Application) getApplication();
    }

    @Override
    public void dismiss() {
        finish();
    }

    @Override
    public Button getRightButton() {
        return layout.rightButton;
    }

    @Override
    public Button getLeftButton() {
        return layout.leftButton;
    }

    @Override
    public Button getMiddleButton() {
        return layout.middleButton;
    }
}
