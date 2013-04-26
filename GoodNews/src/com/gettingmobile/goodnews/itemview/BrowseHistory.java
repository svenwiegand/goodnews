package com.gettingmobile.goodnews.itemview;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

final class BrowseHistory {
    private static final String LOG_TAG = "goodnews.BrowseHistory";
    private final List<BrowseHistoryEntry> history = new ArrayList<BrowseHistoryEntry>();
    private BrowseHistoryListener listener;
    
    public void setListener(BrowseHistoryListener listener) {
        this.listener = listener;
    }

    public void clear() {
        if (!history.isEmpty()) {
            history.clear();
            fireChanged();
        }
    }

    public void setCurrent(BrowseHistoryEntry current) {
        Log.d(LOG_TAG, "Adding browse history entry " + current);
        final BrowseHistoryEntry newest = getCurrent();
        if (newest == null || !newest.equals(current)) {
            history.add(current);
            fireChanged();
        }
    }

    public BrowseHistoryEntry getCurrent() {
        return !history.isEmpty() ? history.get(history.size() - 1) : null;
    }

    public boolean isEmpty() {
        return history.isEmpty();
    }

    public boolean canGoBack() {
        return history.size() > 1;
    }

    public BrowseHistoryEntry goBack() {
        if (!canGoBack())
            throw new IllegalStateException("No more history entries");

        history.remove(history.size() - 1);
        fireChanged();
        return getCurrent();
    }
            
    private void fireChanged() {
        if (listener != null) {
            listener.onBrowseHistoryChanged(this);
        }
    }
}
