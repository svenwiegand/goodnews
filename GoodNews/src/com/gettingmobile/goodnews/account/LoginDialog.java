package com.gettingmobile.goodnews.account;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.EditText;

import com.gettingmobile.goodnews.R;

public final class LoginDialog extends Dialog {	
	private final Listener listener;
	
	public LoginDialog(Context context, Listener listener, String userName, String password) {
		super(context);		
		this.listener = listener;
		
		setTitle(R.string.login_title);
		setContentView(R.layout.login);
		setCancelable(true);
		
		findViewById(R.id.login_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onClickLoginButton();
			}
		});
		((EditText) findViewById(R.id.login_username)).setText(userName != null ? userName : "");
		((EditText) findViewById(R.id.login_password)).setText(password != null ? password : "");
	}
	
	protected void onClickLoginButton() {
		String username = ((EditText) findViewById(R.id.login_username)).getText().toString();
		String password = ((EditText) findViewById(R.id.login_password)).getText().toString();
		if (username.length() == 0 || password.length() == 0) {
			new AlertDialog.Builder(getContext())
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(R.string.login_title)
				.setMessage(R.string.login_incomplete)
				.setNeutralButton(R.string.ok, null)
				.show();
		} else {
			dismiss();
			if (listener != null) {
				listener.onLoginClicked(username, password);
			}
		}
	}
	
	public static interface Listener {
		void onLoginClicked(String userName, String password);
	}
}
