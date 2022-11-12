package com.yatenesturno.functionality.days_off;

import com.yatenesturno.utils.CalendarUtils;

import java.util.Calendar;
import java.util.Objects;

public class DayOff {

    private String date;
    private Calendar parsedDate;

    public Calendar getDate() {
        if (parsedDate == null) {
            parsedDate = CalendarUtils.parseDate(date);
        }
        return parsedDate;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setParsedDate(Calendar date) {
        this.parsedDate = date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DayOff)) return false;
        DayOff dayOff = (DayOff) o;
        return compareWithDate(dayOff.getDate());
    }

    @Override
    public int hashCode() {
        return Objects.hash(date);
    }

    private boolean compareWithDate(Calendar otherDate) {
        Calendar thisDate = getDate();
        return Objects.equals(thisDate.get(Calendar.DAY_OF_MONTH), otherDate.get(Calendar.DAY_OF_MONTH)) &&
                Objects.equals(thisDate.get(Calendar.MONTH), otherDate.get(Calendar.MONTH)) &&
                Objects.equals(thisDate.get(Calendar.YEAR), otherDate.get(Calendar.YEAR));
    }

    public boolean hasDate(Calendar date) {
        return compareWithDate(date);
    }
}
