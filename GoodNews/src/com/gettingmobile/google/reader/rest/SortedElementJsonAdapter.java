package com.gettingmobile.google.reader.rest;

import org.json.JSONException;
import org.json.JSONObject;

import com.gettingmobile.google.reader.SortedElement;

public abstract class SortedElementJsonAdapter<T extends SortedElement> extends ElementJsonAdapter<T> {

	@Override
	public T read(JSONObject json) throws JSONException {
		final T e = super.read(json);
		
		e.setSortId(json.getString("sortid"));
		
		return e;
	}

}
