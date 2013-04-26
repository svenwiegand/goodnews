package com.gettingmobile.google.reader.rest;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.gettingmobile.google.reader.Feed;

public class FeedListJsonAdapter extends SortedElementListJsonAdapter<Feed> {
	
	public FeedListJsonAdapter() {
		super(new FeedJsonAdapter());
	}

	@Override
	public List<Feed> create() {
		return new ArrayList<Feed>();
	}

	@Override
	protected JSONArray getJsonArray(JSONObject json) throws JSONException {
		return json.getJSONArray("subscriptions");
	}

}
