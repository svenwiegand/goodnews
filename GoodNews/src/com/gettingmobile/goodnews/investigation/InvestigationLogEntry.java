package com.gettingmobile.goodnews.investigation;

import com.gettingmobile.google.reader.db.Table;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

final class InvestigationLogEntry {
    private static DateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private long id = Table.INVALID_ID;
    private String tag = "";
    private long timestamp = 0;
    private String message = "";

    public InvestigationLogEntry(String tag, String message) {
        this.tag = tag;
        this.timestamp = System.currentTimeMillis();
        this.message = message;
    }

    public InvestigationLogEntry() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getFormattedTimestamp() {
        return TIMESTAMP_FORMAT.format(timestamp);
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return getFormattedTimestamp() + ": " + tag + ": " + message;
    }
}
