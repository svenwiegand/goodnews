package com.gettingmobile.google.reader.rest;

import com.gettingmobile.google.reader.Item;
import com.google.gson.stream.JsonReader;

public class ItemStreamExtractor extends JsonStreamExtractor<Item, ItemStream> {
    @Override
    protected ItemStream createIterator(JsonReader reader) {
        return new ItemStream(reader);
    }
}
