package com.gettingmobile.google.reader.sync;

import com.gettingmobile.ApplicationException;
import com.gettingmobile.google.Authenticator;
import com.gettingmobile.google.reader.db.DatabaseHelper;
import com.gettingmobile.rest.Request;
import com.gettingmobile.rest.RequestCallback;
import com.gettingmobile.rest.RequestProcessor;

import java.io.IOException;
import java.net.URISyntaxException;

public abstract class AbstractSynchronizer {
    public static final String LOG_TAG = "goodnews.Synchronizer";
    protected final SyncContext context;
	protected final SyncSettings settings;
	protected final Authenticator authenticator;
	protected RequestProcessor requestProcessor;

	private boolean syncing = false;
    private boolean cancelled = false;

	public AbstractSynchronizer(SyncContext context) {
        this.context = context;
		this.settings = context.getSettings();
		this.authenticator = context.getAuthenticator();
	}

    public void setRequestProcessor(RequestProcessor requestProcessor) {
        this.requestProcessor = requestProcessor;
    }

    protected DatabaseHelper getDbHelper() {
        return context.getDbHelper();
    }

	public boolean isSyncing() {
		return syncing;
	}

    public synchronized void cancel() {
        if (isSyncing()) {
            this.cancelled = true;
        }
    }

    public synchronized void resetCancelled() {
        this.cancelled = false;
    }

    protected boolean isCancelled() {
        return cancelled;
    }

    protected void throwCancelled() throws SyncException {
        resetCancelled();
        throw new SyncException(SyncException.ErrorCode.CANCELLED);
    }

    protected void throwCancelledIfApplicable() throws SyncException {
        if (isCancelled())
            throwCancelled();
    }

    public abstract int forecastMaxProgress();

	public void sync(RequestProcessor requestProcessor, SyncCallbackHelper callback) throws SyncException {
		if (!syncing) {
            syncing = true;
            this.requestProcessor = requestProcessor;
            try {
			    doSync(callback);
            } catch (URISyntaxException ex) {
                throw new SyncException(ex);
            } finally {
                this.requestProcessor = null;
                syncing = false;
            }
		}
	}

	protected abstract void doSync(SyncCallbackHelper callback) throws URISyntaxException, SyncException;

	protected <R extends Request<T>, T> T sendRequest(R request, RequestCallback<R, T> callback) throws SyncException {
		try {
			final T result = requestProcessor.requestResult(request);
            if (callback != null) {
			    callback.onRequestProcessed(request, result, null);
            }
			return result;
        } catch (IOException ex) {
            throw new SyncException(SyncException.ErrorCode.CONNECTION, ex);
		} catch (ApplicationException ex) {
            throw new SyncException(ex);
		}
	}

    protected <R extends Request<T>, T> T sendRequest(R request) throws SyncException {
        return sendRequest(request, null);
    }
}
