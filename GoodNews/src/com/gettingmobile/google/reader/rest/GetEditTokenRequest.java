package com.gettingmobile.google.reader.rest;

import java.net.URISyntaxException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;

import com.gettingmobile.google.Authenticator;
import com.gettingmobile.rest.ContentIOException;
import com.gettingmobile.rest.entity.StringExtractor;

public class GetEditTokenRequest extends AuthenticatedReaderRequest<HttpGet, String> {
	public GetEditTokenRequest(Authenticator authenticator)
		throws URISyntaxException {
		super("/api/0/token", authenticator);		
	}

	@Override
	public String processResponse(HttpResponse response)
			throws ContentIOException {
		return new StringExtractor().extract(response.getEntity());
	}

	@Override
	protected HttpGet createRequest() {
		return new HttpGet();
	}

}
