package com.gettingmobile.google.reader;


public class UnreadCount {
	private ElementId elementId = null;
	private int count = 0;
	long newestItemTimestamp = 0;
	
	public UnreadCount() {
	}
	
	public ElementId getElementId() {
		return elementId;
	}
	
	public void setElementId(ElementId elementId) {
		this.elementId = elementId;
	}
	
	public int getCount() {
		return count;
	}
	
	public void setCount(int count) {
		this.count = count;
	}
	
	public long getNewestItemTimestamp() {
		return newestItemTimestamp;
	}
	
	public void setNewestItemTimestamp(long newestItemTimestamp) {
		this.newestItemTimestamp = newestItemTimestamp;
	}
}
