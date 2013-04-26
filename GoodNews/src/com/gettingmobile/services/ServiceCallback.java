package com.gettingmobile.services;

public interface ServiceCallback<T> {
	void onFinished(T result, Throwable error);
}
