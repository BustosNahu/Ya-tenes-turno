package com.yatenesturno.activities.place_register.step_2.OpenHoursItem

import android.content.res.ColorStateList
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.switchmaterial.SwitchMaterial
import com.yatenesturno.R
import com.yatenesturno.objects.open_hour_cardView.OpenHoursObject
import eu.davidea.flexibleadapter.items.IFilterable


class OpenHoursAdapter(private val OpenHoursList : MutableList<OpenHoursObject>, var listener : ClicksAdapterListener) : RecyclerView.Adapter<OpenHoursAdapter.OpenHoursViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OpenHoursViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.opening_hours_item, parent, false)

        return OpenHoursViewHolder(view, listener)
    }



     class OpenHoursViewHolder(itemView : View, listener: ClicksAdapterListener) : RecyclerView.ViewHolder(itemView)  {
        var onClickListener: ClicksAdapterListener = listener
        val dayTv : TextView = itemView.findViewById(R.id.tv_day)
        val open_hour : TextView = itemView.findViewById(R.id.open_time)

        val open_hour_edit_cv : CardView = itemView.findViewById(R.id.open_time_edit_view)
        val close_hour_edit_cv : CardView = itemView.findViewById(R.id.close_time_edit_view)
        val open_hour_edit_tv : TextView = itemView.findViewById(R.id.open_time_edit_tv)
        val close_hour_edit_tv : TextView = itemView.findViewById(R.id.close_time_edit)

        val close_hour : TextView = itemView.findViewById(R.id.close_time)
        val mitday_start_time : TextView = itemView.findViewById(R.id.mitday_close_time)
        val mitday_end_time : TextView = itemView.findViewById(R.id.mitday_open_time)

        val mitday_start_time_edit_cv : CardView = itemView.findViewById(R.id.open_midtime_time_edit_view_cv)
        val mitday_end_time_edit_cv : CardView = itemView.findViewById(R.id.close_midtime_time_edit_view_cv)
        val mitday_start_time_edit_tv : TextView = itemView.findViewById(R.id.open_midtime_time_edit_view_tv)
        val mitday_end_time_edit_tv : TextView = itemView.findViewById(R.id.close_midtime_time_edit_view_tv)

         val startMidTimeTv : TextView = itemView.findViewById(R.id.startMidTime_tv)
         val endMidTimeTv : TextView = itemView.findViewById(R.id.endMidTimeTv)
         val endTv : TextView = itemView.findViewById(R.id.end_tv)
         val startTv : TextView = itemView.findViewById(R.id.start_tv)

        var active : SwitchMaterial = itemView.findViewById(R.id.switch_active)
        val midTime : CheckBox = itemView.findViewById(R.id.midtime_state)

        val editBtn : ImageButton = itemView.findViewById(R.id.clock_ic)
        val okeyBtn : ImageButton = itemView.findViewById(R.id.ok_ic)
        val cancelBtn : ImageButton = itemView.findViewById(R.id.cancel_ic)

         val editBtnCard : CardView = itemView.findViewById(R.id.clock_cv)
         val okeyBtnCard : CardView = itemView.findViewById(R.id.ok_cv)
         val cancelBtnCard : CardView = itemView.findViewById(R.id.cancel_cv)

        val mainCardView : CardView = itemView.findViewById(R.id.cv_openhours_item)
        val mainCL : ConstraintLayout = itemView.findViewById(R.id.cl_all_openHoursItem)
        val closeOrNoActive : ConstraintLayout = itemView.findViewById(R.id.close_no_active)

         val closeMitDayTv : TextView = itemView.findViewById(R.id.close_mit_day)



        init {
            active.setOnClickListener {
                Log.d("clicked", "bind + $bindingAdapterPosition")
                onClickListener.turnBtnClick(it, bindingAdapterPosition)
            }

            editBtn.setOnClickListener {
                Log.d("clickedEdit", "bind + $bindingAdapterPosition")
                onClickListener.editBtnClick(it, bindingAdapterPosition)
            }

            open_hour_edit_cv.setOnClickListener {
                onClickListener.openHourCvClick(it, bindingAdapterPosition)
            }

            close_hour_edit_cv.setOnClickListener {
                onClickListener.closeHourCvClick(it, bindingAdapterPosition)
            }

            mitday_end_time_edit_cv.setOnClickListener {
                onClickListener.openMidTimeCvClick(it, bindingAdapterPosition)
            }

            mitday_start_time_edit_cv.setOnClickListener {
                onClickListener.closeMidTimeCvClick(it, bindingAdapterPosition)

            }

            midTime.setOnClickListener {
                onClickListener.midTimeCBClick(it, bindingAdapterPosition)

            }

            okeyBtn.setOnClickListener {
                onClickListener.saveBtnClick(it, bindingAdapterPosition)
            }

        }

    }

    override fun onBindViewHolder(holder: OpenHoursViewHolder, position: Int) {
        val openHourCard = OpenHoursList[position]


        holder.dayTv.text = openHourCard.day_name
        holder.open_hour.text = if (openHourCard.open_time == "null:null") "00:00" else openHourCard.open_time
        holder.close_hour.text = if (openHourCard.close_time == "null:null") "00:00" else openHourCard.close_time

        holder.open_hour_edit_tv.text = if (openHourCard.open_time == "null:null") "00:00" else openHourCard.open_time
        holder.close_hour_edit_tv.text = if (openHourCard.close_time == "null:null") "00:00" else openHourCard.close_time

        holder.mitday_end_time.text = if (openHourCard.rest_end_time == "null:null") "00:00" else openHourCard.rest_end_time
        holder.mitday_start_time.text = if (openHourCard.rest_start_time == "null:null") "00:00" else openHourCard.rest_start_time

        holder.mitday_start_time_edit_tv.text = if (openHourCard.rest_start_time == "null:null") "00:00" else openHourCard.rest_start_time
        holder.mitday_end_time_edit_tv.text = if (openHourCard.rest_end_time == "null:null") "00:00" else openHourCard.rest_end_time
        holder.midTime.isChecked = openHourCard.restTime


        if (openHourCard.isAnotherEditing == true){
            holder.active.isEnabled = false
            holder.active.isClickable = false
            holder.dayTv.setTextColor(Color.parseColor("#D8D2D2"))
            holder.active.thumbTintList = ColorStateList.valueOf(Color.parseColor("#B6B2B2"))
            holder.active.trackTintList = ColorStateList.valueOf(Color.parseColor("#D8D2D2"))
            holder.open_hour.setTextColor(Color.parseColor("#D8D2D2"))
            holder.close_hour.setTextColor(Color.parseColor("#D8D2D2"))
            holder.startMidTimeTv.setTextColor(Color.parseColor("#D8D2D2"))
            holder.endMidTimeTv.setTextColor(Color.parseColor("#D8D2D2"))
            holder.endTv.setTextColor(Color.parseColor("#D8D2D2"))
            holder.startTv.setTextColor(Color.parseColor("#D8D2D2"))
            holder.closeMitDayTv.setTextColor(Color.parseColor("#D8D2D2"))
            holder.mitday_start_time.setTextColor(Color.parseColor("#D8D2D2"))
            holder.mitday_end_time.setTextColor(Color.parseColor("#D8D2D2"))
            holder.editBtn.setImageDrawable(ContextCompat.getDrawable(holder.itemView.context, R.drawable.ic_inactive_clock))
        }else{
            holder.dayTv.setTextColor(Color.parseColor("#FF8672"))
            holder.active.thumbTintList = ColorStateList.valueOf(Color.parseColor("#FF8672"))
            holder.active.trackTintList = ColorStateList.valueOf(Color.parseColor("#D8F6B0B4"))
            holder.open_hour.setTextColor(Color.parseColor("#717171"))
            holder.close_hour.setTextColor(Color.parseColor("#717171"))
            holder.startMidTimeTv.setTextColor(Color.parseColor("#717171"))
            holder.endMidTimeTv.setTextColor(Color.parseColor("#717171"))
            holder.endTv.setTextColor(Color.parseColor("#717171"))
            holder.startTv.setTextColor(Color.parseColor("#717171"))
            holder.closeMitDayTv.setTextColor(Color.parseColor("#717171"))
            holder.mitday_start_time.setTextColor(Color.parseColor("#717171"))
            holder.mitday_end_time.setTextColor(Color.parseColor("#717171"))
            holder.editBtn.setImageDrawable(ContextCompat.getDrawable(holder.itemView.context, R.drawable.ic_clock_outline))
            holder.active.isEnabled = true
            holder.active.isClickable = true

        }




        if (openHourCard.active){
            holder.active.isChecked = true
            holder.mainCardView.setBackgroundColor(Color.parseColor("#FFFFFF"))
            holder.mainCL.visibility = View.VISIBLE
            holder.closeOrNoActive.visibility = View.INVISIBLE

            if (openHourCard.isEditing == true){
                holder.active.isEnabled = false

                if (openHourCard.restTime){
                    holder.mitday_start_time_edit_tv.setTextColor(Color.parseColor("#717171"))
                    holder.mitday_end_time_edit_tv.setTextColor(Color.parseColor("#717171"))
                    holder.startMidTimeTv.setTextColor(Color.parseColor("#717171"))
                    holder.endMidTimeTv.setTextColor(Color.parseColor("#717171"))
                    holder.mitday_end_time_edit_cv.isEnabled = true
                    holder.mitday_end_time_edit_cv.isEnabled = true
                }else{
                    holder.mitday_start_time_edit_tv.setTextColor(Color.parseColor("#c4c4c4"))
                    holder.mitday_end_time_edit_tv.setTextColor(Color.parseColor("#c4c4c4"))

                    holder.startMidTimeTv.setTextColor(Color.parseColor("#c4c4c4"))
                    holder.endMidTimeTv.setTextColor(Color.parseColor("#c4c4c4"))
                    holder.mitday_end_time_edit_cv.isEnabled = false
                    holder.mitday_end_time_edit_cv.isEnabled = false
                }
                holder.closeMitDayTv.text = "¿Cerrás al mediodia?"
                holder.midTime.visibility = View.VISIBLE
                holder.editBtnCard.visibility = View.INVISIBLE

                holder.okeyBtnCard.visibility = View.VISIBLE
                holder.cancelBtnCard.visibility = View.VISIBLE

                holder.open_hour.visibility = View.INVISIBLE
                holder.close_hour.visibility = View.INVISIBLE
                holder.mitday_start_time.visibility = View.INVISIBLE
                holder.mitday_end_time.visibility = View.INVISIBLE

                holder.open_hour_edit_cv.visibility = View.VISIBLE
                holder.close_hour_edit_cv.visibility = View.VISIBLE
                holder.mitday_end_time_edit_cv.visibility = View.VISIBLE
                holder.mitday_start_time_edit_cv.visibility = View.VISIBLE
            }else{
                holder.active.isEnabled = true

                holder.midTime.visibility = View.INVISIBLE
                holder.editBtnCard.visibility = View.VISIBLE
                holder.okeyBtnCard.visibility = View.INVISIBLE
                holder.cancelBtnCard.visibility = View.INVISIBLE
                holder.open_hour.visibility = View.VISIBLE
                holder.close_hour.visibility = View.VISIBLE

                holder.open_hour_edit_cv.visibility = View.INVISIBLE
                holder.close_hour_edit_cv.visibility = View.INVISIBLE
                holder.mitday_end_time_edit_cv.visibility = View.INVISIBLE
                holder.mitday_start_time_edit_cv.visibility = View.INVISIBLE

                holder.mitday_start_time_edit_tv.visibility = View.VISIBLE
                holder.mitday_end_time_edit_tv.visibility = View.VISIBLE
                holder.mitday_start_time.visibility = View.VISIBLE
                holder.mitday_end_time.visibility = View.VISIBLE
            }
        }else{
            holder.active.isChecked = false
            openHourCard.isEditing = false
            openHourCard.active = false
            holder.active.thumbTintList = ColorStateList.valueOf(Color.parseColor("#B6B2B2"))
            holder.active.trackTintList = ColorStateList.valueOf(Color.parseColor("#CAC5C5"))
            holder.dayTv.setTextColor(Color.parseColor("#717171"))
            holder.mainCardView.setBackgroundColor(Color.parseColor("#E8E6E6"))
            holder.mainCL.visibility = View.INVISIBLE
            holder.closeOrNoActive.visibility = View.VISIBLE
            holder.dayTv.setTextColor(Color.parseColor("#717171"))
        }


    }

    override fun getItemCount(): Int {
        return OpenHoursList.size
    }

    interface ClicksAdapterListener {
        fun editBtnClick(v: View?, position: Int)
        fun turnBtnClick(v: View?, position: Int)
        fun cancelBtnClick(v: View?, position: Int)
        fun saveBtnClick(v: View?, position: Int)

        fun midTimeCBClick(v : View? , position: Int)


        fun openHourCvClick(v: View?, position: Int)
        fun closeHourCvClick(v: View?, position: Int)

        fun closeMidTimeCvClick(v: View?, position: Int)
        fun openMidTimeCvClick(v: View?, position: Int)



    }
}
