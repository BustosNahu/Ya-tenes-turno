package com.yatenesturno.custom_views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.yatenesturno.R;

public class NumberCounter {

    private int topLimit = 15;

    private int counter;
    private ImageButton btnIncrease, btnDecrease;
    private TextView labelText;
    private ListenerOnChange onChangeListener;

    public NumberCounter(ViewGroup parent) {
        init();
        inflateView(parent);
        updateUI();
    }

    public int getTopLimit() {
        return topLimit;
    }

    public void setTopLimit(int topLimit) {
        this.topLimit = topLimit;
    }

    public int getCount() {
        return counter;
    }

    public void setCount(int n) {
        counter = n;
        updateUI();
    }

    public void reset() {
        init();
        updateUI();
    }

    protected void init() {
        counter = 1;
    }

    private void inflateView(ViewGroup parent) {
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup timePickerView = (ViewGroup) inflater.inflate(getLayoutResource(), parent, false);

        labelText = timePickerView.findViewById(R.id.labelText);

        btnDecrease = timePickerView.findViewById(R.id.btnDecrease);
        btnDecrease.setOnClickListener(v -> {
            if (!isAtBottomLimit()) {
                decreaseCounter();
            }
            updateUI();

            notifyListener();
        });

        btnIncrease = timePickerView.findViewById(R.id.btnIncrease);
        btnIncrease.setOnClickListener(v -> {
            if (!isAtTopLimit()) {
                increaseCounter();
            }
            updateUI();

            notifyListener();
        });

        parent.addView(timePickerView);
    }

    private void notifyListener() {
        if (onChangeListener != null) {
            onChangeListener.onChange();
        }
    }

    protected int getLayoutResource() {
        return R.layout.number_counter_layout;
    }

    protected void increaseCounter() {
        counter++;
    }

    protected void decreaseCounter() {
        counter--;
    }

    public void setOnChangeListener(ListenerOnChange onChangeListener) {
        this.onChangeListener = onChangeListener;
    }

    protected void updateUI() {
        String displayableText = getDisplayableText();
        labelText.setText(displayableText);

        if (isAtBottomLimit()) {
            btnDecrease.setVisibility(View.INVISIBLE);
        } else {
            btnDecrease.setVisibility(View.VISIBLE);
        }

        if (isAtTopLimit()) {
            btnIncrease.setVisibility(View.INVISIBLE);
        } else {
            btnIncrease.setVisibility(View.VISIBLE);
        }
    }

    protected String getDisplayableText() {
        return counter + "";
    }

    protected boolean isAtBottomLimit() {
        return counter <= 1;
    }

    protected boolean isAtTopLimit() {
        return counter >= topLimit;
    }

    public interface ListenerOnChange {
        void onChange();
    }


}
