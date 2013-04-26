package com.gettingmobile.rest;

import java.io.IOException;

public class ContentIOException extends IOException {
	private static final long serialVersionUID = 1L;

	public ContentIOException() {
		super();
	}

	public ContentIOException(String detailMessage, Throwable cause) {
		super(detailMessage);
		initCause(cause);
	}

	public ContentIOException(String detailMessage) {
		super(detailMessage);
	}

	public ContentIOException(Throwable cause) {
		super();
		initCause(cause);
	}

}
