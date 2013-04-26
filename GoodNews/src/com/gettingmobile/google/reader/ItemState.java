package com.gettingmobile.google.reader;

public enum ItemState {
	READ("read"),
	KEPT_UNREAD("kept-unread"),
	TRACKING_KEPT_UNREAD("tracking-kept-unread"),
	STARRED("starred"),
	READING_LIST("reading-list"),
    ROOT("root");

	private final ElementId id;
	private ItemState(String id) {
		this.id = new ElementId("user/-/state/com.google/" + id);
	}
	
	public ElementId getId() {
		return id;
	}
	
	public String getIdText() {
		return id.getId();
	}
}
