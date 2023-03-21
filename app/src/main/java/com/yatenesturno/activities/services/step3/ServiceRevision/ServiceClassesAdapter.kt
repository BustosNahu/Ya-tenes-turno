package com.yatenesturno.activities.services.step3.ServiceRevision

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.yatenesturno.R

class ServiceClassesAdapter(private val mList: List<String>) : RecyclerView.Adapter<ServiceClassesAdapter.ServiceClassesViewHolder>() {


    class ServiceClassesViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView)  {
        val classTime: TextView = itemView.findViewById(R.id.labelTimeStamp)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceClassesViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.timestamp_confirm_step, parent, false)

        return ServiceClassesViewHolder(view)
    }

    override fun onBindViewHolder(holder: ServiceClassesViewHolder, position: Int) {
        val _class = mList[position]

        holder.classTime.text = _class

    }

    override fun getItemCount(): Int {
       return mList.size
    }
}