package com.yatenesturno.object_views;

import android.view.View;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.yatenesturno.R;
import com.yatenesturno.utils.TimeZoneManager;

import java.util.Calendar;
import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flexibleadapter.items.IFlexible;
import eu.davidea.viewholders.FlexibleViewHolder;

public class TimestampView extends AbstractFlexibleItem<TimestampView.ViewHolderTimestamp> {


    private final Calendar calendarUTC;

    public TimestampView(Calendar calendar) {
        calendarUTC = calendar;
    }

    public Calendar getCalendarUTC() {
        return calendarUTC;
    }

    @Override
    public boolean equals(Object o) {
        return false;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.timestamp_view;
    }


    @Override
    public ViewHolderTimestamp createViewHolder(View view, FlexibleAdapter<IFlexible> adapter) {
        return new ViewHolderTimestamp(view, adapter);
    }

    @Override
    public void bindViewHolder(FlexibleAdapter<IFlexible> adapter, ViewHolderTimestamp holder, int position, List<Object> payloads) {
        holder.labelTimeStamp.setText(
                formatCalendar(TimeZoneManager.fromUTC(calendarUTC))
        );
    }

    private String formatCalendar(Calendar calendar) {
        return String.format("%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE));
    }

    public static class ViewHolderTimestamp extends FlexibleViewHolder {
        public TextView labelTimeStamp;
        public CardView cardViewTimeStamp;

        public ViewHolderTimestamp(View view, FlexibleAdapter adapter) {
            super(view, adapter);
            this.cardViewTimeStamp = view.findViewById(R.id.cardViewTimestamp);
            this.labelTimeStamp = view.findViewById(R.id.labelTimeStamp);
        }
    }
}
