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
import com.yatenesturno.database.localDb.SchedulePlaceTime
import com.yatenesturno.objects.open_hour_cardView.OpenHoursObject
import eu.davidea.flexibleadapter.items.IFilterable


class OhRevisionAdapter(private val OpenHoursList : MutableList<SchedulePlaceTime>) : RecyclerView.Adapter<OhRevisionAdapter.OhRevisionViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OhRevisionViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.opening_hours_revision_items_rv, parent, false)

        return OhRevisionViewHolder(view)
    }



    class OhRevisionViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView)  {
        val activeDayTv : TextView = itemView.findViewById(R.id.rv_opening_hours_revision_active_day_tv)
        val inactiveDayTv : TextView = itemView.findViewById(R.id.rv_opening_hours_revision_inactive_day_tv)
        val openHour : TextView = itemView.findViewById(R.id.openHour_tv)
        val closeHour : TextView = itemView.findViewById(R.id.closeHour_tv)
        val noActiveTimeLayout : CardView = itemView.findViewById(R.id.close_no_active_cv)
        val activeTimeLayout : ConstraintLayout = itemView.findViewById(R.id.openHours_constraint_layout)


    }

    override fun onBindViewHolder(holder: OhRevisionViewHolder, position: Int) {
        val openHourCard = OpenHoursList[position]
        if (openHourCard.active){
            holder.noActiveTimeLayout.visibility = View.GONE
            holder.activeTimeLayout.visibility = View.VISIBLE
            holder.activeDayTv.text = openHourCard.day
            holder.openHour.text = openHourCard.openTime
            holder.closeHour.text = openHourCard.closeTime
        }else{
            holder.activeTimeLayout.visibility = View.INVISIBLE
            holder.noActiveTimeLayout.visibility = View.VISIBLE
            holder.inactiveDayTv.text = openHourCard.day
        }

    }

    override fun getItemCount(): Int {
        return OpenHoursList.size
    }

}
