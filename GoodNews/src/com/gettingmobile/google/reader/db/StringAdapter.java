package com.gettingmobile.google.reader.db;

public interface StringAdapter<T> {
	String write(T o);
	T read(String string);
}
