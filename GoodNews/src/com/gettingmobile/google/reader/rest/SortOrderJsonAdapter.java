package com.gettingmobile.google.reader.rest;

import com.gettingmobile.google.reader.ElementId;
import com.gettingmobile.rest.entity.JsonAdapter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public final class SortOrderJsonAdapter implements JsonAdapter<StreamContentOrder> {
	private static final String ID_SUBSCRIPTION_ORDERING = "subscription-ordering";
	private static final int SORT_ITEM_LENGTH = 8;

	@Override
	public StreamContentOrder create() {
		return new StreamContentOrder();
	}

    @SuppressWarnings("unchecked")
	@Override
	public StreamContentOrder read(JSONObject json) throws JSONException {
		final StreamContentOrder sortOrder = create();

        final JSONObject streamPrefs = json.optJSONObject("streamprefs");
        if (streamPrefs != null) {
            for (Iterator<String> it = streamPrefs.keys(); it.hasNext(); ) {
                final String key = it.next();
                final JSONArray streamValues = streamPrefs.optJSONArray(key);
                if (streamValues != null) {
                    for (int i = 0; i < streamValues.length(); ++i) {
                        final JSONObject value = streamValues.getJSONObject(i);
                        final String id = value.optString("id");
                        if (ID_SUBSCRIPTION_ORDERING.equals(id)) {
                            sortOrder.put(new ElementId(key), splitSortIds(value.getString("value")));
                        }
                    }
                }
            }
        }

        return sortOrder;
	}

    protected String[] splitSortIds(String sortIdString) {
        assert (sortIdString.length() % SORT_ITEM_LENGTH) == 0;
        final int count = sortIdString.length() / SORT_ITEM_LENGTH;
        final String[] sortIds = new String[count];
        for (int i = 0; i < count; ++i) {
            sortIds[i] = sortIdString.substring(i * SORT_ITEM_LENGTH, i * SORT_ITEM_LENGTH + SORT_ITEM_LENGTH);
        }
        return sortIds;
    }
}
