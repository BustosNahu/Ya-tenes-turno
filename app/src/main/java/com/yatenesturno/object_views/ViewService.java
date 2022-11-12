package com.yatenesturno.object_views;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.material.card.MaterialCardView;
import com.yatenesturno.R;
import com.yatenesturno.object_interfaces.Service;

import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flexibleadapter.items.IFilterable;
import eu.davidea.flexibleadapter.items.IFlexible;
import eu.davidea.viewholders.FlexibleViewHolder;

public class ViewService extends AbstractFlexibleItem<ViewService.ViewHolderService> implements IFilterable<String> {
    private final Service service;
    private boolean isProvided;
    private boolean emergency;
    private boolean isCredits;

    public ViewService(Service service, boolean isProvided, boolean emergency, boolean credits) {
        this.isProvided = isProvided;
        this.service = service;
        this.emergency = emergency;
        this.isCredits = credits;
    }

    public boolean isCredits() {
        return isCredits;
    }

    public void setCredits(boolean credits) {
        isCredits = credits;
    }

    public void setEmergency(boolean emergency) {
        this.emergency = emergency;
    }

    public boolean isProvided() {
        return isProvided;
    }

    public void setProvided(boolean provided) {
        isProvided = provided;
    }

    public Service getService() {
        return service;
    }

    @Override
    public boolean filter(String constraint) {
        return service.getName().toLowerCase().contains(constraint);
    }

    @Override
    public boolean equals(Object o) {
        return false;
    }

    @LayoutRes
    @Override
    public int getLayoutRes() {
        return R.layout.view_service_original_layout;
    }

    @Override
    public ViewHolderService createViewHolder(final View view, FlexibleAdapter<IFlexible> adapter) {
        return new ViewHolderService(view, adapter);
    }

    @Override
    public void bindViewHolder(FlexibleAdapter<IFlexible> adapter, ViewHolderService holder, int position, List<Object> payloads) {
        holder.labelName.setText(service.getName());

        holder.typeIcon.setVisibility(View.GONE);
        if (isProvided) {
            holder.labelName.setTextColor(holder.labelName.getResources().getColor(R.color.white));
            holder.cardView.setCardBackgroundColor(holder.cardView.getResources().getColor(R.color.colorPrimary));

            if (emergency) {
                holder.typeIcon.setVisibility(View.VISIBLE);
                holder.typeIcon.setImageDrawable(ResourcesCompat.getDrawable(holder.typeIcon.getResources(), R.drawable.ic_emergency, null));
            } else if (isCredits) {
                holder.typeIcon.setVisibility(View.VISIBLE);
                holder.typeIcon.setImageDrawable(ResourcesCompat.getDrawable(holder.typeIcon.getResources(), R.drawable.ic_credits, null));
            } else {
                holder.typeIcon.setVisibility(View.GONE);
            }

        } else {
            holder.labelName.setTextColor(holder.labelName.getContext().getResources().getColor(R.color.black));
            holder.cardView.setCardBackgroundColor(holder.cardView.getResources().getColor(R.color.white));
        }
    }

    public static class ViewHolderService extends FlexibleViewHolder {
        TextView labelName;
        MaterialCardView cardView;
        AppCompatImageView typeIcon;

        public ViewHolderService(View view, FlexibleAdapter adapter) {
            super(view, adapter);

            cardView = view.findViewById(R.id.cardViewContainer);
            labelName = view.findViewById(R.id.labelName);
            typeIcon = view.findViewById(R.id.typeIcon);
        }
    }
}
