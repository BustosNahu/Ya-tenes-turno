package com.yatenesturno.view_builder;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.yatenesturno.R;
import com.yatenesturno.activities.appointment_view.DailyCalendar;
import com.yatenesturno.object_interfaces.Appointment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public abstract class DailyCalendarView implements Comparable<DailyCalendarView> {

    private final Appointment appointment;
    private final DailyCalendar.OnAppointmentClickListener listener;
    private final List<DailyCalendarView> collisions;
    private final ViewGroup container;
    private int position = -1;
    private int topMargin;
    private View view;

    public DailyCalendarView(ViewGroup container, Appointment appointment, DailyCalendar.OnAppointmentClickListener listener) {
        this.container = container;
        this.appointment = appointment;
        collisions = new ArrayList<>();
        this.listener = listener;
    }

    protected static Calendar getDayStart() {
        Calendar out = Calendar.getInstance();

        out.set(Calendar.HOUR_OF_DAY, 0);
        out.set(Calendar.MINUTE, 0);

        return out;
    }

    /**
     * Get top margin pixels according to its start time
     *
     * @param calendar appointment time
     * @return top margin in pixels
     */
    public static int calculateTopMargin(Context context, Calendar calendar) {
        Calendar dayStart = getDayStart();

        float halfHourRatio = getHalfHourRatio(dayStart, calendar);
        int halfHourDimension = context.getResources().getDimensionPixelSize(R.dimen.half_hour);

        return (int) (halfHourDimension * halfHourRatio);
    }

    private static boolean isMidNight(Calendar calendar) {
        return calendar.get(Calendar.HOUR_OF_DAY) == 0 && calendar.get(Calendar.MINUTE) == 0;
    }

    /**
     * Get the half hour ratio for an appointment
     * e.g: an appointment lasting 01:15 has a 2.5 half-hour-ratio
     *
     * @param start appointment start
     * @param end   appointment end
     * @return half hour ratio as described
     */
    protected static float getHalfHourRatio(Calendar start, Calendar end) {
        int hoursAsMinutesStart, hoursAsMinutesEnd;

        hoursAsMinutesStart = start.get(Calendar.HOUR_OF_DAY) * 60 + start.get(Calendar.MINUTE);

        if (isMidNight(end)) {
            hoursAsMinutesEnd = 1439;
        } else if (end.get(Calendar.HOUR_OF_DAY) < start.get(Calendar.HOUR_OF_DAY)) {
            hoursAsMinutesEnd = 1440 + end.get(Calendar.HOUR_OF_DAY) * 60 + end.get(Calendar.MINUTE);
        } else {
            hoursAsMinutesEnd = end.get(Calendar.HOUR_OF_DAY) * 60 + end.get(Calendar.MINUTE);
        }

        return (hoursAsMinutesEnd - hoursAsMinutesStart) / 30f;
    }

    public void addCollision(DailyCalendarView av) {
        collisions.add(av);
    }

    public void make() {
        view = inflateView();

        fillView(view);
        setListener(view);

        container.addView(view);
    }

    private View inflateView() {
        LayoutInflater inflater = LayoutInflater.from(getContainer().getContext());
        LinearLayout appointmentView = (LinearLayout) inflater.inflate(getLayoutResId(), getContainer(), false);

        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) appointmentView.getLayoutParams();

        if (getPosition() == -1) {
            setPosition(calculatePosition());
        }

        int maxMutualCollisions = calculateMaxMutualCollisions();

        lp.height = calculateHeight();
        topMargin = calculateTopMargin(container.getContext(), appointment.getTimeStampStart());
        lp.topMargin = topMargin;
        lp.leftMargin = calculateLeftMargin(maxMutualCollisions);
        lp.width = calculateWidth(maxMutualCollisions);

        return appointmentView;
    }

    public int getTopMargin() {
        return topMargin;
    }

    protected abstract int getLayoutResId();

    public ViewGroup getContainer() {
        return container;
    }

    protected int getOneDpMargin() {
        return container.getContext().getResources().getDimensionPixelSize(R.dimen.one_dp);
    }

    protected int calculatePosition() {

        int max = Integer.MIN_VALUE;
        int minAvailable = -1;
        for (DailyCalendarView av : getCollisions()) {
            if (av.getPosition() != -1) {
                if (av.getPosition() > max) {
                    max = av.getPosition();
                }
                if (av.getPosition() < minAvailable || minAvailable == -1) {
                    minAvailable = av.getPosition();
                }
            }
        }
        if (minAvailable > 0) {
            return minAvailable - 1;
        } else {
            return Math.max(0, max + 1);
        }
    }

    public void setListener(View view) {
        view.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAppointmentClick(appointment);
            }
        });
    }

    protected abstract void fillView(View view);

    protected boolean doCollide(Appointment a1, Appointment a2) {
        return a1.getTimeStampStart().compareTo(a2.getTimeStampEnd()) < 0 &&
                a2.getTimeStampStart().compareTo(a1.getTimeStampEnd()) < 0;
    }

    protected int getPosition() {
        return position;
    }

    protected void setPosition(int pos) {
        this.position = pos;
    }

    public Appointment getAppointment() {
        return appointment;
    }

    protected int calculateMaxMutualCollisions() {
        if (collisions.size() > 0) {
            int max = 1;
            int index = 1;
            for (DailyCalendarView a1 : collisions) {
                DailyCalendarView a2;
                int currSum = 1;
                for (int j = index; j < collisions.size(); j++) {
                    a2 = collisions.get(j);

                    if (doCollide(a1.getAppointment(), a2.getAppointment())) {
                        currSum++;
                    }
                }
                max = Math.max(max, currSum);

                index++;
            }
            return max + 1;
        }
        return 1;
    }

    protected int calculateLeftMargin(int maxMutualCollisions) {
        return position * calculateWidth(maxMutualCollisions);
    }

    protected int calculateHeight() {
        Calendar start = appointment.getTimeStampStart();
        Calendar end = appointment.getTimeStampEnd();

        int halfHourDimension = container.getContext().getResources().getDimensionPixelSize(R.dimen.half_hour);

        return (int) (halfHourDimension * getHalfHourRatio(start, end) - getOneDpMargin());
    }

    /**
     * Get Width according to its mutual collisions
     *
     * @param maxMutualCollisions colissions count (sharing an hour and minute atleast)
     * @return width in pixels
     */
    protected int calculateWidth(int maxMutualCollisions) {
        return container.getWidth() / maxMutualCollisions;
    }

    public List<DailyCalendarView> getCollisions() {
        return collisions;
    }

    public int compareTo(DailyCalendarView appointmentView) {
        return collisions.size() - appointmentView.getCollisions().size();
    }

    /**
     * When daily view is accessed via a notification, make a highlight animation
     * indicating which apopointment has been booked
     */
    public void doHighlightAnimation() {
        if (view != null) {
            ObjectAnimator scaleDown = ObjectAnimator.ofPropertyValuesHolder(
                    view,
                    PropertyValuesHolder.ofFloat("scaleX", 1.1f),
                    PropertyValuesHolder.ofFloat("scaleY", 1.1f));
            scaleDown.setDuration(150);
            scaleDown.setStartDelay(300);
            scaleDown.setRepeatCount(3);
            scaleDown.setRepeatMode(ObjectAnimator.REVERSE);

            scaleDown.start();
        }
    }
}
