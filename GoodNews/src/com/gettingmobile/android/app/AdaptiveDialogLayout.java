package com.gettingmobile.android.app;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.gettingmobile.goodnews.R;

final class AdaptiveDialogLayout implements View.OnClickListener {
    protected final Button rightButton;
    protected final Button leftButton;
    protected final Button middleButton;
    private final AdaptiveDialogHandler dlgHandler;
    
    AdaptiveDialogLayout(ViewGroup view, AdaptiveDialogHandler dlgHandler) {
        rightButton = initButton(view, R.id.rightButton);
        leftButton = initButton(view, R.id.leftButton);
        middleButton = initButton(view, R.id.middleButton);
        this.dlgHandler = dlgHandler;
    }
    
    private Button initButton(ViewGroup view, int buttonId) {
        final Button button = (Button) view.findViewById(buttonId);
        button.setOnClickListener(this);
        return button;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rightButton:
                dlgHandler.onRightButtonClicked();
                break;
            case R.id.leftButton:
                dlgHandler.onLeftButtonClicked();
                break;
            case R.id.middleButton:
                dlgHandler.onMiddleButtonClicked();
                break;
        }
    }
}
