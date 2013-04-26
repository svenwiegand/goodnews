package com.gettingmobile.rest;

public class RedirectionException extends HttpStatusException {
	private static final long serialVersionUID = 1L;
	
	public enum ErrorCode {
		REDIRECTION
	}
	
	public RedirectionException(ErrorCode errorCode, String details) {
		super(errorCode, details);
	}
}
