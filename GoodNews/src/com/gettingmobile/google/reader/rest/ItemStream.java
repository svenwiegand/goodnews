package com.gettingmobile.google.reader.rest;

import com.gettingmobile.google.reader.Item;
import com.google.gson.stream.JsonReader;

import java.io.IOException;

public final class ItemStream extends RecoverableElementStream<Item> {
    private String continuationToken = null;
    private long updatedTimeStamp = 0;

    public ItemStream(JsonReader reader) {
        super(reader, new ItemJsonAdapter());
    }

    public boolean hasContinuation() {
        return continuationToken != null;
    }

    public String getContinuationToken() {
        return continuationToken;
    }

    public long getUpdatedTimeStamp() {
        return updatedTimeStamp;
    }

    @Override
    protected boolean readArrayStart() throws IOException {
        reader.beginObject();
        while (reader.hasNext()) {
            final String fieldName = reader.nextName();
            if ("continuation".equals(fieldName)) {
                /*
                 * read continuation token
                 */
                continuationToken = reader.nextString();
            } else if ("updated".equals(fieldName)) {
                /*
                 * read update timestamp
                 */
                updatedTimeStamp = reader.nextLong();
            } else if ("items".equals(fieldName)) {
                /*
                 * we've found the items attribute, so lets finish here
                 */
                reader.beginArray();
                return true;
            } else {
                reader.skipValue();
            }
        }
        return false;
    }
}
