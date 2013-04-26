/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.gettingmobile.rest;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;

/**
 *
 * @author sven.wiegand
 */
public final class RequestHandler {	
    private final RequestHandlerThread thread;
    private final RequestProcessor processor;
    private final Handler callbackHandler;

    public RequestHandler() {
        processor = new RequestProcessor();
        callbackHandler = new Handler();
        thread = new RequestHandlerThread();
    }
    
    public void start() {
    	synchronized (thread.initEvent) {
    		thread.start();
    		try {
    			thread.initEvent.wait();
    		} catch (InterruptedException ex) {
    			Log.e(getClass().getName(), "Failed while waiting for handler thread to start", ex);
    		}
    	}
    }

    public void shutdown() {
        thread.shutdown();
    }

    public <R extends Request<T>, T> void send(R request, RequestCallback<R, T> callback) 
    		throws InterruptedException {
    	thread.getHandler().post(new AsynchronousRequest<R, T>(request, callback));
    }
    
    public void send(Request<?> request) throws InterruptedException {
    	send(request, null);
    }
    
    final class RequestHandlerThread extends HandlerThread {
    	public final Object initEvent = new Object();
    	private Handler handler;
    	
    	public RequestHandlerThread() {
    		super(RequestHandler.class.getName());
    	}
    	
    	public void shutdown() {
    		final Looper l = getLooper();
    		if (l != null) {
    			l.quit();
    		}
    	}

		@Override
		protected void onLooperPrepared() {
			synchronized (initEvent) {
				handler = new Handler();
				initEvent.notifyAll();
			}
			super.onLooperPrepared();
		}
		
		public Handler getHandler() {
			return handler;
		}
    }
    
	final class AsynchronousRequest<R extends Request<T>, T> implements Runnable {
		public final R request;
		public final RequestCallback<R, T> callback;
		
		public AsynchronousRequest(R request, RequestCallback<R, T> callback) {
			this.request = request;
			this.callback = callback;
		}

		@Override
		public void run() {
			try {
				final T result = processor.requestResult(request);
				callbackHandler.post(new CallbackEvent<R, T>(this, result, null));
			} catch (Exception ex) {
				callbackHandler.post(new CallbackEvent<R, T>(this, null, ex));				
			}
		}
	}
	
	static final class CallbackEvent<R extends Request<T>, T> implements Runnable {
		public final AsynchronousRequest<R, T> request;
		public final T result;
		public final Throwable error;
		
		public CallbackEvent(AsynchronousRequest<R, T> request, T result, Throwable error) {
			this.request = request;
			this.result = result;
			this.error = error;
		}
		
		@Override
		public void run() {
			request.callback.onRequestProcessed(request.request, result, error);
		}
	}
}
