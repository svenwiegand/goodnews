package com.gettingmobile.google.reader.rest;

import com.google.gson.stream.JsonReader;

public class ItemReferenceStreamExtractor extends JsonStreamExtractor<ItemReference, ItemReferenceStream> {
    @Override
    protected ItemReferenceStream createIterator(JsonReader reader) {
        return new ItemReferenceStream(reader);
    }
}
