/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.gettingmobile.rest.entity;

import com.gettingmobile.io.CharacterSet;
import com.gettingmobile.io.IOUtils;
import com.gettingmobile.rest.ContentIOException;
import org.apache.http.HttpEntity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author sven.wiegand
 */
public class ContentLinesExtractor extends AbstractEntityExtractor<List<String>> {
    @Override
    public List<String> extract(HttpEntity entity) throws ContentIOException {
        BufferedReader r = null;
        try {
            r = new BufferedReader(new InputStreamReader(getContent(entity), CharacterSet.UTF8));
            final List<String> lines = new ArrayList<String>();
            for (String line = r.readLine(); line != null; line = r.readLine()) {
                lines.add(line);
            }
            return lines;
        } catch (IOException ex) {
        	throw new ContentIOException("Failed to read content lines", ex);
        } finally {
            IOUtils.closeQuietly(r);
        }
    }
    
}
