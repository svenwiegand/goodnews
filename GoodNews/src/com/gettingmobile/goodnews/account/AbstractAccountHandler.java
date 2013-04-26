package com.gettingmobile.goodnews.account;

import android.app.Dialog;
import com.gettingmobile.android.app.actions.ActionContext;
import com.gettingmobile.goodnews.Application;
import com.gettingmobile.goodnews.settings.Settings;

public abstract class AbstractAccountHandler implements AccountHandler {
    protected final Application app;
    protected Dialog promptDialog = null;

	public AbstractAccountHandler(Application app) {
        this.app = app;
	}

	protected String getString(int resId) {
		return app.getString(resId);
	}

	protected Application getApp() {
		return app;
	}

	protected Settings getSettings() {
		return app.getSettings();
	}

	protected void invalidateCache() {
        getSettings().resetMinUnreadTimestamp();
		getApp().getDbHelper().clean();
	}

    @Override
    public boolean isPromptShowing() {
        return promptDialog != null && promptDialog.isShowing();
    }

    @Override
    public void promptAccount(ActionContext actionContext, LoginCallback callback) {
    }

    protected void onAuthenticationFinished(final LoginCallback loginCallback, Throwable error, final String authToken) {
		if (error == null) {
			getApp().authenticate(authToken);
            fireOnLoginSucceeded(loginCallback);
        } else {
			fireOnLoginFailed(loginCallback, error);
		}
	}

    /*
     * callback handling
     */

    protected void fireOnLoginStarted(LoginCallback loginCallback) {
        if (loginCallback != null) {
            loginCallback.onLoginStarted();
        }
    }

	protected void fireOnLoginFailed(LoginCallback loginCallback, Throwable error) {
        if (loginCallback != null) {
            if (error == null) {
                error = new Exception("Unknown login error");
            }
            loginCallback.onLoginFinished(error);
        }
	}

    protected void fireOnLoginSucceeded(LoginCallback loginCallback) {
        if (loginCallback != null) {
            loginCallback.onLoginFinished(null);
        }
    }
	
}
