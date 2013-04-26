package com.gettingmobile.google.reader.rest;

import com.gettingmobile.google.reader.ElementId;
import com.gettingmobile.rest.entity.JsonAdapter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ItemReferenceJsonAdapter implements JsonAdapter<ItemReference> {
    @Override
    public ItemReference create() {
        return new ItemReference();
    }

    @Override
    public ItemReference read(JSONObject json) throws JSONException {
        final ItemReference ref = create();

        ref.setId(Long.parseLong(json.getString("id")));
        ref.setTimestampUSec(Long.parseLong(json.getString("timestampUsec")));

        final JSONArray jsonDirectStreamIds = json.getJSONArray("directStreamIds");
        final List<ElementId> directStreamIds = new ArrayList<ElementId>(jsonDirectStreamIds.length());
        for (int i = 0; i < jsonDirectStreamIds.length(); ++i) {
            directStreamIds.add(new ElementId(jsonDirectStreamIds.getString(i)));
        }
        ref.setDirectStreamIds(directStreamIds);

        return ref;
    }
}
