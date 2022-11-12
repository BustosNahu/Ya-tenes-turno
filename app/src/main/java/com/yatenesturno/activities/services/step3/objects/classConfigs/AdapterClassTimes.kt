package com.yatenesturno.activities.services.step3.objects.classConfigs

import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.yatenesturno.R
import com.yatenesturno.activities.services.CreateServiceActivity
import com.yatenesturno.activities.services.step3.objects.BasicServiceInfoConfigurator
import com.yatenesturno.utils.CalendarUtils
import java.util.*

class AdapterClassTimes(
    val context: Context, val daysCalendar: Calendar,
    private var classTimes: MutableList<Calendar>? = null , val onItemClicked: (position: Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val TYPE_ADD_TIME = 0
    val TYPE_TIME_VIEW = 1

    fun getClassTimes() : MutableList<Calendar>? {
        return classTimes
    }
    class ViewHolderTimeView(itemView: View, ) :
        RecyclerView.ViewHolder(itemView) {

        var labelTimestamp: TextView
        var cardViewTimestamp: CardView
        var cvRemoveIcon: ImageButton

        init {
            cardViewTimestamp = itemView.findViewById(R.id.cardViewTimestamp)
            labelTimestamp = itemView.findViewById(R.id.labelTimeStamp)
            cvRemoveIcon = itemView.findViewById(R.id.removeIcon_cv_timestamp)

        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v: View
        return when (viewType) {
            TYPE_ADD_TIME -> {
                v = LayoutInflater.from(context)
                    .inflate(R.layout.class_time_add, parent, false)
                ViewHolderTimeAdd(v )
            }
            TYPE_TIME_VIEW -> {
                v = LayoutInflater.from(context)
                    .inflate(R.layout.timestamp_view, parent, false)
                ViewHolderTimeView(v)
            }
            else -> {
                v = LayoutInflater.from(context)
                    .inflate(R.layout.timestamp_view, parent, false)
                ViewHolderTimeView(v)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            //si no funciona añadir callbacks
            TYPE_ADD_TIME -> setAddTimeViewListener(holder as ViewHolderTimeAdd)
            TYPE_TIME_VIEW -> {
                (holder as ViewHolderTimeView).labelTimestamp.text = CalendarUtils.formatTime(
                    classTimes?.get(position)
                )
                setRemoveTimeListener(holder as ViewHolderTimeView, position)
            }
        }
    }
    private fun removeClassTime(position: Int) {
        classTimes!!.removeAt(position)
        notifyItemChanged(position)
    }
    private fun setRemoveTimeListener(holder: ViewHolderTimeView, position: Int) {
        holder.cvRemoveIcon.setOnClickListener(View.OnClickListener {
            removeClassTime(
                position
            )
        })

    }

    private fun createNewClassTime(hourOfDay: Int, minute: Int) {
        val newClassCalendar = Calendar.getInstance()
        newClassCalendar[Calendar.HOUR_OF_DAY] = hourOfDay
        newClassCalendar[Calendar.MINUTE] = minute
        classTimes!!.add(newClassCalendar)
        notifyDataSetChanged()
        onItemClicked(0)
        Log.d("validatedFromRev LOL", "A")
    }

    private fun showNewClassTimeDialog() {
        val timePickerDialog = TimePickerDialog(
            context,
            OnTimeSetListener { view: TimePicker?, hourOfDay: Int, minute: Int ->
                if (validateNewClassTime(hourOfDay, minute)) {
                    createNewClassTime(hourOfDay, minute)
                }
            },
            8, 0, true
        )
        timePickerDialog.show()
    }

    private fun getTimeInMinutes(calendar: Calendar): Int {
        return calendar[Calendar.HOUR_OF_DAY] * 60 + calendar[Calendar.MINUTE]
    }

    private fun showSelectValidTimeWarning() {
        Toast.makeText(
            context,
            context.getString(R.string.select_valid_class_time),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun showTimeAlreadySelectedWarning() {
        Toast.makeText(
            context,
            context.getString(R.string.already_selected_class_time),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun validateNewClassTime(hourOfDay: Int, minute: Int): Boolean {
        val duration = daysCalendar
        val durationInMinutes: Int = getTimeInMinutes(duration)
        val newClassTimeInMinutes = hourOfDay * 60 + minute
        var diff: Int
        for (c in classTimes!!) {
            diff = Math.abs(c.let { getTimeInMinutes(it) }.minus(newClassTimeInMinutes) ?: 10)
            if (diff == 0) {
                showTimeAlreadySelectedWarning()
                return false
            } else if (diff < durationInMinutes) {
                showSelectValidTimeWarning()
                return false
            }
        }
        return true
    }
    fun setAddTimeViewListener(holder: ViewHolderTimeAdd) {
        holder.cardViewTimeAdd.setOnClickListener(View.OnClickListener { v: View? ->
            if (hasSelectedDate(daysCalendar)) {
                showNewClassTimeDialog()
            } else {
                Toast.makeText(
                    context,
                    context.getString(R.string.req_first_select_duration),
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun hasSelectedDate(calendar: Calendar): Boolean {
        return calendar[Calendar.MINUTE] != 0 ||
                calendar[Calendar.HOUR_OF_DAY] != 0
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == classTimes?.size) {
            TYPE_ADD_TIME
        } else {
            TYPE_TIME_VIEW
        }
    }

    override fun getItemCount(): Int {
        return classTimes?.size?.plus(1) ?: 1
    }

    class ViewHolderTimeAdd(
        itemView: View,
    ) :
        RecyclerView.ViewHolder(itemView) {
        var cardViewTimeAdd: CardView

        init {
            cardViewTimeAdd = itemView.findViewById(R.id.cardViewTimeAdd)
        }
    }


}