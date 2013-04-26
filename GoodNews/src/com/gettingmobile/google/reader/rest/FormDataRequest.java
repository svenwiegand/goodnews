package com.gettingmobile.google.reader.rest;

import android.util.Log;
import com.gettingmobile.google.Authenticator;
import com.gettingmobile.io.CharacterSet;
import com.gettingmobile.rest.ContentIOException;
import com.gettingmobile.rest.entity.StringExtractor;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public abstract class FormDataRequest<T> extends AuthenticatedReaderRequest<HttpPost, T> {
    private final String LOG_TAG = "goodnews.FormDataRequest";
	private final StringExtractor stringExtractor = new StringExtractor();
	
	public FormDataRequest(String supUrl, Authenticator authenticator) throws URISyntaxException {
		super("/api/0/" + supUrl, authenticator);
	}

	@Override
	protected HttpPost createRequest() {
		return new HttpPost();
	}
	
	protected HttpEntity getEntity() {
        final List<NameValuePair> formValues = new ArrayList<NameValuePair>();
        setFormValues(formValues);
        try {
            return new UrlEncodedFormEntity(formValues, CharacterSet.UTF8);
        } catch (UnsupportedEncodingException ex) {
        	/*
        	 *  will not happen, as UTF8 is always supported
        	 */
        	Log.e(FormDataRequest.class.getName(), ex.getMessage());
            throw new RuntimeException(ex); 
        }        		
		
	}

	@Override
	protected void initRequest(HttpPost request) {
		super.initRequest(request);
		request.setEntity(getEntity());
	}
	
	protected void setFormValues(List<NameValuePair> formValues) {
	}

	@Override
	public String toString() {
		try {
			return super.toString() + "\n" + stringExtractor.extract(getEntity());
		} catch (ContentIOException ex) {
			return super.toString();
		}
	}	
}
