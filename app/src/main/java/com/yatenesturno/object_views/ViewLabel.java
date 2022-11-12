package com.yatenesturno.object_views;

import android.view.View;

import androidx.appcompat.widget.AppCompatImageButton;
import androidx.cardview.widget.CardView;

import com.yatenesturno.R;
import com.yatenesturno.functionality.LabelSelectorView;
import com.yatenesturno.object_interfaces.Label;

import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flexibleadapter.items.IFlexible;
import eu.davidea.viewholders.FlexibleViewHolder;

public class ViewLabel extends AbstractFlexibleItem<ViewLabel.ViewHolderLabel> {

    private final Label label;
    private final LabelSelectorView.OnLabelClickListener listener;

    public ViewLabel(Label label, LabelSelectorView.OnLabelClickListener listener) {
        this.label = label;
        this.listener = listener;
    }

    @Override
    public boolean equals(Object o) {
        return false;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.view_label_layout;
    }

    @Override
    public ViewHolderLabel createViewHolder(View view, FlexibleAdapter<IFlexible> adapter) {
        return new ViewHolderLabel(view, adapter);
    }

    @Override
    public void bindViewHolder(FlexibleAdapter<IFlexible> adapter, ViewHolderLabel holder, int position, List<Object> payloads) {
        LabelSelectorView.fillView(holder.cardViewLabel, label, false);
        holder.cardViewLabel.setOnClickListener(v -> listener.onLabelSelected(label));

        if (label.isNotAttendedLabel()) {
            holder.btnRemove.setVisibility(View.INVISIBLE);
        } else {
            holder.btnRemove.setVisibility(View.VISIBLE);
            holder.btnRemove.setOnClickListener(v -> listener.onDelete(label));
        }
    }

    public static class ViewHolderLabel extends FlexibleViewHolder {
        CardView cardViewLabel;
        AppCompatImageButton btnRemove;

        public ViewHolderLabel(View view, FlexibleAdapter adapter) {
            super(view, adapter);

            cardViewLabel = view.findViewById(R.id.cardViewLabel);
            btnRemove = view.findViewById(R.id.btnDelete);
        }
    }
}
