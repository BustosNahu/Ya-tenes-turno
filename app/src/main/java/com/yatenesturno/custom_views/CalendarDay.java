package com.yatenesturno.custom_views;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yatenesturno.R;

import java.util.Calendar;

public class CalendarDay {

    protected Calendar calendar;


    public CalendarDay(Calendar calendar) {
        this.calendar = calendar;
    }

    public Calendar getCalendar() {
        return calendar;
    }

    public void populateView(ViewGroup view, LayoutInflater inflater) {
        TextView textViewDayNumber = view.findViewById(R.id.textViewDayNumber);
        textViewDayNumber.setText(calendar.get(Calendar.DATE) + "");
    }

}
