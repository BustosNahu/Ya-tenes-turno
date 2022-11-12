package com.yatenesturno.view_builder;

import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.cardview.widget.CardView;

import com.yatenesturno.R;
import com.yatenesturno.activities.appointment_view.DailyCalendar;
import com.yatenesturno.functionality.LabelSelectorView;
import com.yatenesturno.objects.AppointmentImpl;

import java.util.Calendar;

/**
 * Daily calendar view for a generic appoitment
 */
public class DailyCalendarViewAppointment extends DailyCalendarView {

    private final AppointmentImpl appointment;

    public DailyCalendarViewAppointment(ViewGroup container, AppointmentImpl appointment, DailyCalendar.OnAppointmentClickListener listener) {
        super(container, appointment, listener);

        this.appointment = appointment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.daily_appointment_view;
    }

    @Override
    protected void fillView(View view) {
        TextView textViewAppointmentTime = view.findViewById(R.id.textViewAppointmentTime);
        TextView textViewClient = view.findViewById(R.id.textViewClient);
        ViewGroup containerCredits = view.findViewById(R.id.containerCredits);
        AppCompatTextView labelWithoutCredits = view.findViewById(R.id.labelWithoutCredits);

        String timeStr = String.format("%02d:%02d", appointment.getTimeStampStart().get(Calendar.HOUR_OF_DAY), appointment.getTimeStampStart().get(Calendar.MINUTE));
        textViewAppointmentTime.setText(timeStr);
        textViewClient.setText(appointment.getClient().getName());

        int textColor = view.getContext().getColor(R.color.white);
        if (!appointment.didAttend()) {
            textColor = view.getContext().getColor(R.color.black);

        } else if (appointment.getLabel() != null) {

            int color = Color.parseColor(appointment.getLabel().getColor());
            if (LabelSelectorView.isLight(color)) {
                textColor = view.getContext().getColor(R.color.black);
            }
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

        textViewAppointmentTime.setTextColor(textColor);
        textViewClient.setTextColor(textColor);

        fillLabel(view);
    }

    private void fillLabel(View view) {

        CardView cardView = view.findViewById(R.id.cardViewAppointmentDaily);
        if (appointment.didAttend() && appointment.getLabel() != null) {
            cardView.setCardBackgroundColor(Color.parseColor(appointment.getLabel().getColor()));
        } else if (!appointment.didAttend()) {
            cardView.setCardBackgroundColor(Color.parseColor("#c9c9c9"));
        }

        CardView cardViewLabel = view.findViewById(R.id.cardViewLabel);
        LabelSelectorView.fillView(cardViewLabel, appointment);
    }
}
