/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.gettingmobile.rest;

import com.gettingmobile.ApplicationException;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;

/**
 *
 * @author sven.wiegand
 */
public interface Request<T> {
    HttpUriRequest getRequest();
    void throwExceptionIfApplicable(HttpResponse response) throws ApplicationException;
    T processResponse(HttpResponse response) throws ContentIOException;
}
