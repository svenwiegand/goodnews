package com.gettingmobile.google.reader.sync;

public class SyncException extends RuntimeException {
    public enum ErrorCode {
        CANCELLED,
        CONNECTION,
        STORAGE,
        DEVICE_STORAGE_LOW,
        GENERIC
    }
    private final ErrorCode errorCode;

    public SyncException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.name(), cause);
        this.errorCode = errorCode;
    }

    public SyncException(ErrorCode errorCode) {
        super(errorCode.name());
        this.errorCode = errorCode;
    }

    public SyncException(Throwable cause) {
        this(ErrorCode.GENERIC, cause);
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    @Override
    public String getLocalizedMessage() {
        return super.getLocalizedMessage();    //To change body of overridden methods use File | Settings | File Templates.
    }
}
