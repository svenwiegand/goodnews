package com.gettingmobile.goodnews.download;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.gettingmobile.goodnews.Application;
import com.gettingmobile.google.reader.Item;
import com.gettingmobile.google.reader.Resource;

import java.util.*;

final class ContentDownloadThread extends Thread {
    private static final String LOG_TAG = "goodnews.ContentDownloadThread";
    private final Application app;
    private final ContentDownloadListener listener;
    private final MainThreadHandler mainThreadHandler;
    private ItemDownloader downloader = null;

    public ContentDownloadThread(Application app, ContentDownloadListener listener) {
        super(ContentDownloadThread.class.getName());
        this.app = app;
        this.listener = listener;
        mainThreadHandler = new MainThreadHandler();
    }

    /*
     * listener handling
     */

    protected void fireDownloadStarted() {
        listener.onDownloadStarted();
    }

    protected void fireDownloadProgressUpdate(int progress, int max) {
        listener.onDownloadProgressUpdate(progress, max);
    }

    protected void fireDownloadStopped() {
        listener.onDownloadStopped();
    }

    protected void fireDownloadSkipped() {
        listener.onDownloadSkipped();
    }

    /*
     * operations
     */

    public void shutdown() {
        if (downloader != null) {
            downloader.shutdown();
        }
    }

    /*
     * doing
     */

    protected Queue<Item> readRelevantItems(Set<Long> processedItemKeys) {
        final boolean readListOnly = app.getSettings().getOfflineStrategy() == OfflineStrategy.READ_LIST;
        final List<Item> items = readListOnly ?
                ItemDownloader.itemDownloadAdapter.readItemDownloadInfosRequiringDownloads(
                        app.getDbHelper().getDatabase(), app.getSettings().getLabelReadListId()) :
                ItemDownloader.itemDownloadAdapter.readItemDownloadInfosRequiringDownloads(
                        app.getDbHelper().getDatabase());
        final Queue<Item> q = new LinkedList<Item>();
        for (Item item : items) {
            /*
             * deciding which items need to be processed
             */
            if (!processedItemKeys.contains(item.getKey())) {
                final OfflineContentType offlineContentType = app.getSettings().getOfflineContentType(item.getFeedId());
                if (offlineContentType != OfflineContentType.NONE) {
                    final Resource alternate = item.getAlternate();
                    final boolean hasValidAlternate = alternate != null && alternate.getHref() != null && alternate.getHref().length() > 0;
                    final boolean wantsContent = offlineContentType.wantsText();
                    final boolean wantsImages = offlineContentType.wantsImages();
                    final boolean hasImages = item.hasImages();
                    final boolean hasSummary = item.hasSummary();
                    final boolean hasContent = item.hasContent();
                    
                    if ((wantsContent && !hasContent && hasValidAlternate) /* requires content */ ||
                            (wantsImages && !hasImages && (hasSummary || hasContent || hasValidAlternate)) /* requires images */) {
                        q.add(item);
                        processedItemKeys.add(item.getKey());
                    }
                }
            }
        }
        return q;
    }

    @Override
    public void run() {
        /*
         * prepare the item queue and cancel if there is nothing to be downloaded
         */
        final Set<Long> processedItemKeys = new HashSet<Long>();
        Queue<Item> q = readRelevantItems(processedItemKeys);
        if (q.isEmpty()) {
            mainThreadHandler.sendDownloadSkipped();
            return;
        }

        mainThreadHandler.sendDownloadStarted();
        try {
            while (!q.isEmpty()) {
                final DownloadProgress progress = new ContentDownloadProgress(q.size());
                downloader = new ItemDownloader(app, q, progress);
                downloader.run();
                q = readRelevantItems(processedItemKeys);
            }
        } finally {
            mainThreadHandler.sendDownloadStopped();
        }
    }

    /*
     * inner classes
     */

    class MainThreadHandler extends Handler {
        private static final int MSG_DOWNLOAD_STARTED = 0;
        private static final int MSG_DOWNLOAD_PROGRESS_UPDATE = 1;
        private static final int MSG_DOWNLOAD_STOPPED = 2;
        private static final int MSG_DOWNLOAD_SKIPPED = 3;

        public void sendDownloadStarted() {
            sendMessage(obtainMessage(MSG_DOWNLOAD_STARTED));
        }

        public void sendDownloadProgressUpdate(int progress, int max) {
            sendMessage(obtainMessage(MSG_DOWNLOAD_PROGRESS_UPDATE, progress, max));
        }

        public void sendDownloadStopped() {
            sendMessage(obtainMessage(MSG_DOWNLOAD_STOPPED));
        }

        public void sendDownloadSkipped() {
            sendMessage(obtainMessage(MSG_DOWNLOAD_SKIPPED));
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_DOWNLOAD_STARTED:
                    fireDownloadStarted();
                    break;
                case MSG_DOWNLOAD_PROGRESS_UPDATE:
                    fireDownloadProgressUpdate(msg.arg1, msg.arg2);
                    break;
                case MSG_DOWNLOAD_STOPPED:
                    fireDownloadStopped();
                    break;
                case MSG_DOWNLOAD_SKIPPED:
                    fireDownloadSkipped();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    class ContentDownloadProgress implements DownloadProgress {
        private static final int MIN_UPDATE_DELAY = 1000;
        private final int max;
        private int progress = 0;
        private long latestUpdateTimestamp = 0;

        public ContentDownloadProgress(int max) {
            this.max = max;
        }

        @Override
        synchronized public void increase() {
            ++progress;

            /*
             * prevent us to flood the system with too many update messages
             */
            final long timestamp = System.currentTimeMillis();
            if (timestamp - latestUpdateTimestamp > MIN_UPDATE_DELAY) {
                Log.d(LOG_TAG, "Content download progres: " + 100 * progress / max + "%");
                mainThreadHandler.sendDownloadProgressUpdate(progress, max);
                latestUpdateTimestamp = timestamp;
            }
        }
    }
}
