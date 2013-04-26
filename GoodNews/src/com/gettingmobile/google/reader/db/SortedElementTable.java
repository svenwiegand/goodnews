package com.gettingmobile.google.reader.db;

import java.util.Map;

public abstract class SortedElementTable extends ElementTable {
	public static final String SORT_ID = "sortId";

	public SortedElementTable(String tableName) {
		super(tableName);
	}
	
	@Override
	protected void defineColumns(Map<String, String> columns) {
		super.defineColumns(columns);
		columns.put(SORT_ID, "TEXT");
	}
}
