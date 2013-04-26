package com.gettingmobile.goodnews.account;

import android.util.Log;
import com.gettingmobile.goodnews.Application;

public class AccountHandlerFactory {
	private static final AccountHandlerFactory instance = new AccountHandlerFactory();
	
	public static AccountHandlerFactory getInstance() {
		return instance;
	}
	
	public AccountHandler createAccountHandler(Application app) {
		try {
			return new AccountManagerHandler(app);
		} catch (VerifyError ex) {
			Log.i(getClass().getName(), 
					"Failed to load AccountManagerHandler -- seems to be an Android version prior to 2.0: " + 
					ex.toString());
			return new CredentialsAccountHandler(app);
		}
	}
}
