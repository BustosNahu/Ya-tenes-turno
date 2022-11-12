package com.yatenesturno.custom_views;

import android.util.Log;
import android.view.ViewGroup;

import java.util.Calendar;

public class TimePicker extends NumberCounter {

    private int MAX_HOURS = 2;
    private int MINUTE_STEP = 15;
    private Calendar calendar;
    private boolean showMinutes = true;

    public TimePicker(ViewGroup parent) {
        super(parent);
    }

    @Override
    protected void init() {
        calendar = Calendar.getInstance();
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
    }

    @Override
    protected void increaseCounter() {
        Log.e("asdasd", "increased");
        calendar.add(Calendar.MINUTE, MINUTE_STEP);
    }

    @Override
    protected void decreaseCounter() {
        Log.e("asdasd", "decreased");
        calendar.add(Calendar.MINUTE, -MINUTE_STEP);
    }

    @Override
    protected boolean isAtBottomLimit() {
        return calendar.get(Calendar.HOUR_OF_DAY) == 0 && calendar.get(Calendar.MINUTE) == 0;
    }

    public void setShowMinutes(boolean show) {
        this.showMinutes = show;
    }

    public void setStep(int minutes) {
        this.MINUTE_STEP = minutes;
    }

    @Override
    protected boolean isAtTopLimit() {
        return calendar.get(Calendar.HOUR_OF_DAY) == MAX_HOURS;
    }

    public void setMax(int hours) {
        MAX_HOURS = hours;
    }

    @Override
    protected String getDisplayableText() {
        if (showMinutes) {
            return calendar.get(Calendar.HOUR_OF_DAY) + "h " + calendar.get(Calendar.MINUTE) + "m";
        }
        return calendar.get(Calendar.HOUR_OF_DAY) + "h";
    }

    public Calendar getTime() {
        return calendar;
    }

    public void setTime(Calendar calendar) {
        if (calendar != null) {
            this.calendar = (Calendar) calendar.clone();
        }
        updateUI();
    }
}
