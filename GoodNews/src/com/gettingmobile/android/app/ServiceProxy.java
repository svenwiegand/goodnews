package com.gettingmobile.android.app;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ServiceProxy<T extends Service, L extends ServiceListener> {
    private final Class<T> serviceClass;
    private final ServiceConnection connection = new ServiceConnection();
    private final List<Operation<T>> startupOperations = new ArrayList<Operation<T>>();
    private T service = null;
    private Set<L> listeners = new HashSet<L>();

    public ServiceProxy(Class<T> serviceClass) {
        this.serviceClass = serviceClass;
    }

    protected Intent createIntent(Context context) {
        return new Intent(context, serviceClass);
    }

    public boolean isStarted() {
        return service != null;
    }

    public void startService(Context context) {
        final Intent intent = createIntent(context);
        context.bindService(intent, connection, Service.BIND_AUTO_CREATE);
    }

    public void stopService(Context context) {
        if (service != null) {
            context.stopService(createIntent(context));
            context.unbindService(connection);
        }
    }

    public T getService() {
        return service;
    }

    /**
     * Executes the specified operation on the service, if the service is already up and running or schedules the
     * operation for execution on service startup.
     * @param operation the operation to be executed with the service.
     * @return {@code true} if the service is up and running and the operation has been executed, {@code false} if the
     * operation has been scheduled for execution on service startup.
     */
    public boolean runOnService(Operation<T> operation) {
        if (service != null) {
            operation.run(service);
            return true;
        } else {
            startupOperations.add(operation);
            return false;
        }
    }

    protected void onStartup() {
        for (Operation<T> op : startupOperations) {
            op.run(service);
        }
    }
    
    /*
     * listener handling
     */

    public boolean addListener(L listener) {
        return listeners.add(listener);
    }

    public boolean removeListener(L listener) {
        return listeners.remove(listener);
    }    
    
    public Set<L> getListeners() {
        return listeners;
    }

    /*
     * inner classes
     */

    public static interface Operation<T> {
        void run(T service);
    }

    class ServiceConnection implements android.content.ServiceConnection {
        @SuppressWarnings("unchecked")
        @Override
        public void onServiceConnected(ComponentName className, IBinder serviceBinder) {
            boolean unexpectedServiceBinder = true;
            if (serviceBinder != null && serviceBinder instanceof ServiceBinder<?>) {
                final Service svc = ((ServiceBinder) serviceBinder).getService();
                if (serviceClass.isAssignableFrom(svc.getClass())) {
                    service = (T) svc;
                    onStartup();
                    unexpectedServiceBinder = false;
                }
            }

            if (unexpectedServiceBinder) {
                /*
                 * this should occur only in the context of the CrashReport as this is running in another process
                 */
                service = null;
                Log.w(getClass().getName(), "Failed to gather service interface");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            service = null;
        }
    }
}
