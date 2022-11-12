package com.yatenesturno.custom_views;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.yatenesturno.R;
import com.yatenesturno.object_interfaces.Job;
import com.yatenesturno.utils.CustomAlertDialogBuilder;

import java.util.Calendar;

/**
 * Button to display date selection in a dialog
 * <p>
 * makes use of JobDateSelection
 */
public class JobDateSelectionButton extends LinearLayoutCompat {

    /**
     * UI References
     */
    private ViewGroup root;
    private CardView btnSelectDate;
    private AppCompatTextView labelDate;
    private JobDateSelection jobDateSelection;
    private AlertDialog alertDialogDateSelection;
    private ConstraintLayout holderDatePicker;

    /**
     * Instance variables
     */
    private Job job;
    private JobDateSelection.OnDateSelectedListener listener;
    private Calendar selectedDate;
    private boolean pastDaysSelectable;

    public JobDateSelectionButton(@NonNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        super(context, attrs);

        inflateView(context);
    }

    public JobDateSelectionButton(@NonNull Context context) {
        this(context, null);
    }

    private void inflateView(Context context) {
        root = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.job_date_selection_button, this, true);
        btnSelectDate = root.findViewById(R.id.btnSelectDate);
        labelDate = root.findViewById(R.id.labelDate);
    }

    private void init() {
        if (selectedDate == null) {
            selectedDate = Calendar.getInstance();
        }
        updateDateBtnText();
        btnSelectDate.setOnClickListener(view -> showDatePicker());

        new Handler(Looper.myLooper()).postDelayed(() -> {
            holderDatePicker = (ConstraintLayout) LayoutInflater.from(root.getContext()).inflate(R.layout.date_selection_dialog, null, false);

            jobDateSelection = new JobDateSelection(job, holderDatePicker.findViewById(R.id.jobDateSelection), selectedDate, pastDaysSelectable);
            holderDatePicker.findViewById(R.id.btnConfirm).setOnClickListener(view -> {
                selectedDate = jobDateSelection.getSelectedDate();
                alertDialogDateSelection.dismiss();
                updateDateBtnText();
                notifyListener();
            });

        }, 200);

    }

    private void showDatePicker() {
        jobDateSelection.setSelectedDate(selectedDate);
        if (alertDialogDateSelection == null) {
            CustomAlertDialogBuilder builder = new CustomAlertDialogBuilder(getContext());
            builder.setView(holderDatePicker);
            alertDialogDateSelection = builder.show();
        } else {
            alertDialogDateSelection.show();
        }
    }

    public void updateDateBtnText() {
        int year = selectedDate.get(Calendar.YEAR);
        int month = selectedDate.get(Calendar.MONTH);
        int day = selectedDate.get(Calendar.DAY_OF_MONTH);

        labelDate.setText(day + "/" + (month + 1) + "/" + year);
    }

    private void notifyListener() {
        if (listener != null) {
            listener.onDateSelected(selectedDate);
        }
    }

    public void setListener(@NonNull JobDateSelection.OnDateSelectedListener listener) {
        this.listener = listener;
    }

    /**
     * Only after job is set, init Views
     *
     * @param job job to find active days
     */
    public void setJob(@NonNull Job job, boolean pastDaysSelectable) {
        this.pastDaysSelectable = pastDaysSelectable;
        this.job = job;
        init();
    }

    public Calendar getDate() {
        return selectedDate;
    }

    public void setDate(Calendar selectedDate) {
        this.selectedDate = selectedDate;
        updateDateBtnText();
        if (jobDateSelection != null) {
            jobDateSelection.setSelectedDate(selectedDate);
        }
    }
}
