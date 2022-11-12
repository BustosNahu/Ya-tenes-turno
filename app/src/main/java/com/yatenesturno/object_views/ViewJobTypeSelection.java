package com.yatenesturno.object_views;

import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.LayoutRes;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.color.MaterialColors;
import com.yatenesturno.R;
import com.yatenesturno.object_interfaces.JobType;

import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.common.IFlexibleLayoutManager;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flexibleadapter.items.IFilterable;
import eu.davidea.flexibleadapter.items.IFlexible;
import eu.davidea.viewholders.FlexibleViewHolder;

public class ViewJobTypeSelection extends AbstractFlexibleItem<ViewJobTypeSelection.ViewHolderJobType> implements IFilterable<String> {

    private final JobType jobType;
    private boolean isSelected, isCheckable;

    public ViewJobTypeSelection(JobType jobType) {
        this.jobType = jobType;
        this.isSelected = false;
        this.isCheckable = true;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public void disableCheck() {
        this.isCheckable = false;
    }

    public JobType getJobType() {
        return jobType;
    }

    @Override
    public boolean equals(Object o) {
        return ((ViewJobTypeSelection) o).getJobType().equals(jobType);
    }

    @LayoutRes
    @Override
    public int getLayoutRes() {
        return R.layout.jobtype_item;
    }

    @Override
    public ViewHolderJobType createViewHolder(View view, FlexibleAdapter<IFlexible> adapter) {
        return new ViewHolderJobType(view, adapter);
    }

    @Override
    public void bindViewHolder(final FlexibleAdapter<IFlexible> adapter, ViewHolderJobType holder, final int position, List<Object> payloads) {
        holder.textView.setText(jobType.getType());
        holder.checkBox.setChecked(isSelected);

        holder.checkBox.setEnabled(isCheckable);
        ;

        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                IFlexibleLayoutManager layoutManager = adapter.getFlexibleLayoutManager();
                int firstVisiblePosition = layoutManager.findFirstVisibleItemPosition();
                int lastVisiblePosition = layoutManager.findLastVisibleItemPosition();
                boolean positionIsVisible =
                        position >= firstVisiblePosition &&
                                position <= lastVisiblePosition;

                if (positionIsVisible) {
                    isSelected = isChecked;
                }
            }
        });

    }

    @Override
    public boolean filter(String constraint) {
        return this.jobType.getType().toLowerCase().contains(constraint);
    }

    public static class ViewHolderJobType extends FlexibleViewHolder {
        public TextView textView;
        public MaterialCheckBox checkBox;

        public ViewHolderJobType(View view, FlexibleAdapter adapter) {
            super(view, adapter);
            this.textView = view.findViewById(R.id.textViewJobTypeName);
            this.checkBox = view.findViewById(R.id.checkBoxJobType);
        }
    }
}
