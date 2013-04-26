package com.gettingmobile.goodnews.download;

public enum OfflineContentType {
    NONE(false, false),
    TEXT(true, false),
    IMAGES(false, true),
    TEXT_IMAGES(true, true);

    private final boolean wantsText;
    private final boolean wantsImages;
    private OfflineContentType(boolean wantsText, boolean wantsImages) {
        this.wantsText = wantsText;
        this.wantsImages = wantsImages;
    }

    public boolean wantsText() {
        return wantsText;
    }

    public boolean wantsImages() {
        return wantsImages;
    }
}
