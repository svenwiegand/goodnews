package com.gettingmobile.google.reader.rest;

import com.gettingmobile.google.reader.SortedElement;
import com.gettingmobile.google.reader.UnknownElementIdTypeException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

public abstract class SortedElementListJsonAdapter<T extends SortedElement> extends ListJsonAdapter<T, List<T>> {
	
	public SortedElementListJsonAdapter(ElementJsonAdapter<T> elementAdapter) {
		super(elementAdapter);
	}

	@Override
	public List<T> read(JSONObject json) throws JSONException {
		final JSONArray jsonElements = getJsonArray(json);
		final SortedSet<T> sortedElements = new TreeSet<T>();
		for (int i = 0; i < jsonElements.length(); ++i) {
            final JSONObject o = jsonElements.getJSONObject(i);
            try {
			    sortedElements.add(getItemAdapter().read(o));
            } catch (Throwable error) {
                handleItemReadError(error, o.toString());
            }
		}

		final List<T> elements = create();
		elements.addAll(sortedElements);
		
		return elements;
	}

    @Override
    protected boolean onItemReadError(Throwable error) {
        return error instanceof UnknownElementIdTypeException;
    }

}
