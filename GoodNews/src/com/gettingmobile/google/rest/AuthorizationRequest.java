/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.gettingmobile.google.rest;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.message.BasicNameValuePair;

import com.gettingmobile.ApplicationException;
import com.gettingmobile.google.AuthorizationException;
import com.gettingmobile.io.CharacterSet;
import com.gettingmobile.net.UriFactory;
import com.gettingmobile.rest.AbstractRequest;
import com.gettingmobile.rest.ContentIOException;
import com.gettingmobile.rest.entity.ContentLinesExtractor;

/**
 *
 * @author sven.wiegand
 */
public class AuthorizationRequest extends AbstractRequest<String> {
    private final String authTokenKey = "Auth=";
    private final HttpPost request;

    public AuthorizationRequest(String email, String password, String service) {
        super();
        request = new HttpPost(UriFactory.createUri("https://www.google.com/accounts/ClientLogin"));

        final List<NameValuePair> formValues = new ArrayList<NameValuePair>(4);
        formValues.add(new BasicNameValuePair("accountType", "HOSTED_OR_GOOGLE"));
        formValues.add(new BasicNameValuePair("service", service));
        formValues.add(new BasicNameValuePair("Email", email));
        formValues.add(new BasicNameValuePair("Passwd", password));

        try {
            request.setEntity(new UrlEncodedFormEntity(formValues, CharacterSet.UTF8));
        } catch (UnsupportedEncodingException ex) {
        	// will never happen
            throw new RuntimeException(ex);
        }
    }

    @Override
    public HttpUriRequest getRequest() {
        return request;
    }

    @Override
	public void throwExceptionIfApplicable(HttpResponse response)
			throws ApplicationException {
    	if (response.getStatusLine().getStatusCode() == 403) {
    		throw new AuthorizationException();
    	}
	}

	@Override
    public String processResponse(HttpResponse response) throws ContentIOException {
        final List<String> lines = new ContentLinesExtractor().extract(response.getEntity());
        for (final String line : lines) {
            if (line.startsWith(authTokenKey)) {
                return line.substring(authTokenKey.length());
            }
        }
        return null;
    }

}
