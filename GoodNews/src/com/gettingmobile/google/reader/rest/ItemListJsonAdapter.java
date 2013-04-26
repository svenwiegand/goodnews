package com.gettingmobile.google.reader.rest;

import com.gettingmobile.google.reader.Item;
import com.gettingmobile.google.reader.UnknownElementIdTypeException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ItemListJsonAdapter extends ListJsonAdapter<Item, ItemList> {
	
	public ItemListJsonAdapter() {
		super(new ItemJsonAdapter());
	}

	@Override
	public ItemList create() {
		return new ItemList();
	}

	@Override
	protected JSONArray getJsonArray(JSONObject json) throws JSONException {
		return json.getJSONArray("items");
	}

	@Override
	public ItemList read(JSONObject json) throws JSONException {
		final ItemList items = super.read(json);
		
		items.setContinuationToken(json.optString("continuation", null));
        items.setUpdatedTimeStamp(Long.parseLong(json.optString("updated", "0")));
		
		return items;
	}

    @Override
    protected boolean onItemReadError(Throwable error) {
        return error instanceof UnknownElementIdTypeException;
    }
}
