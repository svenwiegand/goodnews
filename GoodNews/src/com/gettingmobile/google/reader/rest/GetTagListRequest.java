package com.gettingmobile.google.reader.rest;

import java.net.URISyntaxException;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;

import com.gettingmobile.google.Authenticator;
import com.gettingmobile.google.reader.Tag;
import com.gettingmobile.rest.ContentIOException;
import com.gettingmobile.rest.entity.JsonExtractor;

public class GetTagListRequest extends AuthenticatedReaderRequest<HttpGet, List<Tag>> {
	private final static JsonExtractor<List<Tag>> tagListExtractor = 
		new JsonExtractor<List<Tag>>(new TagListJsonAdapter());
	
	public GetTagListRequest(Authenticator authenticator)
		throws URISyntaxException {
		super("/api/0/tag/list?output=json", authenticator);		
	}

	@Override
	public List<Tag> processResponse(HttpResponse response)
			throws ContentIOException {
		return tagListExtractor.extract(response.getEntity());
	}

	@Override
	protected HttpGet createRequest() {
		return new HttpGet();
	}

}
