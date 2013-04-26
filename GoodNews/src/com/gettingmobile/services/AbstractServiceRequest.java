package com.gettingmobile.services;

import android.util.Log;
import com.gettingmobile.ApplicationException;
import com.gettingmobile.rest.Request;
import org.apache.http.HttpResponse;

import java.io.IOException;

public abstract class AbstractServiceRequest<T> implements Request<T> {
    public static final String BASE_URL = "http://getting-mobile.appspot.com/api/1/";

	@Override
	public void throwExceptionIfApplicable(HttpResponse response) throws ApplicationException {
		// do nothing by default
	}

    /**
     * Reads the whole response without interpreting it to ensure, that the response's connection can be used again.
     * Can be called by implementations which are not interested in the response's content.
     * @param response the response to be consumed.
     */
    protected void consumeResponse(HttpResponse response) {
        try {
            response.getEntity().consumeContent();
        } catch (IOException ex) {
            Log.w(AbstractServiceRequest.class.getSimpleName(), "Failed to consume the response");
        }
    }
}
