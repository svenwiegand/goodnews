package com.gettingmobile.google;

import com.gettingmobile.ApplicationException;

public class AuthorizationException extends ApplicationException {
	private static final long serialVersionUID = 1L;

	public enum ErrorCode {
		AUTHORIZATION_FAILED
	}
	
	public AuthorizationException() {
		super(ErrorCode.AUTHORIZATION_FAILED);
	}
}
