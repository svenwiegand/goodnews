package com.gettingmobile.rest;

import com.gettingmobile.ThrowableWithErrorCode;

import java.io.IOException;

public abstract class HttpStatusException extends IOException implements ThrowableWithErrorCode {
	private static final long serialVersionUID = 1L;
	private final Enum<?> errorCode;
	
	public HttpStatusException(Enum<?> errorCode, String details) {
		super(errorCode.name() + "; Details:\n" + details);
		this.errorCode = errorCode;
	}

	@Override
	public String getErrorCode() {
		return errorCode.name();
	}
}
