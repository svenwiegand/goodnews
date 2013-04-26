package com.gettingmobile.goodnews;

public class SimpleActivityAccessor<T extends android.app.Activity> implements ActivityAccessor<T> {
	private T activity = null;

	public SimpleActivityAccessor(T activity) {
		this.activity = activity;
	}
	
	@Override
	public T getActivity() {
		return activity;
	}
	
	public void setActivity(T activity) {
		this.activity = activity;
	}
}
