package com.gettingmobile.google.reader;

public class UnknownElementIdTypeException extends IllegalArgumentException {
    public UnknownElementIdTypeException(String id) {
        super("Type of element id '" + id + "' is unknown.");
    }
}
