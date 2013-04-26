package com.gettingmobile.google.reader.rest;

import com.gettingmobile.rest.ContentIOException;
import com.gettingmobile.rest.entity.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class JsonStream<T> implements Iterator<T>, Closeable {
    protected final JsonReader reader;
    private final JsonAdapter<T> adapter;
    private boolean hasNext = false;

    protected JsonStream(JsonReader reader, JsonAdapter<T> adapter) {
        this.reader = reader;
        this.adapter = adapter;
    }

    protected void startReading() throws IOException {
        hasNext = readArrayStart() && hasNextObjectStart();
        if (!hasNext()) {
            readToEnd();
        }
    }

    public List<T> readAsList() {
        final List<T> list = new ArrayList<T>();
        while (hasNext()) {
            list.add(next());
        }
        return list;
    }

    @Override
    public boolean hasNext() {
        return hasNext;
    }

    protected boolean isExceptionRecoverable(Exception ex) {
        return false;
    }

    protected void prepareNext() throws IOException {
        hasNext  = hasNextObjectStart();
        if (!hasNext()) {
            readToEnd();
        }
    }

    @Override
    public T next() {
        T next = null;
        JsonStreamException error = null;
        try {
            next = readNextObject();
        } catch (JsonStreamException ex) {
            error = ex;
        } catch (Exception ex) {
            error = new JsonStreamException(ex, isExceptionRecoverable(ex));
        }
        if (error == null || error.isRecoverable()) {
            try {
                prepareNext();
            } catch (IOException ex) {
                throw error != null ? error : new JsonStreamException(ex, false);
            }
        }
        if (error != null)
            throw error;

        return next;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("JsonStream.remove does not make sense");
    }

    @Override
    public void close() throws IOException {
        hasNext = false;
        reader.close();
    }

    protected abstract boolean readArrayStart() throws IOException;

    protected boolean hasNextObjectStart() throws IOException {
        return reader.hasNext() && reader.peek() == JsonToken.BEGIN_OBJECT;
    }

    protected T readNextObject() throws IOException, JSONException {
        return readObject(JsonObjectReader.readObject(reader));
    }

    protected T readObject(JSONObject json) throws JSONException, ContentIOException {
        return adapter.read(json);
    }

    protected void readToEnd() throws IOException {
        reader.close();
    }
}
