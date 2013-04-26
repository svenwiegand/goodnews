/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.gettingmobile;

/**
 * Extends the {@link Throwable} interface by a string-based error code. This is the base interface for <em>all</em>
 * exceptions in the client module.
 *
 * @author sven.wiegand
 */
public interface ThrowableWithErrorCode {
    /**
     * Returns the exception's error code.
     * @return the exception's error code.
     */
    String getErrorCode();
}
