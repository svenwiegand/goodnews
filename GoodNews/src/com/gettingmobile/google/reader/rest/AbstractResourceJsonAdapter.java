package com.gettingmobile.google.reader.rest;

import com.gettingmobile.google.reader.Resource;
import com.gettingmobile.rest.entity.JsonAdapter;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class AbstractResourceJsonAdapter<T extends Resource> implements JsonAdapter<T> {

	@Override
	public T read(JSONObject json) throws JSONException {
		final T r = create();
		
		r.setHref(json.getString("href"));
		r.setMimeType(json.optString("type", null));
		
		return r;
	}
	
}
