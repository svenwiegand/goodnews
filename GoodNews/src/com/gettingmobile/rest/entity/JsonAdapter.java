package com.gettingmobile.rest.entity;

import org.json.JSONException;
import org.json.JSONObject;

public interface JsonAdapter<T> {
	T create();
	T read(JSONObject json) throws JSONException;
}
