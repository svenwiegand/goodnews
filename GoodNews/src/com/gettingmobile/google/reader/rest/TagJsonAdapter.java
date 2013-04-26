package com.gettingmobile.google.reader.rest;

import com.gettingmobile.google.reader.Tag;

public class TagJsonAdapter extends SortedElementJsonAdapter<Tag> {

	@Override
	public Tag create() {
		return new Tag();
	}
}
