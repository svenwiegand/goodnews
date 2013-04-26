package com.gettingmobile.google.reader.rest;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.gettingmobile.google.reader.Tag;

public class TagListJsonAdapter extends SortedElementListJsonAdapter<Tag> {
	
	public TagListJsonAdapter() {
		super(new TagJsonAdapter());
	}

	@Override
	public List<Tag> create() {
		return new ArrayList<Tag>();
	}

	@Override
	protected JSONArray getJsonArray(JSONObject json) throws JSONException {
		return json.getJSONArray("tags");
	}

}
