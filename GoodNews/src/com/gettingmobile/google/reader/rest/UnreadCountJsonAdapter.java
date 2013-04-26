package com.gettingmobile.google.reader.rest;

import com.gettingmobile.google.reader.ElementId;
import com.gettingmobile.google.reader.UnreadCount;
import com.gettingmobile.rest.entity.JsonAdapter;
import org.json.JSONException;
import org.json.JSONObject;

public class UnreadCountJsonAdapter implements JsonAdapter<UnreadCount> {

	@Override
	public UnreadCount create() {
		return new UnreadCount();
	}

	@Override
	public UnreadCount read(JSONObject json) throws JSONException {
		final UnreadCount uc = create();
		
		uc.setElementId(new ElementId(json.getString("id")));
		uc.setCount(json.getInt("count"));
		uc.setNewestItemTimestamp(json.getLong("newestItemTimestampUsec") / 1000);
		
		return uc;
	}

}
