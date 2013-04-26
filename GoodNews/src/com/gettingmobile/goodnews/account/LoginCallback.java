package com.gettingmobile.goodnews.account;

public interface LoginCallback {
    void onLoginStarted();
	void onLoginFinished(Throwable error);
}
