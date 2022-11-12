package com.yatenesturno.object_views;

import android.view.View;

import androidx.annotation.LayoutRes;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.cardview.widget.CardView;

import com.yatenesturno.R;
import com.yatenesturno.object_interfaces.PlaceCredits;
import com.yatenesturno.utils.CalendarUtils;

import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flexibleadapter.items.IFlexible;
import eu.davidea.viewholders.FlexibleViewHolder;

public class ViewJobCredit extends AbstractFlexibleItem<ViewJobCredit.ViewHolderJobCredit> {

    private final PlaceCredits placeCredits;
    private final View.OnClickListener listener;

    public ViewJobCredit(PlaceCredits placeCredits, View.OnClickListener listener) {
        this.placeCredits = placeCredits;
        this.listener = listener;
    }

    public PlaceCredits getJobCredits() {
        return placeCredits;
    }

    @Override
    public boolean equals(Object o) {
        return ((ViewJobCredit) o).getJobCredits().getId().equals(placeCredits.getId());
    }

    @LayoutRes
    @Override
    public int getLayoutRes() {
        return R.layout.job_credit_item;
    }

    @Override
    public ViewHolderJobCredit createViewHolder(View view, FlexibleAdapter<IFlexible> adapter) {
        return new ViewHolderJobCredit(view, adapter);
    }

    @Override
    public void bindViewHolder(final FlexibleAdapter<IFlexible> adapter, ViewHolderJobCredit holder, final int position, List<Object> payloads) {

        holder.labelCreditsCount.setText(String.valueOf(placeCredits.getCurrentCredits()));
        holder.labelCreditsTotal.setText(String.format("/%s", placeCredits.getCredits()));
        holder.labelPeriodFrom.setText(CalendarUtils.formatDate(placeCredits.getValidFrom()));
        holder.labelPeriodUntil.setText(CalendarUtils.formatDate(placeCredits.getValidUntil()));

        holder.cardView.setOnClickListener(listener);
    }

    public static class ViewHolderJobCredit extends FlexibleViewHolder {
        public CardView cardView;
        public AppCompatTextView labelPeriodFrom, labelPeriodUntil, labelCreditsCount, labelCreditsTotal;

        public ViewHolderJobCredit(View view, FlexibleAdapter adapter) {
            super(view, adapter);

            this.cardView = view.findViewById(R.id.root);
            this.labelPeriodFrom = view.findViewById(R.id.labelPeriodFrom);
            this.labelPeriodUntil = view.findViewById(R.id.labelPeriodUntil);
            this.labelCreditsCount = view.findViewById(R.id.labelCreditsCount);
            this.labelCreditsTotal = view.findViewById(R.id.labelCreditsTotal);
        }
    }

}
