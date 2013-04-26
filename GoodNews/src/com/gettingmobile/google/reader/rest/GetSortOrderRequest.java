package com.gettingmobile.google.reader.rest;

import com.gettingmobile.google.Authenticator;
import com.gettingmobile.rest.ContentIOException;
import com.gettingmobile.rest.entity.JsonExtractor;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;

import java.net.URISyntaxException;

public class GetSortOrderRequest extends AuthenticatedReaderRequest<HttpGet, StreamContentOrder> {
	private static final JsonExtractor<StreamContentOrder> extractor = new JsonExtractor<StreamContentOrder>(new SortOrderJsonAdapter());
	
	public GetSortOrderRequest(Authenticator authenticator)
		throws URISyntaxException {
		super("/api/0/preference/stream/list?output=json", authenticator);
	}

	@Override
	public StreamContentOrder processResponse(HttpResponse response) throws ContentIOException {
		return extractor.extract(response.getEntity());
	}

	@Override
	protected HttpGet createRequest() {
		return new HttpGet();
	}
}
