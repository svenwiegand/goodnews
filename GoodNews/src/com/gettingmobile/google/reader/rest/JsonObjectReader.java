package com.gettingmobile.google.reader.rest;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public final class JsonObjectReader {
    private JsonObjectReader() {
    }

    private static Object read(JsonReader reader) throws IOException, JSONException {
        final JsonToken token = reader.peek();
        if (token == JsonToken.BEGIN_OBJECT) {
            return readObject(reader);
        } else if (token == JsonToken.BEGIN_ARRAY) {
            return readArray(reader);
        } else if (token == JsonToken.BOOLEAN) {
            return reader.nextBoolean();
        } else if (token == JsonToken.NULL) {
            reader.nextNull();
            return null;
        } else if (token == JsonToken.NUMBER) {
            try {
                return reader.nextLong();
            } catch (NumberFormatException ex) {
                return reader.nextDouble();
            }
        } else if (token == JsonToken.STRING) {
            return reader.nextString();
        } else {
            reader.skipValue();
            return null;
        }
    }

    public static JSONObject readObject(JsonReader reader) throws IOException, JSONException {
        reader.beginObject();

        final JSONObject o = new JSONObject();
        while (reader.peek() == JsonToken.NAME) {
            o.put(reader.nextName(), read(reader));
        }
        reader.endObject();
        return o;
    }

    public static JSONArray readArray(JsonReader reader) throws IOException, JSONException {
        reader.beginArray();

        final JSONArray a = new JSONArray();
        while (reader.peek() != JsonToken.END_ARRAY) {
            a.put(read(reader));
        }
        reader.endArray();
        return a;
    }
}
