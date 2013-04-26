package com.gettingmobile.android.app;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import com.gettingmobile.goodnews.Application;
import com.gettingmobile.goodnews.R;
import roboguice.RoboGuice;

public final class AdaptiveDialogTabletFragment extends DialogFragment implements AdaptiveDialogInterface {
    private AdaptiveDialogHandler dlgHandler;
    private Dialog dlg = null;
    private AdaptiveDialogLayout layout = null;

    public AdaptiveDialogTabletFragment() {
    }

    /*
     * AdaptiveDialogInterface
     */
    
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

    @Override
    public Application getApp() {
        return (Application) getActivity().getApplication();
    }

    @Override
    public void setTitle(CharSequence title) {
        dlg.setTitle(title);
    }

    @Override
    public void setTitle(int titleId) {
        dlg.setTitle(titleId);
    }
    
    /*
     * lifecycle management
     */

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        dlgHandler = AdaptiveDialogHandler.createDialogHandlerFromIntent(getActivity(), getActivity().getIntent());
        dlgHandler.setDialog(this);

        dlg = new Dialog(getActivity());
        final ViewGroup dlgLayout = (ViewGroup) dlg.getLayoutInflater().inflate(R.layout.dialog, null);
        layout = new AdaptiveDialogLayout(dlgLayout, dlgHandler);

        final ViewGroup view = (ViewGroup) dlgLayout.findViewById(R.id.dialog);
        getActivity().getLayoutInflater().inflate(dlgHandler.getLayoutId(), view);

        dlg.setContentView(dlgLayout);
        dlg.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.FILL_PARENT);
        RoboGuice.getInjector(getActivity()).injectMembersWithoutViews(dlgHandler);
        dlgHandler.onCreate(view);
        if (savedInstanceState != null) {
            dlgHandler.onRestoreInstanceState(
                    savedInstanceState.getBundle(AdaptiveDialogHandler.EXTRA_KEY_HANDLER_EXTRAS));
        }
        return dlg;
    }

    @Override
    public void onStart() {
        super.onStart();
        dlgHandler.onStart();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        final Bundle handlerBundle = new Bundle();
        dlgHandler.onSaveInstanceState(handlerBundle);
        outState.putBundle(AdaptiveDialogHandler.EXTRA_KEY_HANDLER_EXTRAS, handlerBundle);
        super.onSaveInstanceState(outState);
    }

    /*
     * action handling
     */

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (getActivity() != null) {
            getActivity().finish();
        }
    }
}
