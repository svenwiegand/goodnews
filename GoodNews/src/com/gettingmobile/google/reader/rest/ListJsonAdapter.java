package com.gettingmobile.google.reader.rest;

import android.util.Log;
import com.gettingmobile.rest.entity.JsonAdapter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public abstract class ListJsonAdapter<T, L extends List<T>> implements JsonAdapter<L> {
    public static final String LOG_TAG = "goodnews.ListJsonAdapter";
	private final JsonAdapter<T> itemAdapter;
	
	public ListJsonAdapter(JsonAdapter<T> adapter) {
		this.itemAdapter = adapter;
	}
	
	protected JsonAdapter<T> getItemAdapter() {
		return itemAdapter;
	}
	
	protected abstract JSONArray getJsonArray(JSONObject json) throws JSONException;

    /**
     * Called when an error occurs while reading an item for the list. By providing the adequate return value the
     * implementation can decide whether to cancel the reading and throw the exception or to go on reading the next
     * item.
     * @param error the error that occured
     * @return {@code true} to continue reading the next item or {@code false} to cancel reading and throw the
     * exception. This base implementation always returns {@code false}.
     */
    protected boolean onItemReadError(Throwable error) {
        return false;
    }

    protected void handleItemReadError(Throwable error, String json) {
        final String msg =
                "An error occurred reading the following JSON item: '" + json + "'";
        if (onItemReadError(error)) {
            Log.e(LOG_TAG, "Ignored error: " + msg, error);
        } else {
            throw new RuntimeException(msg, error);
        }
    }

	@Override
	public L read(JSONObject json) throws JSONException {
		final L elements = create();
		
		final JSONArray jsonElements = getJsonArray(json);
		for (int i = 0; i < jsonElements.length(); ++i) {
            final JSONObject o = jsonElements.getJSONObject(i);
            try {
			    elements.add(itemAdapter.read(o));
            } catch (Throwable error) {
                handleItemReadError(error, o.toString());
            }
		}
		
		return elements;
	}	
}
