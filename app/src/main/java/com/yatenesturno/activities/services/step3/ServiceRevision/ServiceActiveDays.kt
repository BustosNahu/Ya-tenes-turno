package com.yatenesturno.activities.services.step3.ServiceRevision

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.yatenesturno.R

class ActiveDaysAdapter(private val mList: List<String>) : RecyclerView.Adapter<ActiveDaysAdapter.ViewHolder>() {

    // create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // inflates the card_view_design view
        // that is used to hold list item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.active_days_layout, parent, false)

        return ViewHolder(view)
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val activeDays = mList[position]

        // sets the image to the imageview from our itemHolder class
        if (mList.last() != mList[position]){
            holder.dayText.text = "$activeDays | "
        }else{
            holder.dayText.text = activeDays

        }

    }

    override fun getItemCount(): Int {
        return mList.size
    }

    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val dayText: TextView = itemView.findViewById(R.id.day)
    }
}