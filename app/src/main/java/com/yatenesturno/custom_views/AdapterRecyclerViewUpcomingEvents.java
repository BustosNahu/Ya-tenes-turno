package com.yatenesturno.custom_views;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yatenesturno.object_interfaces.Appointment;
import com.yatenesturno.objects.AppointmentImpl;
import com.yatenesturno.view_builder.OverviewAppointment;
import com.yatenesturno.view_builder.OverviewAppointmentClass;

import java.util.List;

public class AdapterRecyclerViewUpcomingEvents extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_VIEW_CLASS = 0, TYPE_VIEW_SERVICE = 1;
    private final List<Appointment> upcomingEvents;
    private final OnAppointmentClickListener listener;

    public AdapterRecyclerViewUpcomingEvents(List<Appointment> events, OnAppointmentClickListener listener) {
        upcomingEvents = events;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_VIEW_CLASS:
                return OverviewAppointmentClass.createViewHolder(parent);

            default:
            case TYPE_VIEW_SERVICE:
                return OverviewAppointment.createViewHolder(parent);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (upcomingEvents.get(position) instanceof AppointmentImpl) {
            return TYPE_VIEW_SERVICE;
        }
        return TYPE_VIEW_CLASS;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        upcomingEvents.get(position).getOverView()
                .bindViewHolder(
                        upcomingEvents.get(position),
                        holder,
                        listener::onClick);
    }

    @Override
    public int getItemCount() {
        return upcomingEvents.size();
    }

    public interface OnAppointmentClickListener {
        void onClick(Appointment appointment);
    }

}