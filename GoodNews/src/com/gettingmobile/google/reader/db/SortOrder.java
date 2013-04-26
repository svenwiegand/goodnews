package com.gettingmobile.google.reader.db;

public enum SortOrder {
	ASCENDING("ASC"),
	DESCENDING("DESC");
	
	private final String sql;
	
	private SortOrder(String sql) {
		this.sql = " " + sql;
	}
	
	public String getSql() {
		return sql;
	}
}
