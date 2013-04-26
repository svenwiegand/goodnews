package com.gettingmobile.google.reader.rest;

import com.gettingmobile.io.CharacterSet;
import com.gettingmobile.io.IOUtils;
import com.gettingmobile.rest.ContentIOException;
import com.gettingmobile.rest.entity.AbstractEntityExtractor;
import com.google.gson.stream.JsonReader;
import org.apache.http.HttpEntity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;

public abstract class JsonStreamExtractor<V, T extends JsonStream<V>> extends AbstractEntityExtractor<T> {
    protected abstract T createIterator(JsonReader parser);

    @Override
    public T extract(HttpEntity entity) throws ContentIOException {
        Reader reader = null;
        T stream = null;
        try {
            reader = new BufferedReader(new InputStreamReader(getContent(entity), CharacterSet.UTF8));
            final JsonReader jsonReader = new JsonReader(reader);
            stream = createIterator(jsonReader);
            stream.startReading();
            return stream;
        } catch (ContentIOException ex) {
            IOUtils.closeQuietly(stream != null ? stream : reader);
            throw ex;
        } catch (Throwable ex) {
            IOUtils.closeQuietly(stream != null ? stream : reader);
            throw new ContentIOException(ex);
        }
    }
}
