package com.yatenesturno.activities.job_edit.emergency_location;

import android.graphics.Color;
import android.view.View;

import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.cardview.widget.CardView;

import com.yatenesturno.R;
import com.yatenesturno.functionality.emergency.EmergencyLocation;

import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flexibleadapter.items.IFlexible;
import eu.davidea.viewholders.FlexibleViewHolder;

public class EmergencyLocationView extends AbstractFlexibleItem<EmergencyLocationView.ViewHolderEmergencyLocation> {

    private final EmergencyLocation emergencyLocation;
    private boolean selected;
    private final LocationViewListener listener;

    public EmergencyLocationView(EmergencyLocation emergencyLocation, LocationViewListener listener) {
        this.emergencyLocation = emergencyLocation;
        this.selected = false;
        this.listener = listener;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public EmergencyLocation getEmergencyLocation() {
        return emergencyLocation;
    }

    @Override
    public boolean equals(Object o) {
        return false;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.emergency_location_view;
    }

    @Override
    public ViewHolderEmergencyLocation createViewHolder(View view, FlexibleAdapter<IFlexible> adapter) {
        return new ViewHolderEmergencyLocation(adapter, view);
    }

    @Override
    public void bindViewHolder(FlexibleAdapter<IFlexible> adapter, ViewHolderEmergencyLocation holder, int position, List<Object> payloads) {
        holder.name.setText(emergencyLocation.getName());

        int color;
        if (selected) {
            color = holder.name.getContext().getResources().getColor(R.color.colorPrimary, null);
            holder.btnRemove.setColorFilter(Color.WHITE);
            holder.btnView.setColorFilter(Color.WHITE);
            holder.name.setTextColor(Color.WHITE);
        } else {
            color = holder.name.getContext().getResources().getColor(R.color.white, null);
            holder.btnRemove.setColorFilter(Color.BLACK);
            holder.btnView.setColorFilter(Color.BLACK);
            holder.name.setTextColor(Color.BLACK);
        }
        holder.cardViewLocation.setCardBackgroundColor(color);

        holder.btnView.setOnClickListener(view -> listener.onViewLocationClick(emergencyLocation));
        holder.btnRemove.setOnClickListener(view -> listener.onDeleteLocationClick(emergencyLocation));
        holder.cardViewLocation.setOnClickListener(view -> listener.onSelected(emergencyLocation));
    }

    public interface LocationViewListener {
        void onViewLocationClick(EmergencyLocation location);

        void onDeleteLocationClick(EmergencyLocation location);

        void onSelected(EmergencyLocation location);
    }

    public static class ViewHolderEmergencyLocation extends FlexibleViewHolder {
        public AppCompatTextView name;
        public AppCompatImageButton btnView, btnRemove;
        public CardView cardViewLocation;

        public ViewHolderEmergencyLocation(FlexibleAdapter<IFlexible> adapter, View v) {
            super(v, adapter);

            this.name = v.findViewById(R.id.labelName);
            this.btnView = v.findViewById(R.id.btnView);
            this.cardViewLocation = v.findViewById(R.id.cardViewLocation);
            this.btnRemove = v.findViewById(R.id.btnRemove);
        }
    }
}
