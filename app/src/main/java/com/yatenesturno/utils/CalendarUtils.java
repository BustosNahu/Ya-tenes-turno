package com.yatenesturno.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class CalendarUtils {

    public static Calendar parseDateTime(String dateTimeString) {
        if (dateTimeString == null) {
            return null;
        }
        try {
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateTimeFormat;
            if (dateTimeString.contains("T")) {
                dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);
            } else {
                dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
            }
            calendar.setTime(dateTimeFormat.parse(dateTimeString));
            return calendar;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Calendar parseDate(String dateString) {
        if (dateString == null) {
            return null;
        }
        try {
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateTimeFormat;

            dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

            calendar.setTime(dateTimeFormat.parse(dateString));
            return calendar;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String formatCalendar(Calendar calendar) {
        if (calendar == null) {
            return null;
        }
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);
        return dateTimeFormat.format(calendar.getTime());
    }

    public static String formatDateYYYYMMDD(Calendar calendar) {
        if (calendar == null) {
            return null;
        }
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        return dateTimeFormat.format(calendar.getTime());
    }

    public static String formatDate(Calendar calendar) {
        if (calendar == null) {
            return null;
        }
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
        return dateTimeFormat.format(calendar.getTime());
    }

    public static String formatTime(Calendar calendar) {
        if (calendar == null) {
            return null;
        }
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
        return dateTimeFormat.format(calendar.getTime());
    }

}
