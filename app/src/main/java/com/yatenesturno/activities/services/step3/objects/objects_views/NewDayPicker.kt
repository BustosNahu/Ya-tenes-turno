package com.yatenesturno.activities.services.step3.objects.objects_views

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ToggleButton
import com.yatenesturno.R

class NewDayPicker(parent: ViewGroup) {

    private var buttons: MutableList<CheckBox> = ArrayList()

    init {
        inflateView(parent)
    }

    private fun inflateView(parent: ViewGroup) {
        val context = parent.context
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val dayPickerView = inflater.inflate(R.layout.new_ui_day_picker_layout, parent, false) as ViewGroup
        parent.addView(dayPickerView)

        val cbLunes : CheckBox = dayPickerView.findViewById(R.id.checkBox)
        val cbMartes : CheckBox = dayPickerView.findViewById(R.id.checkBox2)
        val cbMiercoles : CheckBox = dayPickerView.findViewById(R.id.checkBox3)
        val cbJueves : CheckBox = dayPickerView.findViewById(R.id.checkBox4)
        val cbViernes : CheckBox = dayPickerView.findViewById(R.id.checkBox5)
        val cbSabado : CheckBox = dayPickerView.findViewById(R.id.checkBox6)
        val cbDoming : CheckBox = dayPickerView.findViewById(R.id.checkBox7)
        buttons.add(cbLunes)
        buttons.add(cbMartes)
        buttons.add(cbMiercoles)
        buttons.add(cbJueves)
        buttons.add(cbViernes)
        buttons.add(cbSabado)
        buttons.add(cbDoming)
    }

    fun getSelectedDays(): List<Int> {
        val out: MutableList<Int> = ArrayList()
        for (i in buttons.indices) {
            if (buttons[i].isChecked) {
                out.add(i + 1)
            }
        }
        return out
    }

    fun clearSelection() {
        for (button in buttons) {
            button.isChecked = false
        }
    }

    fun selectDay(day: Int) {
        buttons[day - 1].isChecked = true
    }

}