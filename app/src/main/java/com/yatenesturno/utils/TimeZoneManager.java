package com.yatenesturno.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class TimeZoneManager {

    public static Calendar toUTC(Calendar calendar) {
        Calendar calendarAux = Calendar.getInstance(TimeZone.getTimeZone("GMT"),
                Locale.getDefault());

        Date currentLocalTime = calendarAux.getTime();
        DateFormat dateFormat = new SimpleDateFormat("Z");
        String localOffset = dateFormat.format(currentLocalTime);

        Calendar out = (Calendar) calendar.clone();
        int offset = -Integer.parseInt(localOffset);

        int hours = offset / 100;
        int minutes = offset % 100;
        offset = hours * 60 + minutes;

        out.add(Calendar.MINUTE, offset);
        return out;
    }

    public static Calendar fromUTC(Calendar calendar) {
        Calendar calendarAux = Calendar.getInstance(TimeZone.getTimeZone("GMT"),
                Locale.getDefault());

        Date currentLocalTime = calendarAux.getTime();
        DateFormat dateFormat = new SimpleDateFormat("Z");
        String localOffset = dateFormat.format(currentLocalTime);

        Calendar out = (Calendar) calendar.clone();
        int offset = Integer.parseInt(localOffset);

        int hours = offset / 100;
        int minutes = offset % 100;
        offset = hours * 60 + minutes;

        out.add(Calendar.MINUTE, offset);
        return out;
    }

}
