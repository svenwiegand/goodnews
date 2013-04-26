package com.gettingmobile.rest.entity;

import com.gettingmobile.io.CharacterSet;
import com.gettingmobile.io.IOUtils;
import com.gettingmobile.rest.ContentIOException;
import org.apache.http.HttpEntity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class StringExtractor extends AbstractEntityExtractor<String> {

	@Override
	public String extract(HttpEntity entity) throws ContentIOException {
        BufferedReader r = null;
        try {
            r = new BufferedReader(new InputStreamReader(getContent(entity), CharacterSet.UTF8));
            final StringBuilder content = new StringBuilder();
            for (String line = r.readLine(); line != null; line = r.readLine()) {
                content.append(line);
            }
            return content.toString();
        } catch (IOException ex) {
        	throw new ContentIOException(ex);
        } finally {
            IOUtils.closeQuietly(r);
        }
	}

}
