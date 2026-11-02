package com.example.taskflow;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

// Small helper for dates. "Today" = the device's current local date.
public class DateUtils {

    // Millis at 00:00 of today (local time)
    public static long todayStart() {
        return dayStart(System.currentTimeMillis());
    }

    // Millis at 00:00 of the given day
    public static long dayStart(long millis) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(millis);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    // Millis at 00:00 of the given year / month (0-11) / day
    public static long dateOf(int year, int month, int day) {
        Calendar c = Calendar.getInstance();
        c.clear();
        c.set(year, month, day);
        return c.getTimeInMillis();
    }

    // Today plus / minus some days (used for sample data)
    public static long daysFromToday(int days) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(todayStart());
        c.add(Calendar.DAY_OF_MONTH, days);
        return c.getTimeInMillis();
    }

    public static boolean isToday(long millis) {
        return dayStart(millis) == todayStart();
    }

    // e.g. "18 May 2022"
    public static String format(long millis) {
        SimpleDateFormat sdf = new SimpleDateFormat("d MMM yyyy", Locale.getDefault());
        return sdf.format(new Date(millis));
    }
}
