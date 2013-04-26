package com.gettingmobile.google.reader.rest;

import com.gettingmobile.google.Authenticator;
import com.gettingmobile.google.reader.ItemState;
import com.gettingmobile.rest.ContentIOException;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;

import java.net.URISyntaxException;

public abstract class GetFeedRequest extends AuthenticatedReaderRequest<HttpGet, ItemStream> {
	private static int pageSize = 50;
	private static final ItemStreamExtractor itemStreamExtractor = new ItemStreamExtractor();

	public static int getPageSize() {
		return pageSize;
	}
	
	public static void setPageSize(int pageSize) {
		GetFeedRequest.pageSize = pageSize;
	}
	
	public GetFeedRequest(Authenticator authenticator, String continuationToken, String relativeUri, 
			ItemState exclude, long startTime, int maxCount)
			throws URISyntaxException {
		super("/api/0/stream/contents/" + 
				relativeUri + "?n=" + Math.min(pageSize, maxCount) +
				(exclude != null ? "&xt=" + exclude.getIdText() : "") +
                (startTime > 0 ? "&ot=" + startTime : "") +
				(continuationToken != null ? "&c=" + continuationToken : ""), 
				authenticator);		
	}

	@Override
	protected HttpGet createRequest() {
		return new HttpGet();
	}
	
	@Override
	public ItemStream processResponse(HttpResponse response) throws ContentIOException {
		return itemStreamExtractor.extract(response.getEntity());
	}
	
}
