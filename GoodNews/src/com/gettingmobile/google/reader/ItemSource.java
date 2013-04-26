package com.gettingmobile.google.reader;

public enum ItemSource {
	POST("post");

	private final ElementId id;
	private ItemSource(String id) {
		this.id = new ElementId("user/-/source/com.google/" + id);
	}
	
	public ElementId getId() {
		return id;
	}
	
	public String getIdText() {
		return id.getId();
	}
}
