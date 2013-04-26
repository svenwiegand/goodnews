package com.gettingmobile.goodnews.locale;

public final class Constants {
    public static final int ACTION_SYNC = 0;

    public static final int SYNC_TYPE_FULL = 0;
    public static final int SYNC_TYPE_PUSH = 1;

    private static final String INTENT_BASE = "com.gettingmobile.goodnews.";
    public static final String INTENT_EXTRA_ACTION = INTENT_BASE + ".ACTION";
    public static final String INTENT_EXTRA_SYNC_TYPE = INTENT_BASE + ".SYNC_TYPE";

    private Constants() {
        throw new UnsupportedOperationException("This class is not meant to be instantiated.");
    }
}
