package com.gettingmobile.google.reader.db;

import com.gettingmobile.google.reader.ElementId;
import com.gettingmobile.util.StringUtils;

import java.util.HashSet;
import java.util.Set;

public class TagIdListStringAdapter implements StringAdapter<Set<ElementId>> {
	public String write(Set<ElementId> tags) {
		return StringUtils.explode(tags, ",", "\"");
	}
	
	public Set<ElementId> read(String string) {
		final Set<ElementId> tagIds = new HashSet<ElementId>();
		if (string != null && string.length() > 0) {
			final String[] tagNames = string.split(",");
			for (String tagName : tagNames) {
				tagIds.add(new ElementId(tagName.substring(1, tagName.length() - 1)));
			}
		}		
		return tagIds;
	}
}
