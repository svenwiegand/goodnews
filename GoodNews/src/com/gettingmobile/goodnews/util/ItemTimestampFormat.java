package com.gettingmobile.goodnews.util;

import android.content.Context;
import com.gettingmobile.goodnews.R;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * These are the supported place holders:
 */
public final class ItemTimestampFormat {
    private final DateFormat dateFormat;
    private final DateFormat timeFormat;
    private final MessageFormat formatToday;
    private final MessageFormat formatWeek;
    private final MessageFormat formatYear;
    private final MessageFormat formatPast;

    private static MessageFormat createFormat(Context context, boolean full, int resIdShort, int resIdLong) {
        return new MessageFormat(context.getString(full ? resIdLong : resIdShort));
    }

    public ItemTimestampFormat(Context context, boolean full) {
        dateFormat = android.text.format.DateFormat.getDateFormat(context);
        timeFormat = android.text.format.DateFormat.getTimeFormat(context);
        formatToday = createFormat(context, full, R.string.format_date_today, R.string.format_date_today);
        formatWeek = createFormat(context, full, R.string.format_date_week_short, R.string.format_date_week_long);
        formatYear = createFormat(context, full, R.string.format_date_year_short, R.string.format_date_year_long);
        formatPast = createFormat(context, full, R.string.format_date_past_short, R.string.format_date_past_long);
    }

    public String format(Date timestamp) {
        if (timestamp == null)
            return "";

        final Object[] args = {timestamp, dateFormat.format(timestamp), timeFormat.format(timestamp)};
        final Calendar border = Calendar.getInstance();

        /*
         * check whether this is a timestamp of today
         */
        border.set(Calendar.HOUR_OF_DAY, 0);
        border.set(Calendar.MINUTE, 0);
        border.set(Calendar.SECOND, 0);
        border.set(Calendar.MILLISECOND, 0);
        if (timestamp.getTime() >= border.getTimeInMillis()) {
            return formatToday.format(args);
        }

        /*
         * check whether this is a timestamp of the last seven days
         */
        border.add(Calendar.DAY_OF_MONTH, -6);
        if (timestamp.getTime() >= border.getTimeInMillis()) {
            return formatWeek.format(args);
        }

        /*
         * check whether the timestamp isn't older than a year
         */
        border.add(Calendar.MONTH, -10);
        if (timestamp.getTime() >= border.getTimeInMillis()) {
            return formatYear.format(args);
        } else {
            return formatPast.format(args);
        }
    }
}
