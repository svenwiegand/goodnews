package com.gettingmobile.rest.entity;

import com.gettingmobile.rest.ContentIOException;
import org.apache.http.HttpEntity;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonExtractor<T> implements EntityExtractor<T> {
	private final StringExtractor stringExtractor = new StringExtractor();
	private final JsonAdapter<T> jsonAdapter;
	
	public JsonExtractor(JsonAdapter<T> jsonAdapter) {
		this.jsonAdapter = jsonAdapter;
	}

	@Override
	public T extract(HttpEntity entity) throws ContentIOException {
		try {
			final JSONObject json = new JSONObject(stringExtractor.extract(entity));
			return jsonAdapter.read(json);
		} catch (JSONException e) {
			throw new ContentIOException(e);
		}
	}
}
