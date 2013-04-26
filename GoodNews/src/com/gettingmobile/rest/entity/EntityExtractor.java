/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.gettingmobile.rest.entity;

import com.gettingmobile.rest.ContentIOException;
import org.apache.http.HttpEntity;

/**
 *
 * @author sven.wiegand
 */
public interface EntityExtractor<T> {
    T extract(HttpEntity entity) throws ContentIOException;
}
