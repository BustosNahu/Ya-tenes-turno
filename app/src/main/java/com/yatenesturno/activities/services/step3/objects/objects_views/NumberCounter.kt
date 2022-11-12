package com.yatenesturno.activities.services.step3.objects.objects_views

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import com.yatenesturno.R
import com.yatenesturno.custom_views.NumberCounter

open class NumberCounter(parent: ViewGroup) {

    private var topLimit = 15

    private var counter = 0
    private lateinit var btnIncrease: ImageButton

    private lateinit  var btnDecrease:ImageButton

    private lateinit var labelText: TextView

    private var onChangeListener: ListenerOnChange? = null

    init {
        init()
        inflateView(parent)
        updateUI()
    }

    fun getTopLimit(): Int {
        return topLimit
    }

    fun setTopLimit(topLimit: Int) {
        this.topLimit = topLimit
    }

    fun getCount(): Int {
        return counter
    }

    fun setCount(n: Int) {
        counter = n
        updateUI()
    }

    fun reset() {
        init()
        updateUI()
    }

    protected open fun init() {
        counter = 1
    }

    private fun inflateView(parent: ViewGroup) {
        val context = parent.context
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val timePickerView = inflater.inflate(getLayoutResource(), parent, false) as ViewGroup

        labelText = timePickerView.findViewById(R.id.labelText)
        btnDecrease = timePickerView.findViewById(R.id.btnDecrease)

        btnDecrease.setOnClickListener {
            if (!isAtBottomLimit()) {
                decreaseCounter()
            }
            updateUI()
            notifyListener()
        }
        btnIncrease = timePickerView.findViewById(R.id.btnIncrease)
        btnIncrease.setOnClickListener {
            if (!isAtTopLimit()) {
                increaseCounter()
            }
            updateUI()
            notifyListener()
        }
        parent.addView(timePickerView)
    }

    private fun notifyListener() {
        if (onChangeListener != null) {
            onChangeListener!!.onChange()
        }
    }

    protected fun getLayoutResource(): Int {
        return R.layout.new_ui_number_counter_layout
    }

    protected open fun increaseCounter() {
        counter++
    }

    protected open fun decreaseCounter() {
        counter--
    }

    fun setOnChangeListener(onChangeListener: ListenerOnChange) {
        this.onChangeListener = onChangeListener
    }

    protected fun updateUI() {
        val displayableText = getDisplayableText()
        labelText.text = displayableText
//        if (isAtBottomLimit()) {
//            btnDecrease.visibility = View.INVISIBLE
//        } else {
//            btnDecrease.visibility = View.VISIBLE
//        }
//        if (isAtTopLimit()) {
//            btnIncrease.visibility = View.INVISIBLE
//        } else {
//            btnIncrease.visibility = View.VISIBLE
//        }
    }

    protected open fun getDisplayableText(): String {
        return counter.toString() + ""
    }

    protected open fun isAtBottomLimit(): Boolean {
        return counter <= 1
    }

    protected open fun isAtTopLimit(): Boolean {
        return counter >= topLimit
    }

    interface ListenerOnChange {
        fun onChange()
    }
}