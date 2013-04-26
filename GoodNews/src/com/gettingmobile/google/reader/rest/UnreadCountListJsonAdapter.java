package com.gettingmobile.google.reader.rest;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.gettingmobile.google.reader.UnreadCount;

public class UnreadCountListJsonAdapter extends ListJsonAdapter<UnreadCount, List<UnreadCount>> {

	public UnreadCountListJsonAdapter() {
		super(new UnreadCountJsonAdapter());
	}

	@Override
	public List<UnreadCount> create() {
		return new ArrayList<UnreadCount>();
	}

	@Override
	protected JSONArray getJsonArray(JSONObject json) throws JSONException {
		return json.getJSONArray("unreadcounts");
	}
	
}
