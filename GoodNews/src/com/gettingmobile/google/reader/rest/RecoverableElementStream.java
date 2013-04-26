package com.gettingmobile.google.reader.rest;

import com.gettingmobile.google.reader.UnknownElementIdTypeException;
import com.gettingmobile.rest.entity.JsonAdapter;
import com.google.gson.stream.JsonReader;

abstract class RecoverableElementStream<T> extends JsonStream<T> {
    protected RecoverableElementStream(JsonReader reader, JsonAdapter<T> adapter) {
        super(reader, adapter);
    }

    @Override
    protected boolean isExceptionRecoverable(Exception ex) {
        return ex instanceof UnknownElementIdTypeException;
    }
}
