package com.gettingmobile.goodnews.download;

interface ContentDownloadListener {
    void onDownloadStarted();
    void onDownloadProgressUpdate(int progress, int max);
    void onDownloadStopped();
    void onDownloadSkipped();
}
