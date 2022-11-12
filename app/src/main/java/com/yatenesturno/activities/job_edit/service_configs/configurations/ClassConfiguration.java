package com.yatenesturno.activities.job_edit.service_configs.configurations;

import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yatenesturno.R;
import com.yatenesturno.activities.job_edit.service_configs.ConfigurationId;
import com.yatenesturno.activities.job_edit.service_configs.ObserverServiceConfiguration;
import com.yatenesturno.activities.job_edit.service_configs.ValidationResult;
import com.yatenesturno.activities.job_edit.service_configs.VisitorServiceConfiguration;
import com.yatenesturno.object_interfaces.ServiceInstance;
import com.yatenesturno.utils.CalendarUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ClassConfiguration extends ServiceConfiguration implements ObserverServiceConfiguration {

    /**
     * Instance variables
     */
    private AdapterClassTimes adapterClassTimes;
    private List<Calendar> classTimes;
    private DaysAndDurationConfiguration daysAndDurationConfiguration;

    /**
     * UI References
     */
    private CheckBox checkBoxIsClass;
    private RecyclerView recyclerViewClassTimes;
    private ViewGroup containerClass;

    public ClassConfiguration(@NonNull Context context) {
        super(context);
    }

    public ClassConfiguration(@NonNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public ConfigurationId getConfigurationId() {
        return ConfigurationId.CLASS;
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.configuration_is_class;
    }

    @Override
    protected void onViewInflated(View view) {
        checkBoxIsClass = view.findViewById(R.id.checkBoxIsClass);
        recyclerViewClassTimes = view.findViewById(R.id.recyclerViewClassTimes);
        containerClass = view.findViewById(R.id.containerClass);
        checkBoxIsClass.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                containerClass.setVisibility(VISIBLE);
            } else {
                classTimes.clear();
                adapterClassTimes.notifyDataSetChanged();
                containerClass.setVisibility(GONE);
            }
            toggleVisibility();
        });

        classTimes = new ArrayList<>();
        initRecyclerViewClassTimes();
    }

    private void toggleVisibility() {
        VisitorClassVisibility visitor = new VisitorClassVisibility(checkBoxIsClass.isChecked());
        runVisitor(visitor);
    }

    private void initRecyclerViewClassTimes() {
        LinearLayoutManager lm = new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false);
        recyclerViewClassTimes.setLayoutManager(lm);

        adapterClassTimes = new AdapterClassTimes();
        recyclerViewClassTimes.setAdapter(adapterClassTimes);
    }

    @Override
    public void initFromServiceInstance(ServiceInstance serviceInstance) {
        if (serviceInstance.isClassType()) {
            if (serviceInstance.getClassTimes() != null) {
                classTimes = new ArrayList<>(serviceInstance.getClassTimes());
            }
        }
        checkBoxIsClass.setChecked(serviceInstance.isClassType());
    }

    @Override
    public void acceptVisitor(VisitorServiceConfiguration visitor) {
        visitor.visit(this);
    }

    @Override
    public void saveState(Bundle bundle) {
        bundle.putBoolean(getConfigurationId().toString(), checkBoxIsClass.isChecked());
        bundle.putSerializable(getConfigurationId().toString() + "_CLASS_TIMES", (ArrayList<Calendar>) classTimes);
    }

    @Override
    public void recoverState(Bundle bundle) {
        checkBoxIsClass.setChecked(bundle.getBoolean(getConfigurationId().toString()));
        classTimes = (List<Calendar>) bundle.getSerializable(getConfigurationId().toString() + "_CLASS_TIMES");
    }

    private void scrollClassTimes() {
        recyclerViewClassTimes.smoothScrollToPosition(classTimes.size());
    }

    private void createNewClassTime(int hourOfDay, int minute) {
        Calendar newClassCalendar = Calendar.getInstance();
        newClassCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        newClassCalendar.set(Calendar.MINUTE, minute);

        classTimes.add(newClassCalendar);
        adapterClassTimes.notifyDataSetChanged();

        scrollClassTimes();
    }

    private void removeClassTime(int position) {
        classTimes.remove(position);
        adapterClassTimes.notifyDataSetChanged();
    }

    @Override
    public ValidationResult validate() {
        if (checkBoxIsClass.isChecked()) {
            if (classTimes.size() == 0) {
                return new ValidationResult(false, R.string.req_select_at_least_one_class_time);
            }
        }
        return new ValidationResult(true);
    }

    @Override
    public void updateUI(Boolean active) {

    }

    @Override
    public void reset() {
        checkBoxIsClass.setChecked(false);
        classTimes.clear();
        adapterClassTimes.notifyDataSetChanged();
        containerClass.setVisibility(GONE);
        hide();

        if (daysAndDurationConfiguration == null) {
            VisitorDuration visitorDuration = new VisitorDuration();
            runVisitor(visitorDuration);
            daysAndDurationConfiguration = visitorDuration.getServiceConfiguration();
            daysAndDurationConfiguration.attach(this);

        }
    }

    private void showNewClassTimeDialog() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                getContext(),
                (view, hourOfDay, minute) -> {
                    if (validateNewClassTime(hourOfDay, minute)) {
                        createNewClassTime(hourOfDay, minute);
                    }
                },
                8, 0, true
        );
        timePickerDialog.setCustomTitle(inflateTitleView());
        timePickerDialog.show();
    }

    private TextView inflateTitleView() {
        TextView textViewTitle = new TextView(getContext());
        textViewTitle.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        textViewTitle.setTextColor(getResources().getColor(R.color.white));
        textViewTitle.setGravity(Gravity.CENTER);
        textViewTitle.setText("Nueva clase");
        textViewTitle.setPadding(10, 20, 10, 10);
        textViewTitle.setTextSize(18);
        return textViewTitle;
    }

    private boolean validateNewClassTime(int hourOfDay, int minute) {
        Calendar duration = daysAndDurationConfiguration.getDuration();

        int durationInMinutes = getTimeInMinutes(duration);
        int newClassTimeInMinutes = hourOfDay * 60 + minute;

        int diff;
        for (Calendar c : classTimes) {
            diff = Math.abs(getTimeInMinutes(c) - newClassTimeInMinutes);
            if (diff == 0) {
                showTimeAlreadySelectedWarning();
                return false;
            } else if (diff < durationInMinutes) {
                showSelectValidTimeWarning();
                return false;
            }
        }

        return true;
    }

    @Override
    public void onUpdate() {
        if (classTimes != null && checkBoxIsClass.isChecked()) {
            if (classTimes.size() > 0) {
                Context context = recyclerViewClassTimes.getContext();
                Toast.makeText(context, context.getString(R.string.reenter_class_times), Toast.LENGTH_SHORT).show();

                classTimes.clear();
                adapterClassTimes.notifyDataSetChanged();
            }
        }
    }

    private int getTimeInMinutes(Calendar calendar) {
        return calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);
    }

    private void showSelectValidTimeWarning() {
        Context context = recyclerViewClassTimes.getContext();
        Toast.makeText(context, context.getString(R.string.select_valid_class_time), Toast.LENGTH_SHORT).show();
    }

    private void showTimeAlreadySelectedWarning() {
        Context context = recyclerViewClassTimes.getContext();
        Toast.makeText(context, context.getString(R.string.already_selected_class_time), Toast.LENGTH_SHORT).show();
    }

    public boolean isClass() {
        return checkBoxIsClass.isChecked();
    }

    public List<Calendar> getClassTimes() {
        return classTimes;
    }

    public static class VisitorDuration extends VisitorServiceConfiguration {
        private DaysAndDurationConfiguration serviceConfiguration;

        public DaysAndDurationConfiguration getServiceConfiguration() {
            return serviceConfiguration;
        }

        @Override
        public void visit(DaysAndDurationConfiguration serviceConfiguration) {
            this.serviceConfiguration = serviceConfiguration;
        }
    }

    private static class VisitorClassVisibility extends VisitorServiceConfiguration {

        private final boolean active;

        public VisitorClassVisibility(boolean active) {
            this.active = active;
        }

        @Override
        public void visit(TimeSlotConfiguration timeSlotConfiguration) {
            if (active) {
                timeSlotConfiguration.hide();
            } else {
                timeSlotConfiguration.show();
            }
        }
    }

     public class AdapterClassTimes extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final int TYPE_ADD_TIME = 0;
        private final int TYPE_TIME_VIEW = 1;

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v;
            switch (viewType) {
                case TYPE_ADD_TIME:
                    v = LayoutInflater.from(getContext())
                            .inflate(R.layout.class_time_add, parent, false);
                    return new ViewHolderTimeAdd(v);

                case TYPE_TIME_VIEW:
                default:
                    v = LayoutInflater.from(getContext())
                            .inflate(R.layout.timestamp_view, parent, false);
                    return new ViewHolderTimeView(v);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            switch (holder.getItemViewType()) {
                case TYPE_ADD_TIME:
                    setAddTimeViewListener((ViewHolderTimeAdd) holder);
                    break;

                case TYPE_TIME_VIEW:
                    ((ViewHolderTimeView) holder).labelTimestamp.setText(CalendarUtils.formatTime(
                            classTimes.get(position)
                    ));
                    setRemoveTimeListener((ViewHolderTimeView) holder, position);
                    break;
            }
        }

        private void setRemoveTimeListener(ViewHolderTimeView holder, final int position) {
            holder.cvRemoveIcon.setOnClickListener( v -> {
                removeClassTime(position);
            });
        }

        private void setAddTimeViewListener(ViewHolderTimeAdd holder) {
            holder.cardViewTimeAdd.setOnClickListener(v -> {
                if (hasSelectedDate(daysAndDurationConfiguration.getDuration())) {
                    showNewClassTimeDialog();
                } else {
                    Context context = recyclerViewClassTimes.getContext();
                    Toast.makeText(context, context.getString(R.string.req_first_select_duration), Toast.LENGTH_SHORT).show();
                }
            });
        }

        private boolean hasSelectedDate(Calendar calendar) {
            return calendar.get(Calendar.MINUTE) != 0 ||
                    calendar.get(Calendar.HOUR_OF_DAY) != 0;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == classTimes.size()) {
                return TYPE_ADD_TIME;
            } else {
                return TYPE_TIME_VIEW;
            }
        }

        @Override
        public int getItemCount() {
            return classTimes.size() + 1;
        }

        private class ViewHolderTimeAdd extends RecyclerView.ViewHolder {

            CardView cardViewTimeAdd;

            public ViewHolderTimeAdd(@NonNull View itemView) {
                super(itemView);

                cardViewTimeAdd = itemView.findViewById(R.id.cardViewTimeAdd);
            }
        }

        private class ViewHolderTimeView extends RecyclerView.ViewHolder {

            TextView labelTimestamp;
            CardView cardViewTimestamp;
            ImageButton cvRemoveIcon;

            public ViewHolderTimeView(@NonNull View itemView) {
                super(itemView);

                cardViewTimestamp = itemView.findViewById(R.id.cardViewTimestamp);
                labelTimestamp = itemView.findViewById(R.id.labelTimeStamp);
                cvRemoveIcon = itemView.findViewById(R.id.removeIcon_cv_timestamp);
            }
        }
    }
}
