package com.yatenesturno.view_builder;

import androidx.recyclerview.widget.RecyclerView;

import com.yatenesturno.object_interfaces.Appointment;

public interface OverView {

    void bindViewHolder(
            final Appointment appointment,
            RecyclerView.ViewHolder holder,
            final OnAppointmentOverviewClickListener listener);
}
