package com.app.afridge.utils;


import android.text.format.DateUtils;


public class TimeSpans {

    private TimeSpans() {

    }

    public static String getRelativeTimeSince(long from, long to) {

        String span = DateUtils.getRelativeTimeSpanString(from, to,
                0, DateUtils.FORMAT_ABBREV_RELATIVE)
                .toString();
        // Cheap hack.
        return span.replace(" hours", "h")
                .replace(" minutes", "m")
                .replace(" seconds", "s")
                .replace(" days", "d");
    }
}
