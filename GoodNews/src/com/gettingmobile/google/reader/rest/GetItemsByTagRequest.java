package com.gettingmobile.google.reader.rest;

import com.gettingmobile.google.Authenticator;
import com.gettingmobile.google.reader.ElementId;
import com.gettingmobile.google.reader.ItemState;

import java.net.URISyntaxException;

public class GetItemsByTagRequest extends GetFeedRequest {
	public GetItemsByTagRequest(Authenticator authenticator, String continuationToken, 
			ElementId tag, ItemState exclude)
			throws URISyntaxException {
		super(authenticator, continuationToken, tag.getUrlEncodedId(), exclude, 0, getPageSize());
	}
	
	public GetItemsByTagRequest(Authenticator authenticator, String continuationToken, 
			ElementId tag, ItemState exclude, int maxCount)
			throws URISyntaxException {
		super(authenticator, continuationToken, tag.getUrlEncodedId(), exclude, 0, maxCount);
	}

    public GetItemsByTagRequest(Authenticator authenticator, String continuationToken,
            ElementId tag, ItemState exclude, long startTime, int maxCount)
            throws URISyntaxException {
        super(authenticator, continuationToken, tag.getUrlEncodedId(), exclude, startTime, maxCount);
    }
}
