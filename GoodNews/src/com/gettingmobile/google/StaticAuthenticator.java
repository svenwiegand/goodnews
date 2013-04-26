package com.gettingmobile.google;

public final class StaticAuthenticator implements Authenticator {
	public static final int AUTH_TOKEN_TIMEOUT_MS = 1000 /*ms*/ * 60 /*s*/ * 60 /*min*/ * 12 /*h*/; // we assume 12 hours
	public static final int EDIT_TOKEN_TIMEOUT_MS = 1000 /*ms*/ * 60 /*s*/ * 30 /*min*/; // we assume 30 minutes
	private String authToken = "";
	private long authTokenTimestamp = 0;
	private String editToken = "";
	private long editTokenTimestamp = 0;
	
	public StaticAuthenticator(String authToken) {
		setAuthToken(authToken);
	}
	
	public StaticAuthenticator() {
		this(null);
	}

	@Override
	public boolean hasValidAuthToken() {
		return authToken.length() > 0 && (System.currentTimeMillis() - authTokenTimestamp) < AUTH_TOKEN_TIMEOUT_MS;
	}

	@Override
	public String getAuthToken() {
		return authToken;
	}
	
	public void setAuthToken(String authToken) {
		this.authToken = authToken != null ? authToken : "";
		authTokenTimestamp = System.currentTimeMillis();
	}

	@Override
	public boolean hasValidEditToken() {
		return editToken.length() > 0 && (System.currentTimeMillis() - editTokenTimestamp) < EDIT_TOKEN_TIMEOUT_MS;
	}

	@Override
	public String getEditToken() {
		return editToken;
	}

	@Override
	public void setEditToken(String editToken) {
		this.editToken = editToken != null ? editToken : "";
		editTokenTimestamp = System.currentTimeMillis();
	}
}
