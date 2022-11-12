package com.yatenesturno.view_builder;

import static com.yatenesturno.functionality.LabelSelectorView.isLight;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.yatenesturno.R;
import com.yatenesturno.object_interfaces.Appointment;
import com.yatenesturno.object_interfaces.AppointmentService;
import com.yatenesturno.object_interfaces.Label;
import com.yatenesturno.objects.AppointmentImpl;

import java.util.Calendar;

public class OverviewAppointment implements OverView {

    private static String formatCalendar(Appointment appointment) {
        return String.format("%02d:%02d", appointment.getTimeStampStart().get(Calendar.HOUR_OF_DAY),
                appointment.getTimeStampStart().get(Calendar.MINUTE));
    }

    public static ViewHolderService createViewHolder(ViewGroup parent) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(getLayoutResId(), parent, false);
        return new ViewHolderService(v);
    }

    private static int getLayoutResId() {
        return R.layout.upcoming_event_service_view;
    }

    @Override
    public void bindViewHolder(
            final Appointment appointment,
            RecyclerView.ViewHolder holder,
            final OnAppointmentOverviewClickListener listener) {

        ViewHolderService holderService = (ViewHolderService) holder;
        holderService.labelClientName.setText(appointment.getName());

        String startStr = formatCalendar(appointment);
        holderService.labelTime.setText(startStr);

        String strDate = appointment.getTimeStampStart().get(Calendar.DATE) +
                "/" +
                ((appointment.getTimeStampStart().get(Calendar.MONTH) + 1) % 13);
        holderService.labelDate.setText(strDate);

        StringBuilder stringBuilder = new StringBuilder();

        for (AppointmentService aService : ((AppointmentImpl) appointment).getAppointmentServices()) {

            stringBuilder
                    .append(aService.getServiceInstance().getService().getName())
                    .append(" ");
        }

        if (appointment.usesCredits()) {
            holderService.ivCredits.setVisibility(View.VISIBLE);
        } else {
            holderService.ivCredits.setVisibility(View.INVISIBLE);
        }

        if (appointment.bookedWithoutCredits()) {
            holderService.labelWithoutCredits.setVisibility(View.VISIBLE);
        } else {
            holderService.labelWithoutCredits.setVisibility(View.GONE);
        }

        holderService.labelServices.setText(stringBuilder.toString());

        holderService.root.setOnClickListener(v -> listener.onClick(appointment));
        setLabel(holderService, appointment);
    }

    public void setLabel(ViewHolderService holder, Appointment app) {

        int labelColor = holder.cardViewTime.getContext().getColor(R.color.colorPrimary);

        Label label = app.getLabel();
        if (label != null) {
            labelColor = Color.parseColor(label.getColor());
        }

        if (isLight(labelColor)) {
            holder.labelTime.setTextColor(holder.cardViewTime.getContext().getColor(R.color.black));
        }

        holder.cardViewTime.setCardBackgroundColor(labelColor);
    }

    public static class ViewHolderService extends RecyclerView.ViewHolder {

        public final TextView labelTime;
        public final TextView labelClientName;
        public final TextView labelServices;
        public final CardView cardViewTime;
        public final RelativeLayout root;
        public final TextView labelDate;
        public final AppCompatImageView ivCredits;
        private final TextView labelWithoutCredits;

        public ViewHolderService(@NonNull View view) {
            super(view);

            root = view.findViewById(R.id.root);
            labelClientName = view.findViewById(R.id.labelClientName);
            labelTime = view.findViewById(R.id.labelTime);
            labelServices = view.findViewById(R.id.labelServices);
            labelDate = view.findViewById(R.id.textViewDate);
            cardViewTime = view.findViewById(R.id.cardViewTime);
            ivCredits = view.findViewById(R.id.ivCredits);
            labelWithoutCredits = view.findViewById(R.id.labelWithoutCredits);
        }
    }
}
