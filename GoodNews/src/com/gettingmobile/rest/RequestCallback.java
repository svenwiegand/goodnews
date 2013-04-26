/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.gettingmobile.rest;


/**
 *
 * @author sven.wiegand
 */
public interface RequestCallback<R extends Request<T>, T> {
    void onRequestProcessed(R request, T result, Throwable error);
}
