package com.yatenesturno.view_builder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.yatenesturno.R;
import com.yatenesturno.object_interfaces.Appointment;
import com.yatenesturno.object_interfaces.AppointmentService;
import com.yatenesturno.objects.AppointmentClassImpl;
import com.yatenesturno.objects.AppointmentImpl;

import java.util.Calendar;

public class OverviewAppointmentClass implements OverView {

    private static String formatCalendar(Appointment appointment) {
        return String.format("%02d:%02d", appointment.getTimeStampStart().get(Calendar.HOUR_OF_DAY),
                appointment.getTimeStampStart().get(Calendar.MINUTE));
    }

    public static RecyclerView.ViewHolder createViewHolder(ViewGroup parent) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(getLayoutResId(), parent, false);
        return new ViewHolderClass(v);
    }

    public static int getLayoutResId() {
        return R.layout.upcoming_event_service_class_view;
    }

    @Override
    public void bindViewHolder(final Appointment appointment, RecyclerView.ViewHolder holder, final OnAppointmentOverviewClickListener listener) {

        ViewHolderClass holderClass = (ViewHolderClass) holder;

        holderClass.labelClassName.setText(((AppointmentClassImpl) appointment).getServiceInstance().getService().getName());

        String startStr = formatCalendar(appointment);
        holderClass.labelTime.setText(startStr);


        if (appointment.usesCredits()) {
            holderClass.ivCredits.setVisibility(View.VISIBLE);
        } else {
            holderClass.ivCredits.setVisibility(View.INVISIBLE);
        }

        if (appointment.bookedWithoutCredits()) {
            holderClass.labelWithoutCredits.setVisibility(View.VISIBLE);
        } else {
            holderClass.labelWithoutCredits.setVisibility(View.GONE);
        }

        String strDate = appointment.getTimeStampStart().get(Calendar.DATE) +
                "/" +
                ((appointment.getTimeStampStart().get(Calendar.MONTH) + 1) % 13);
        holderClass.labelDate.setText(strDate);

        String peopleCountText = getPeopleCountString((AppointmentClassImpl) appointment, holderClass);
        holderClass.labelPeopleCount.setText(peopleCountText);

        holderClass.root.setOnClickListener(v -> listener.onClick(appointment));
    }

    public String getPeopleCountString(AppointmentClassImpl appointment, ViewHolderClass holderClass) {
        Context context = holderClass.labelClassName.getContext();
        int peopleCount = appointment.getPeopleCount();
        String peopleCountText;
        if (peopleCount > 1) {
            peopleCountText = peopleCount + " " + context.getResources().getString(R.string.people_count_plural);
        } else {
            peopleCountText = peopleCount + " " + context.getResources().getString(R.string.people_count_singular);
        }
        return "Clase: " + peopleCountText + " de " + appointment.getServiceInstance().getConcurrency();
    }


    public static class ViewHolderClass extends RecyclerView.ViewHolder {

        public final AppCompatImageView ivCredits;
        private final TextView labelTime;
        private final TextView labelClassName;
        private final TextView labelPeopleCount;
        private final RelativeLayout root;
        private final TextView labelDate;
        private final TextView labelWithoutCredits;

        public ViewHolderClass(@NonNull View view) {
            super(view);

            root = view.findViewById(R.id.root);
            labelClassName = view.findViewById(R.id.labelClassName);
            labelTime = view.findViewById(R.id.labelTime);
            labelPeopleCount = view.findViewById(R.id.labelPeopleCount);
            labelDate = view.findViewById(R.id.textViewDate);
            ivCredits = view.findViewById(R.id.ivCredits);
            labelWithoutCredits = view.findViewById(R.id.labelWithoutCredits);
        }
    }

}
