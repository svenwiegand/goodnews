package com.gettingmobile.google.reader.rest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.gettingmobile.google.reader.ElementId;
import com.gettingmobile.google.reader.Feed;

public class FeedJsonAdapter extends SortedElementJsonAdapter<Feed> {

	@Override
	public Feed create() {
		return new Feed();
	}

	@Override
	public Feed read(JSONObject json) throws JSONException {
		final Feed f = super.read(json);
		
		/*
		 * read additional attributes
		 */
		f.setTitle(json.optString("title", ""));
		f.setHtmlUrl(json.optString("htmlUrl", null));
		
		/*
		 * read category IDs
		 */
		final JSONArray jsonCategories = json.optJSONArray("categories");
		if (jsonCategories != null) {
			for (int i = 0; i < jsonCategories.length(); ++i) {
				f.getTagIds().add(new ElementId(jsonCategories.getJSONObject(i).getString("id")));
			}
		} else {
			f.getTagIds().clear();
		}
		
		return f;
	}

}
