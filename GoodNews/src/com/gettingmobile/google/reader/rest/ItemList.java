package com.gettingmobile.google.reader.rest;

import com.gettingmobile.google.reader.Item;

import java.util.ArrayList;

public class ItemList extends ArrayList<Item> {
	private static final long serialVersionUID = 1L;
	
	private String continuationToken = null;
    private long updatedTimeStamp = 0;

	public ItemList() {
	}
	
	public boolean hasContinuation() {
		return continuationToken != null;
	}
	
	public String getContinuationToken() {
		return continuationToken;
	}
	
	public void setContinuationToken(String continuationToken) {
		this.continuationToken = continuationToken;
	}

    public long getUpdatedTimeStamp() {
        return updatedTimeStamp;
    }

    public void setUpdatedTimeStamp(long updatedTimeStamp) {
        this.updatedTimeStamp = updatedTimeStamp;
    }
}
