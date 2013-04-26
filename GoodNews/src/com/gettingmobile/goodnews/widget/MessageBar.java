package com.gettingmobile.goodnews.widget;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.gettingmobile.android.app.actions.Action;
import com.gettingmobile.goodnews.Activity;
import com.gettingmobile.goodnews.Application;
import com.gettingmobile.goodnews.R;

public class MessageBar implements View.OnClickListener, View.OnLongClickListener {
    public static final int HIDE = 0;
    public static final int INFO = 1;
    public static final int WARNING = 2;
    
    private final Activity activity;
    private final View msgBar;
    private final ImageView msgIcon;
    private final TextView msgText;
    private Action<Application> clickAction = null;
    private Action<Application> longClickAction = null;
    

    public MessageBar(Activity activity) {
        msgBar = activity.findViewById(R.id.msg_bar);
        if (msgBar != null) {
            msgIcon = (ImageView) msgBar.findViewById(R.id.msg_icon);
            msgText = (TextView) msgBar.findViewById(R.id.msg);
            final View closeButton = msgBar.findViewById(R.id.close);
            closeButton.setOnClickListener(this);
            msgBar.setOnClickListener(this);
            msgBar.setLongClickable(true);
            msgBar.setOnLongClickListener(this);
        } else {
            msgIcon = null;
            msgText = null;
        }
        this.activity = activity;
    }
    
    public void setClickAction(Action<Application> action) {
        this.clickAction = action;
    }
    
    public void setLongClickAction(Action<Application> action) {
        this.longClickAction = action;
    }

    protected void show(int type) {
        if (msgBar != null) {
            if (type > 0) {
                final int icon;
                final int color;
                if (type == WARNING) {
                    icon = R.drawable.ic_msg_warning;
                    color = R.color.background_warning;
                } else {
                    icon = R.drawable.ic_msg_info;
                    color = R.color.background_info;
                }
                if (msgIcon != null) {
                    msgIcon.setImageResource(icon);
                }
                msgBar.setBackgroundColor(activity.getResources().getColor(color));
            }
            msgBar.setVisibility(type > 0 ? View.VISIBLE : View.GONE);
        }
    }

    public void showMessage(int type, CharSequence msg) {
        if (msgText != null) {
            msgText.setText(msg);
            show(type);
        }
    }

    public void showWarning(CharSequence msg) {
        showMessage(WARNING, msg);
    }

    public void showMessage(int type, int msgId) {
        showMessage(type, activity.getText(msgId));
    }

    public void showInfo(int msgId) {
        showMessage(INFO, msgId);
    }

    public void dismiss() {
        show(HIDE);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.close) {
            show(HIDE);
        } else if (view.getId() == R.id.msg_bar && clickAction != null) {
            clickAction.onFired(activity);
        }
    }

    @Override
    public boolean onLongClick(View view) {
        return view.getId() == R.id.msg_bar && longClickAction != null && longClickAction.onFired(activity);
    }
}
