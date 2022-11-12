package com.yatenesturno.custom_views;

import android.view.ViewGroup;

import com.yatenesturno.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MonthPicker extends NumberCounter {
    private static final List<String> monthsList = new ArrayList<String>() {{
        add("Enero");
        add("Febrero");
        add("Marzo");
        add("Abril");
        add("Mayo");
        add("Junio");
        add("Julio");
        add("Agosto");
        add("Septiembre");
        add("Octubre");
        add("Noviembre");
        add("Diciembre");
    }};
    private boolean pastDays = true;
    private Calendar currMonth;

    public MonthPicker(ViewGroup parent) {
        super(parent);
    }

    @Override
    protected void init() {
        this.currMonth = Calendar.getInstance();
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.month_picker_layout;
    }

    @Override
    protected void increaseCounter() {
        currMonth.add(Calendar.MONTH, 1);
    }

    @Override
    protected void decreaseCounter() {
        currMonth.add(Calendar.MONTH, -1);
    }

    @Override
    protected boolean isAtBottomLimit() {
        if (pastDays) {
            return false;
        } else {
            return compareCalendars(currMonth, Calendar.getInstance()) <= 0;
        }
    }

    public int compareCalendars(Calendar c1, Calendar c2) {
        if (c1.get(Calendar.YEAR) != c2.get(Calendar.YEAR))
            return c1.get(Calendar.YEAR) - c2.get(Calendar.YEAR);

        return c1.get(Calendar.MONTH) - c2.get(Calendar.MONTH);
    }

    @Override
    protected boolean isAtTopLimit() {
        return false;
    }

    public void enablePastDays() {
        pastDays = true;
    }

    public void disablePastDays() {
        pastDays = false;
    }

    @Override
    protected String getDisplayableText() {
        return currMonth.get(Calendar.YEAR) + "\n" + monthsList.get(currMonth.get(Calendar.MONTH));
    }

    public Calendar getCurrMonth() {
        return currMonth;
    }

    public void setCurrMonth(Calendar currMonth) {
        this.currMonth = currMonth;
        updateUI();
    }
}
