package com.yatenesturno.custom_views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ToggleButton;

import com.yatenesturno.R;

import java.util.ArrayList;
import java.util.List;

public class DayPicker {

    private final List<ToggleButton> buttons;

    public DayPicker(ViewGroup parent) {
        this.buttons = new ArrayList<>();

        inflateView(parent);
    }

    private void inflateView(ViewGroup parent) {
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup dayPickerView = (ViewGroup) inflater.inflate(R.layout.day_picker_layout, parent, false);
        parent.addView(dayPickerView);

        for (int i = 0; i < dayPickerView.getChildCount(); i++) {
            ToggleButton toggleButton = (ToggleButton) dayPickerView.getChildAt(i);
            buttons.add(toggleButton);
        }
    }

    public List<Integer> getSelectedDays() {
        List<Integer> out = new ArrayList<>();
        for (int i = 0; i < buttons.size(); i++) {
            if (buttons.get(i).isChecked()) {
                out.add(i + 1);
            }
        }

        return out;
    }

    public void clearSelection() {
        for (ToggleButton button : buttons) {
            button.setChecked(false);
        }
    }

    public void selectDay(int day) {
        buttons.get(day - 1).setChecked(true);
    }

}
