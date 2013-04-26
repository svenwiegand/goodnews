package com.gettingmobile;

public abstract class ApplicationException extends Exception implements
		ThrowableWithErrorCode {
	private static final long serialVersionUID = 1L;
	protected final Enum<?> errorCode;

	public ApplicationException(Enum<?> errorCode) {
		this(errorCode, null, null);
	}

	public ApplicationException(Enum<?> errorCode, String detailMessage) {
		this(errorCode, detailMessage, null);
	}

	public ApplicationException(Enum<?> errorCode, Throwable throwable) {
		this(errorCode, null, throwable);
	}

	public ApplicationException(Enum<?> errorCode, String detailMessage, Throwable throwable) {
		super(errorCode.name() + (detailMessage != null ? ": " + detailMessage : ""), throwable);
		this.errorCode = errorCode;
	}

	@Override
	public String getErrorCode() {
		return errorCode.name();
	}

}
