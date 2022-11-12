package com.yatenesturno.object_views

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.yatenesturno.R

class ViewService2Adapter (private val serviceObjectList : MutableList<ServiceObject>, var listener : OnSelectServiceListener) :
    RecyclerView.Adapter<ViewService2Adapter.ViewService2ViewHolder>() {

    class ViewService2ViewHolder(itemView : View, listener: OnSelectServiceListener) : RecyclerView.ViewHolder(itemView){
        var onClickListener: OnSelectServiceListener = listener

         val cardViewContainer : CardView = itemView.findViewById(R.id.cardViewContainer)
        val checkBox : CheckBox = itemView.findViewById(R.id.checkBoxService)
        val serviceName : TextView = itemView.findViewById(R.id.labelName)

        init {

            cardViewContainer.setOnClickListener {
                onClickListener.onClick(it, bindingAdapterPosition)
            }
            checkBox.setOnClickListener {
                onClickListener.onClick(it, bindingAdapterPosition)
            }
            serviceName.setOnClickListener {
                onClickListener.onClick(it, bindingAdapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewService2ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.view_service_layout, parent, false)

        return ViewService2ViewHolder(view, listener)
    }


    override fun getItemCount(): Int {
        return serviceObjectList.size
    }



    override fun onBindViewHolder(holder: ViewService2ViewHolder, position: Int) {
        val currentService = serviceObjectList[position]
        holder.serviceName.text = currentService.service.name

        if (currentService.isSelected ){
            holder.checkBox.isChecked = true
            holder.serviceName.setTextColor(Color.parseColor("#FF8672"))

        }else{
            holder.checkBox.isChecked = false
            holder.serviceName.setTextColor(Color.parseColor("#717171"))
        }


        if (currentService.isProvided){
            holder.cardViewContainer.setCardBackgroundColor(Color.parseColor("#FF8672"))
            holder.checkBox.visibility = View.INVISIBLE
            holder.serviceName.setTextColor(Color.parseColor("#FFFFFF"))
        }else{
            holder.cardViewContainer.setCardBackgroundColor(Color.parseColor("#FFFFFF"))
            holder.checkBox.visibility = View.VISIBLE
            holder.serviceName.setTextColor(Color.parseColor("#717171"))

        }


    }

    interface OnSelectServiceListener {
        fun onClick(v: View?, position: Int)
    }
}