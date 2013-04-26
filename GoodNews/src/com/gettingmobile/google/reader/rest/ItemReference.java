package com.gettingmobile.google.reader.rest;

import com.gettingmobile.google.reader.ElementId;

import java.util.Calendar;
import java.util.List;

public class ItemReference {
    private long id = 0;
    private List<ElementId> directStreamIds = null;
    private long timestampUSec = 0;

    public static long getTimestampUSecByDate(Calendar date) {
        return date.getTimeInMillis() * 1000;
    }

    public static Calendar getDateByTimestampUSec(long timestampUSec) {
        final Calendar c = Calendar.getInstance();
        c.setTimeInMillis(timestampUSec / 1000);
        return c;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public List<ElementId> getDirectStreamIds() {
        return directStreamIds;
    }

    public void setDirectStreamIds(List<ElementId> directStreamIds) {
        this.directStreamIds = directStreamIds;
    }

    public long getTimestampUSec() {
        return timestampUSec;
    }

    public void setTimestampUSec(long timestampUSec) {
        this.timestampUSec = timestampUSec;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(
                "ItemReference[id=" + id + ";timestampUsec=" + timestampUSec + ";directStreamIds=[");
        for (ElementId streamId : directStreamIds) {
            sb.append(streamId.toString()).append(';');
        }
        sb.append("]]");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ItemReference that = (ItemReference) o;

        if (id != that.id) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }
}
