package com.gettingmobile.google.reader.rest;

import com.gettingmobile.google.Authenticator;
import com.gettingmobile.google.reader.ElementId;
import com.gettingmobile.rest.ContentIOException;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;

public class ChangeItemTagRequest extends FormDataRequest<Boolean> {
    public static final int MAX_ITEMS = 100;
	private final Collection<ElementId> feedIds;
	private final Collection<ElementId> itemIds;
	private final ElementId addTag;
	private final ElementId removeTag;
	
	public ChangeItemTagRequest(Authenticator authenticator, 
			Collection<ElementId> feedIds, Collection<ElementId> itemIds, 
			ElementId addTag, ElementId removeTag) throws URISyntaxException {
		super("edit-tag", authenticator);
		
		if (itemIds.size() != feedIds.size())
			throw new IllegalArgumentException("There must be one feed ID for each item ID");
		
		this.feedIds = feedIds;
		this.itemIds = itemIds;
		this.addTag = addTag;
		this.removeTag = removeTag;
	}

    @Override
    public Boolean processResponse(HttpResponse response) throws ContentIOException {
        consumeResponse(response);
        return Boolean.TRUE;
    }

	@Override
	protected void setFormValues(List<NameValuePair> formValues) {
		super.setFormValues(formValues);
		
        formValues.add(new BasicNameValuePair("T", authenticator.getEditToken()));
        formValues.add(new BasicNameValuePair("async", "false"));
        for (ElementId feedId : feedIds) {
        	formValues.add(new BasicNameValuePair("s", feedId.getId()));
        }
        for (ElementId itemId : itemIds) {
        	formValues.add(new BasicNameValuePair("i", itemId.getId()));
        }
        if (addTag != null) {
            formValues.add(new BasicNameValuePair("a", addTag.getId()));
        }
        if (removeTag != null) {
            formValues.add(new BasicNameValuePair("r", removeTag.getId()));
        }
	}
}
