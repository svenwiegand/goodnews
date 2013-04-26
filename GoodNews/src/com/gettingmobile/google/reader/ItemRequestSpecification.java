package com.gettingmobile.google.reader;

import java.util.Calendar;

public final class ItemRequestSpecification {
    private ElementId streamId;
    private int maxAgeInDays;
    private int maxItemCount;
    
    public ItemRequestSpecification() {
        this(null, 0, 0);
    }
    
    public ItemRequestSpecification(ElementId streamId, int maxAgeInDays, int maxItemCount) {
        this.streamId = streamId;
        this.maxAgeInDays = maxAgeInDays;
        this.maxItemCount = maxItemCount;
    }

    public ElementId getStreamId() {
        return streamId;
    }

    public void setStreamId(ElementId streamId) {
        this.streamId = streamId;
    }
    
    public long getStartTime() {
        final Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_YEAR, -1 * maxAgeInDays);
        return c.getTimeInMillis();
    }

    public int getMaxAgeInDays() {
        return maxAgeInDays;
    }

    public void setMaxAgeInDays(int maxAgeInDays) {
        this.maxAgeInDays = maxAgeInDays;
    }

    public int getMaxItemCount() {
        return maxItemCount;
    }

    public void setMaxItemCount(int maxItemCount) {
        this.maxItemCount = maxItemCount;
    }
}
