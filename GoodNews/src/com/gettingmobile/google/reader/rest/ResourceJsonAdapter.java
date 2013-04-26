package com.gettingmobile.google.reader.rest;

import com.gettingmobile.google.reader.Resource;

public class ResourceJsonAdapter extends AbstractResourceJsonAdapter<Resource> {

	@Override
	public Resource create() {
		return new Resource();
	}

}
