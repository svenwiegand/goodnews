package com.gettingmobile.google.reader.rest;

import com.gettingmobile.ApplicationException;
import com.gettingmobile.google.Authenticator;
import com.gettingmobile.google.AuthorizationException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpRequestBase;

import java.net.URISyntaxException;

public abstract class AuthenticatedReaderRequest<R extends HttpRequestBase, T> extends ReaderRequest<R, T> {
	protected final Authenticator authenticator;

	public AuthenticatedReaderRequest(String relativeUri, Authenticator authenticator)
			throws URISyntaxException {
		super(relativeUri);
		this.authenticator = authenticator;
	}

	@Override
	protected void initRequest(R request) {
		super.initRequest(request);
        if (authenticator != null) {
		    request.setHeader("Authorization", "GoogleLogin auth=" + authenticator.getAuthToken());
        }
	}

	@Override
	public void throwExceptionIfApplicable(HttpResponse response)
			throws ApplicationException {
		super.throwExceptionIfApplicable(response);
    	if (response.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
    		throw new AuthorizationException();
    	}
	}

}
