package com.gettingmobile.google.reader.rest;

import com.gettingmobile.google.reader.ElementId;
import com.gettingmobile.google.reader.Item;
import com.gettingmobile.google.reader.ItemState;
import com.gettingmobile.google.reader.Tag;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class ItemJsonAdapter extends ElementJsonAdapter<Item> {

	@Override
	public Item create() {
		return new Item();
	}

	@Override
	public Item read(JSONObject json) throws JSONException {
		final Item itm = super.read(json);

        final JSONObject jsonOrigin = json.getJSONObject("origin");
		itm.setFeedId(new ElementId(jsonOrigin.getString("streamId")));
        itm.setFeedTitle(jsonOrigin.optString("title", ""));
		itm.setTitle(json.optString("title", ""));
		
		/*
		 * additional properties
		 */
		long timestamp = json.optLong("updated", 0);
		if (timestamp == 0) {
			timestamp = json.optLong("published", 0);
		}
		itm.setTimestamp(timestamp != 0 ? new Date(1000 * timestamp) : null);
		itm.setAuthor(json.optString("author", null));
		
		/*
		 * read category IDs
		 */
		final JSONArray jsonCategories = json.optJSONArray("categories");
		if (jsonCategories != null) {
			for (int i = 0; i < jsonCategories.length(); ++i) {
				try {
					final ElementId tagId = new ElementId(jsonCategories.getString(i));
					if (Tag.isUsed(tagId)) {
						itm.getTagIds().add(tagId);
					} else if (ItemState.READ.getId().equals(tagId)) {
						itm.setRead(true);
					}
				} catch (IllegalArgumentException ex) {
					// ignore none-google-reader tags
				}
			}
		}

		/*
		 * read alternates
		 */
		final JSONArray jsonAlternate = json.optJSONArray("alternate");
		if (jsonAlternate != null && jsonAlternate.length() >= 1) {
			itm.setAlternate(new ResourceJsonAdapter().read(jsonAlternate.getJSONObject(0)));
		}
		
		/*
		 * content information
		 */
		itm.setSummary(optContent(json, "summary"));
		itm.setContent(optContent(json, "content"));
				
		return itm;
	}
	
	protected String optContent(JSONObject json, String name) {
		final JSONObject o = json.optJSONObject(name);
		if (o != null) {
			return o.optString("content", null);
		} else {
			return null;
		}
	}	
}
