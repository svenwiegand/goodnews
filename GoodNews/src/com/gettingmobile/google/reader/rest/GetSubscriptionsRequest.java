package com.gettingmobile.google.reader.rest;

import java.net.URISyntaxException;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;

import com.gettingmobile.google.Authenticator;
import com.gettingmobile.google.reader.Feed;
import com.gettingmobile.rest.ContentIOException;
import com.gettingmobile.rest.entity.JsonExtractor;

public class GetSubscriptionsRequest extends AuthenticatedReaderRequest<HttpGet, List<Feed>> {
	private final static JsonExtractor<List<Feed>> feedItemListExtractor = 
		new JsonExtractor<List<Feed>>(new FeedListJsonAdapter());
	
	public GetSubscriptionsRequest(Authenticator authenticator)
		throws URISyntaxException {
		super("/api/0/subscription/list?output=json", authenticator);		
	}

	@Override
	public List<Feed> processResponse(HttpResponse response)
			throws ContentIOException {
		return feedItemListExtractor.extract(response.getEntity());
	}

	@Override
	protected HttpGet createRequest() {
		return new HttpGet();
	}

}
