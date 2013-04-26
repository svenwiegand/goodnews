/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.gettingmobile.net;

import java.net.URI;
import java.net.URISyntaxException;

/**
 *
 * @author sven.wiegand
 */
public class UriFactory {
    public static URI createUri(String uri) {
        try {
            return new URI(uri);
        } catch (URISyntaxException ex) {
            throw new IllegalArgumentException("The specified URI (" + uri + ") is not valid.", ex);
        }
    }
}
