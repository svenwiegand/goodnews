/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.gettingmobile.io;

import java.io.UnsupportedEncodingException;

/**
 *
 * @author sven.wiegand
 */
public class CharacterSet {
    public static final String UTF8 = "UTF-8";

    public static byte[] stringToBytes(String string) {
        try {
            return string.getBytes(UTF8);
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException("UTF-8 encoding not found", ex);
        }
    }

    public static String stringFromBytes(byte[] data) {
        try {
            return new String(data, UTF8);
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException("UTF-8 encoding not found", ex);
        }
    }
}
