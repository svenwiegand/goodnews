package com.gettingmobile.google.reader.sync;

class SimpleSyncCallbackHelper implements SyncCallbackHelper {
    private static final int MIN_PROGRESS_UPDATE_INTERVAL_NANOS = 500 * 1000 * 1000;
	private final CallbackHandler callbackHandler;
	private final SyncCallback callback;
	private int maxProgress = 0;
	private int progress = 0;
    private int unreadCount = 0;
    private int newUnreadCount = 0;
    private int skipCount = 0;
    private long lastProgressUpdateNanoTime = 0;
	
	public SimpleSyncCallbackHelper(CallbackHandler callbackHandler, SyncCallback callback) {
		this.callbackHandler = callbackHandler;
		this.callback = callback;
	}

	@Override
	public void setMaxProgress(int maxProgress) {
		this.maxProgress = maxProgress;
		sendProgressUpdate();
	}
	
	@Override
	public void addMaxProgress(int additionalMaxProgress) {
		maxProgress += additionalMaxProgress;
		sendProgressUpdate();
	}

    @Override
    public void incrementProgress() {
        incrementProgress(1);
    }

	@Override
	public void incrementProgress(int steps) {
		progress += steps;
		sendProgressUpdate();
	}

    @Override
    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    @Override
    public void setNewUnreadCount(int newUnreadCount) {
        this.newUnreadCount = newUnreadCount;
    }

    @Override
    public void incrementSkipCount() {
        ++skipCount;
    }

    @Override
	public void onSyncFinished(SyncException error) {
		callbackHandler.sendSyncFinished(callback, error, unreadCount, newUnreadCount, skipCount);
	}
	
	protected void sendProgressUpdate() {
        final long nanoTime = System.nanoTime();
        if (nanoTime - lastProgressUpdateNanoTime > MIN_PROGRESS_UPDATE_INTERVAL_NANOS || progress >= maxProgress) {
		    callbackHandler.sendProgressUpdate(callback, progress, maxProgress);
            lastProgressUpdateNanoTime = nanoTime;
        }
	}
}
