package com.gettingmobile.rest;

public class BadRequestException extends HttpStatusException {
	private static final long serialVersionUID = 1L;

	public enum ErrorCode {
		UNAUTHORIZED,
		PAYMENT_REQUIRED,
		FORBIDDEN,
		NOT_FOUND,
		GONE,
		GENERIC_REQUEST_ERROR
	}

	public BadRequestException(ErrorCode errorCode, String details) {
		super(errorCode, details);
	}

}
