package com.yatenesturno.object_views;

import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.LayoutRes;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.yatenesturno.R;
import com.yatenesturno.object_interfaces.ServiceInstance;

import java.io.Serializable;
import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.common.IFlexibleLayoutManager;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flexibleadapter.items.IFilterable;
import eu.davidea.flexibleadapter.items.IFlexible;
import eu.davidea.viewholders.FlexibleViewHolder;

public class ViewSelectableServiceInstance extends AbstractFlexibleItem<ViewSelectableServiceInstance.ViewHolderJobType> implements IFilterable {

    private final ServiceInstance serviceInstance;
    private final CompoundButton.OnCheckedChangeListener listener;
    private boolean isSelected;

    public ViewSelectableServiceInstance(ServiceInstance serviceInstance, CompoundButton.OnCheckedChangeListener listener) {
        this.serviceInstance = serviceInstance;
        this.isSelected = false;
        this.listener = listener;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public ServiceInstance getServiceInstance() {
        return serviceInstance;
    }

    @Override
    public boolean equals(Object o) {
        return ((ViewSelectableServiceInstance) o).getServiceInstance().getService().equals(serviceInstance.getService());
    }

    @LayoutRes
    @Override
    public int getLayoutRes() {
        return R.layout.concurrent_service_item;
    }

    @Override
    public ViewHolderJobType createViewHolder(View view, FlexibleAdapter<IFlexible> adapter) {
        return new ViewHolderJobType(view, adapter);
    }

    @Override
    public void bindViewHolder(final FlexibleAdapter<IFlexible> adapter, ViewHolderJobType holder, final int position, List<Object> payloads) {
        holder.textView.setText(serviceInstance.getService().getName());
        holder.checkBox.setChecked(isSelected);

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            IFlexibleLayoutManager layoutManager = adapter.getFlexibleLayoutManager();
            int firstVisiblePosition = layoutManager.findFirstVisibleItemPosition();
            int lastVisiblePosition = layoutManager.findLastVisibleItemPosition();
            boolean positionIsVisible =
                    position >= firstVisiblePosition &&
                            position <= lastVisiblePosition;

            if (positionIsVisible) {
                isSelected = isChecked;
            }
            notifyListener(buttonView, isChecked);
        });

    }

    private void notifyListener(CompoundButton buttonView, boolean isChecked) {
        if (listener != null) {
            listener.onCheckedChanged(buttonView, isChecked);
        }
    }

    @Override
    public boolean filter(Serializable constraint) {
        return serviceInstance.getService().getName().toLowerCase().contains((CharSequence) constraint);
    }

    public static class ViewHolderJobType extends FlexibleViewHolder {
        public TextView textView;
        public MaterialCheckBox checkBox;
        public MaterialCardView cardView;

        public ViewHolderJobType(View view, FlexibleAdapter adapter) {
            super(view, adapter);
            this.cardView = view.findViewById(R.id.root);
            this.textView = view.findViewById(R.id.textViewJobTypeName);
            this.checkBox = view.findViewById(R.id.checkBoxConcurrentService);
        }
    }

}
