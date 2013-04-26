package com.gettingmobile.google.reader.rest;

import com.gettingmobile.google.reader.Element;
import com.gettingmobile.google.reader.ElementId;
import com.gettingmobile.rest.entity.JsonAdapter;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class ElementJsonAdapter<T extends Element> implements JsonAdapter<T> {

	@Override
	public T read(JSONObject json) throws JSONException {
		final T e = create();
		e.setId(new ElementId(json.getString("id")));
		return e;
	}
	
}
