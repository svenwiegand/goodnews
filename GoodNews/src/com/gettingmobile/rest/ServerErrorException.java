package com.gettingmobile.rest;


public class ServerErrorException extends HttpStatusException {
	private static final long serialVersionUID = 1L;

	public enum ErrorCode {
		INTERNAL_SERVER_ERROR,
		NOT_IMPLEMENTED,
		SERVICE_UNAVAILABLE,
		GENERIC_SERVER_ERROR
	}

	public ServerErrorException(ErrorCode errorCode, String details) {
		super(errorCode, details);
	}
}
