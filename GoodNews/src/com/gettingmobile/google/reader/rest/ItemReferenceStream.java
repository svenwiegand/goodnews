package com.gettingmobile.google.reader.rest;

import com.google.gson.stream.JsonReader;

import java.io.IOException;

public class ItemReferenceStream extends RecoverableElementStream<ItemReference> {
    public ItemReferenceStream(JsonReader reader) {
        super(reader, new ItemReferenceJsonAdapter());
    }

    @Override
    protected boolean readArrayStart() throws IOException {
        reader.beginObject();
        reader.nextName();
        reader.beginArray();
        return true;
    }
}
