package com.yatenesturno.activities.job_appointment_book

import android.view.View
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.yatenesturno.R
import com.yatenesturno.utils.TimeZoneManager
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem
import eu.davidea.flexibleadapter.items.IFlexible
import eu.davidea.viewholders.FlexibleViewHolder
import java.util.*

class TimestampViewKt(val calendarUTC: Calendar) :
    AbstractFlexibleItem<TimestampViewKt.ViewHolderTimestamp>() {

    override fun equals(o: Any?): Boolean {
        return false
    }

    override fun getLayoutRes(): Int {
        return R.layout.timestamp_view_original
    }

    override fun createViewHolder(
        view: View,
        adapter: FlexibleAdapter<IFlexible<*>?>?
    ): ViewHolderTimestamp {
        return ViewHolderTimestamp(view, adapter)
    }

    override fun bindViewHolder(
        adapter: FlexibleAdapter<IFlexible<*>?>?,
        holder: ViewHolderTimestamp,
        position: Int,
        payloads: List<Any>
    ) {
        holder.labelTimeStamp.text = formatCalendar(TimeZoneManager.fromUTC(calendarUTC))
    }

    private fun formatCalendar(calendar: Calendar): String {
        return String.format(
            "%02d:%02d", calendar[Calendar.HOUR_OF_DAY],
            calendar[Calendar.MINUTE]
        )
    }

    class ViewHolderTimestamp(view: View, adapter: FlexibleAdapter<*>?) :
        FlexibleViewHolder(view, adapter) {
        var labelTimeStamp: TextView
        var cardViewTimeStamp: CardView

        init {
            cardViewTimeStamp = view.findViewById(R.id.cardViewTimestamp)
            labelTimeStamp = view.findViewById(R.id.labelTimeStamp)
        }
    }
}
