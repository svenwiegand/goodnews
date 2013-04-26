package com.gettingmobile.android.app;

import android.app.Service;
import android.os.Binder;

public class ServiceBinder<T extends Service> extends Binder {
    private final T service;

    public ServiceBinder(T service) {
        this.service = service;
    }

    T getService() {
        return service;
    }

}
