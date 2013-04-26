package com.gettingmobile.google;

public interface Authenticator {
	boolean hasValidAuthToken();
	String getAuthToken();
	boolean hasValidEditToken();
	String getEditToken();
	void setEditToken(String editToken);
}
