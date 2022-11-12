package com.yatenesturno.view_builder;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.cardview.widget.CardView;

import com.yatenesturno.R;
import com.yatenesturno.activities.appointment_view.DailyCalendar;
import com.yatenesturno.object_interfaces.Appointment;
import com.yatenesturno.objects.AppointmentClassImpl;

import java.util.Calendar;

/**
 * Daily calendar view for a class appointment
 */
public class DailyCalendarViewAppointmentClass extends DailyCalendarView {

    private final AppointmentClassImpl appointment;

    public DailyCalendarViewAppointmentClass(ViewGroup container, AppointmentClassImpl appointment, DailyCalendar.OnAppointmentClickListener listener) {
        super(container, appointment, listener);

        this.appointment = appointment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.daily_appointment_view_class;
    }

    @Override
    protected void fillView(View view) {
        TextView labelAppointmentTime = view.findViewById(R.id.labelAppointmentTime);
        TextView labelPeopleCount = view.findViewById(R.id.labelPeopleCount);
        TextView labelClassName = view.findViewById(R.id.labelClassName);
        ViewGroup containerCredits = view.findViewById(R.id.containerCredits);
        AppCompatTextView labelWithoutCredits = view.findViewById(R.id.labelWithoutCredits);

        labelClassName.setText(appointment.getServiceInstance().getService().getName());

        String timeStr = String.format("%02d:%02d", appointment.getTimeStampStart().get(Calendar.HOUR_OF_DAY), appointment.getTimeStampStart().get(Calendar.MINUTE));
        labelAppointmentTime.setText(timeStr);

        labelPeopleCount.setText(getPeopleCountString(view.getContext()));

        if (!atLeastOneAssisted()) {
            ((CardView) view.findViewById(R.id.cardViewAppointmentDaily)).setCardBackgroundColor(view.getContext().getColor(R.color.darker_grey));
        }

        if (appointment.usesCredits()) {
            containerCredits.setVisibility(View.VISIBLE);
            if (appointment.bookedWithoutCredits()) {
                labelWithoutCredits.setVisibility(View.VISIBLE);
            } else {
                labelWithoutCredits.setVisibility(View.GONE);
            }
        } else {
            containerCredits.setVisibility(View.GONE);
        }
    }

    private boolean atLeastOneAssisted() {
        for (Appointment app : appointment.getCompositionApps()) {
            if (app.didAttend()) return true;
        }
        return false;
    }

    /**
     * Get user friendly string to display de amount of clients in the class appointment
     *
     * @param context
     * @return
     */
    public String getPeopleCountString(Context context) {
        int peopleCount = appointment.getPeopleCount();
        String peopleCountText;
        if (peopleCount > 1) {
            peopleCountText = peopleCount + " " + context.getResources().getString(R.string.people_count_plural);
        } else {
            peopleCountText = peopleCount + " " + context.getResources().getString(R.string.people_count_singular);
        }
        return "Clase: " + peopleCountText + " de " + appointment.getServiceInstance().getConcurrency();
    }
}
