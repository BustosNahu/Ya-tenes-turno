package com.yatenesturno.activities.appointment_view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.yatenesturno.R;
import com.yatenesturno.object_interfaces.Appointment;
import com.yatenesturno.objects.AppointmentClassImpl;
import com.yatenesturno.view_builder.DailyCalendarView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Displays appointments in a daily grid
 */
public class DailyCalendar {

    /**
     * UI Refs
     */
    private final ViewGroup root;
    private final RelativeLayout appointmentsContainer;
    private final LinearLayout timesContainer;
    private final LinearLayout timeSeparator;
    private final View rootView;
    private final RelativeLayout nowIndicatorContainer;

    /**
     * Instance variables
     */
    private final OnAppointmentClickListener listener;
    private List<Appointment> appointmentList;
    private ArrayList<DailyCalendarView> appointmentViewList;

    public DailyCalendar(ViewGroup root, OnAppointmentClickListener listener) {
        this.root = root;
        this.listener = listener;

        rootView = LayoutInflater.from(root.getContext()).inflate(R.layout.daily_calendar_layout, root, true);

        appointmentsContainer = rootView.findViewById(R.id.appointmentsContainer);
        timesContainer = rootView.findViewById(R.id.timesContainer);
        timeSeparator = rootView.findViewById(R.id.timeLineContainer);
        nowIndicatorContainer = rootView.findViewById(R.id.nowIndicatorContainer);

        populateTimesContainer();
    }

    public void clearAppointments() {
        appointmentsContainer.removeAllViews();
    }

    private void populateTimesContainer() {
        Calendar currentTime = getDayStart();
        Calendar endTime = getDayEnd();

        timeSeparator.removeAllViews();
        timesContainer.removeAllViews();

        while (endTime.compareTo(currentTime) > 0) {
            newTimeView(currentTime);
            newTimeSeparator();
            currentTime.add(Calendar.MINUTE, 30);
        }
    }

    private void addNowIndicatorLine() {
        nowIndicatorContainer.removeAllViews();
        Calendar now = Calendar.getInstance();
        if (compareCalendars(appointmentList.get(0).getTimeStampStart(), now) != 0) {
            return;
        }

        Context context = root.getContext();

        CardView nowIndicator = (CardView) LayoutInflater.from(context).inflate(R.layout.daily_calendar_now_indicator, nowIndicatorContainer, false);

        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) nowIndicator.getLayoutParams();
        lp.topMargin = DailyCalendarView.calculateTopMargin(context, now);

        nowIndicatorContainer.addView(nowIndicator);
    }

    private int compareCalendars(Calendar c1, Calendar c2) {
        if (c1.get(Calendar.YEAR) != c2.get(Calendar.YEAR))
            return c1.get(Calendar.YEAR) - c2.get(Calendar.YEAR);
        if (c1.get(Calendar.MONTH) != c2.get(Calendar.MONTH))
            return c1.get(Calendar.MONTH) - c2.get(Calendar.MONTH);
        return c1.get(Calendar.DAY_OF_MONTH) - c2.get(Calendar.DAY_OF_MONTH);
    }

    private void newTimeView(Calendar time) {
        View view = LayoutInflater.from(root.getContext()).inflate(R.layout.hour_minute, timesContainer, false);
        view.getLayoutParams().height = root.getContext().getResources().getDimensionPixelSize(R.dimen.quarter_hour) * 2;

        TextView textViewTime = view.findViewById(R.id.textViewTime);
        String timeStr = String.format("%02d:%02d", time.get(Calendar.HOUR_OF_DAY), time.get(Calendar.MINUTE));
        textViewTime.setText(timeStr);

        timesContainer.addView(view);
    }

    private void newTimeSeparator() {
        View view = LayoutInflater.from(root.getContext()).inflate(R.layout.half_hour_separator, timeSeparator, false);
        view.getLayoutParams().height = root.getContext().getResources().getDimensionPixelSize(R.dimen.quarter_hour) * 2;
        timeSeparator.addView(view);
    }

    private void inflateAppointments() {


        Map<Appointment, DailyCalendarView> appointmentViewMap = new HashMap<>();
        for (Appointment a : appointmentList) {
            appointmentViewMap.put(a, a.addSelfViewToContainer(appointmentsContainer, listener));
        }

        int index = 1;
        for (Appointment a1 : appointmentList) {
            Appointment a2;
            for (int j = index; j < appointmentList.size(); j++) {
                a2 = appointmentList.get(j);

                if (doCollide(a1, a2)) {
                    DailyCalendarView av1, av2;

                    av1 = appointmentViewMap.get(a1);
                    av2 = appointmentViewMap.get(a2);

                    appointmentViewMap.get(a1).addCollision(av2);
                    appointmentViewMap.get(a2).addCollision(av1);
                }
            }

            index++;
        }

        appointmentViewList = new ArrayList<>(appointmentViewMap.values());
        Collections.sort(appointmentViewList);
        for (DailyCalendarView av : appointmentViewList) {
            av.make();
        }
    }

    public List<Appointment> getAppointments() {
        return appointmentList;
    }

    public void setAppointments(List<Appointment> appointmentList) {
        this.appointmentList = appointmentList;

        clearAppointments();
        inflateAppointments();
        addNowIndicatorLine();
    }

    protected Calendar getDayStart() {
        Calendar out = Calendar.getInstance();

        out.set(Calendar.HOUR_OF_DAY, 0);
        out.set(Calendar.MINUTE, 0);

        return out;
    }

    protected Calendar getDayEnd() {
        Calendar out = Calendar.getInstance();

        out.set(Calendar.HOUR_OF_DAY, 0);
        out.set(Calendar.MINUTE, 59);
        out.add(Calendar.DAY_OF_MONTH, 1);

        return out;
    }

    protected boolean doCollide(Appointment a1, Appointment a2) {
        return a1.getTimeStampStart().compareTo(a2.getTimeStampEnd()) < 0 &&
                a2.getTimeStampStart().compareTo(a1.getTimeStampEnd()) < 0;
    }

    public void animateAppointment(String appointmentId) {
        DailyCalendarView appView = findViewForAppointment(appointmentId);
        if (appView != null) {
            ScrollView scrollView = rootView.findViewById(R.id.scrollViewDaily);
            scrollView.smoothScrollTo(0, appView.getTopMargin());
            appView.doHighlightAnimation();
        }
    }

    private DailyCalendarView findViewForAppointment(String appId) {
        for (DailyCalendarView dcv : appointmentViewList) {
            if (isClassType(dcv)) {
                for (Appointment app : ((AppointmentClassImpl) dcv.getAppointment()).getCompositionApps()) {
                    if (app.getId().equals(appId)) {
                        return dcv;
                    }
                }

            } else if (dcv.getAppointment().getId().equals(appId)) {
                return dcv;
            }
        }
        return null;
    }

    private boolean isClassType(DailyCalendarView dcv) {
        return dcv.getAppointment().getId() == null;
    }

    public interface OnAppointmentClickListener {
        void onAppointmentClick(Appointment appointment);
    }


}
