/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.gettingmobile.rest;

import android.util.Log;
import com.gettingmobile.ApplicationException;
import org.apache.http.HttpResponse;

import java.io.IOException;

/**
 *
 * @author sven.wiegand
 */
public abstract class AbstractRequest<T> implements Request<T> {
    public AbstractRequest() {
    }

    @Override
	public void throwExceptionIfApplicable(HttpResponse response)
			throws ApplicationException {
		// do not throw exception by default
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
            Log.w(AbstractRequest.class.getSimpleName(), "Failed to consume the response");
        }
    }
}
