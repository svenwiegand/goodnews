package com.gettingmobile.google.reader.rest;

public class JsonStreamException extends RuntimeException {
    private final boolean recoverable;

    public JsonStreamException(Throwable cause, boolean recoverable) {
        super(cause);
        this.recoverable = recoverable;
    }

    public boolean isRecoverable() {
        return recoverable;
    }
}
