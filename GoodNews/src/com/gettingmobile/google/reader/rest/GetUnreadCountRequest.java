package com.gettingmobile.google.reader.rest;

import java.net.URISyntaxException;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;

import com.gettingmobile.google.Authenticator;
import com.gettingmobile.google.reader.UnreadCount;
import com.gettingmobile.rest.ContentIOException;
import com.gettingmobile.rest.entity.JsonExtractor;

public class GetUnreadCountRequest extends AuthenticatedReaderRequest<HttpGet, List<UnreadCount>> {
	private final static JsonExtractor<List<UnreadCount>> unreadCountListExtractor = 
		new JsonExtractor<List<UnreadCount>>(new UnreadCountListJsonAdapter());
	
	public GetUnreadCountRequest(Authenticator authenticator)
		throws URISyntaxException {
		super("/api/0/unread-count?all=true&output=json", authenticator);		
	}

	@Override
	public List<UnreadCount> processResponse(HttpResponse response)
			throws ContentIOException {
		return unreadCountListExtractor.extract(response.getEntity());
	}

	@Override
	protected HttpGet createRequest() {
		return new HttpGet();
	}

}
