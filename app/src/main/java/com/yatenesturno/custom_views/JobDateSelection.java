package com.yatenesturno.custom_views;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.yatenesturno.R;
import com.yatenesturno.functionality.DayScheduleManager;
import com.yatenesturno.functionality.days_off.DayOff;
import com.yatenesturno.functionality.days_off.DaysOffManager;
import com.yatenesturno.object_interfaces.DaySchedule;
import com.yatenesturno.object_interfaces.Job;
import com.yatenesturno.object_interfaces.ServiceInstance;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Displays date selection with disabled days
 */
public class JobDateSelection {

    private final Job job;
    private final ViewGroup parent;
    private final boolean pastDaysSelectable;
    private LinearLayout weekRowContainer;
    private MonthPicker monthPicker;
    private Calendar selectedDate;
    private LoadingOverlay loadingOverlay;
    private Map<ViewGroup, Calendar> mapViewCalendar;
    private OnDateSelectedListener onDateSelectedListener;
    private List<DayOff> daysOff;
    private boolean[] daysWithNoServiceProvided;
    private CardView root;

    public JobDateSelection(Job job, ViewGroup parent, Calendar selectedDate, boolean pastDaysSelectable) {
        this.job = job;
        this.parent = parent;
        this.pastDaysSelectable = pastDaysSelectable;
        this.selectedDate = selectedDate;

        inflateView();
        initMonthsSpinner();

        populateMonth();
        fetchDaysOff();
        fetchDaySchedules();

    }

    private void inflateView() {
        root = (CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.date_selection_layout, parent, false);
        parent.addView(root);
        weekRowContainer = root.findViewById(R.id.weekRowContainer);
    }

    private void fetchDaySchedules() {
        if (job != null) {
            DayScheduleManager.getInstance().getDaySchedules(job.getId(), dayScheduleList -> {
                job.setDaySchedules(dayScheduleList);
                disableDaysWithNoServiceProvided();
            });
        }
    }

    private void disableDaysWithNoServiceProvided() {
        if (daysWithNoServiceProvided == null) {
            daysWithNoServiceProvided = getDaysWithNoServicesProvided();
        }
        markDaysWithNoService();
    }

    /**
     * Returns an array indexed by day of week
     * If there is no service provided in a given day,
     * then the days corresponding index will be set to true
     *
     * @return array indexed by day of week containing true if
     * the day index does not provide a service
     */
    private boolean[] getDaysWithNoServicesProvided() {
        boolean[] disabledDays = new boolean[8];

        DaySchedule ds;
        for (int dayOfWeek = 1; dayOfWeek < disabledDays.length; dayOfWeek++) {
            ds = job.getDaySchedule(dayOfWeek);
            if (ds != null && ds.getServiceInstances().size() > 0) {
                boolean foundServiceThatIsNotEmergency = false;
                for (ServiceInstance si : ds.getServiceInstances()) {
                    if (!si.isEmergency()) {
                        disabledDays[dayOfWeek] = false;
                        foundServiceThatIsNotEmergency = true;
                        break;
                    }
                }
                disabledDays[dayOfWeek] = !foundServiceThatIsNotEmergency;
            } else {
                disabledDays[dayOfWeek] = true;
            }

        }

        return disabledDays;
    }

    private void markDaysWithNoService() {
        for (Map.Entry<ViewGroup, Calendar> v : mapViewCalendar.entrySet()) {
            if (!providesServicesInDay(v.getValue())) {
                markViewAsNoServiceProvided(v.getKey());
            }
        }
    }

    private boolean providesServicesInDay(Calendar calendar) {
        if (daysWithNoServiceProvided != null) {
            return !daysWithNoServiceProvided[calendar.get(Calendar.DAY_OF_WEEK)];
        }
        return true;
    }

    private void markViewAsNoServiceProvided(ViewGroup key) {
        setDateTextColor(key, R.color.almost_black);
    }

    public void setLoadingOverlay(LoadingOverlay loadingOverlay) {
        this.loadingOverlay = loadingOverlay;
    }

    public void setOnDateSelectedListener(OnDateSelectedListener onDateSelectedListener) {
        this.onDateSelectedListener = onDateSelectedListener;
    }

    public void addDayOff(Calendar calendar) {
        DayOff dayOff = new DayOff();
        dayOff.setParsedDate(calendar);
        daysOff.add(dayOff);
        markView(getViewForCalendar(calendar));
    }

    public void removeDayOff(Calendar calendar) {
        DayOff dayOff = findDayOff(calendar);
        daysOff.remove(dayOff);
        markView(getViewForCalendar(calendar));
    }

    private void populateMonth() {
        weekRowContainer.removeAllViews();

        mapViewCalendar = new HashMap<>();

        ViewGroup currentWeekRow = newWeekRow();
        addPastDays(currentWeekRow);

        final Calendar currentCalendar = (Calendar) monthPicker.getCurrMonth().clone();

        currentCalendar.set(Calendar.DAY_OF_MONTH, 1);
        int maxMonthDay = getMaxMonthDay();

        CalendarDay currentCalendarDay;
        Calendar today = Calendar.getInstance();
        LayoutInflater layoutInflater = LayoutInflater.from(root.getContext());
        while (currentCalendar.get(Calendar.DAY_OF_MONTH) <= maxMonthDay && currentCalendar.get(Calendar.MONTH) == monthPicker.getCurrMonth().get(Calendar.MONTH)) {

            if (currentWeekRow.getChildCount() == 7) {
                currentWeekRow = newWeekRow();
            }

            currentCalendarDay = new CalendarDay(currentCalendar);
            ViewGroup dayView = inflateDayView(currentWeekRow);
            currentCalendarDay.populateView(dayView, layoutInflater);
            mapViewCalendar.put(dayView, (Calendar) currentCalendar.clone());

            if (compareCalendars(currentCalendarDay.getCalendar(), today) < 0) {
                if (pastDaysSelectable) {
                    dayView.setOnClickListener(this::onDayClicked);
                } else {
                    dayView.setEnabled(false);
                }
                setDateTextColor(dayView, R.color.darker_grey);
            } else {
                dayView.setOnClickListener(this::onDayClicked);
            }

            if (compareCalendars(today, currentCalendar) == 0) {
                setDateTextColor(dayView, R.color.colorPrimary);
            }

            currentCalendar.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    private void setDateTextColor(ViewGroup dayView, int colorId) {
        final TextView dayNumberTextView = dayView.findViewById(R.id.textViewDayNumber);
        dayNumberTextView.setTextColor(root.getContext().getColor(colorId));
        dayNumberTextView.setTypeface(null, Typeface.NORMAL);
    }

    private void initMonthsSpinner() {
        monthPicker = new MonthPicker(root.findViewById(R.id.containerMonthPicker));

        if (!pastDaysSelectable) {
            monthPicker.disablePastDays();
        }
        monthPicker.setCurrMonth((Calendar) selectedDate.clone());

        monthPicker.setOnChangeListener(new NumberCounter.ListenerOnChange() {
            @Override
            public void onChange() {
                int selectedMonth = monthPicker.getCurrMonth().get(Calendar.MONTH);
                if (hasChangedMonth(selectedMonth)) {
                    changeMonth(selectedMonth);
                }
            }

            private boolean hasChangedMonth(int position) {
                return selectedDate.get(Calendar.MONTH) != position;
            }
        });
    }

    private void changeMonth(int position) {
        selectedDate = Calendar.getInstance();
        selectedDate.set(Calendar.DAY_OF_MONTH, 1);
        selectedDate.set(Calendar.MONTH, position);
        selectedDate.set(Calendar.YEAR, monthPicker.getCurrMonth().get(Calendar.YEAR));

        if (!pastDaysSelectable && compareCalendars(selectedDate, Calendar.getInstance()) < 0) {
            selectedDate = Calendar.getInstance();
        }

        populateMonth();

        if (job != null) {
            markDaysOff();
            disableDaysWithNoServiceProvided();
        }
        markView(getViewForCalendar(selectedDate));
    }

    public int compareCalendars(Calendar c1, Calendar c2) {
        if (c1.get(Calendar.YEAR) != c2.get(Calendar.YEAR))
            return c1.get(Calendar.YEAR) - c2.get(Calendar.YEAR);
        if (c1.get(Calendar.MONTH) != c2.get(Calendar.MONTH))
            return c1.get(Calendar.MONTH) - c2.get(Calendar.MONTH);
        return c1.get(Calendar.DAY_OF_MONTH) - c2.get(Calendar.DAY_OF_MONTH);
    }

    private Map.Entry<ViewGroup, Calendar> getViewForCalendar(Calendar calendar) {
        for (Map.Entry<ViewGroup, Calendar> v : mapViewCalendar.entrySet()) {
            if (compareCalendars(calendar, v.getValue()) == 0) {
                return v;
            }
        }
        return null;
    }

    private int getMaxMonthDay() {
        return monthPicker.getCurrMonth().getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    private ViewGroup inflateDayView(ViewGroup weekRow) {
        RelativeLayout relativeLayout = (RelativeLayout) LayoutInflater.from(root.getContext()).inflate(R.layout.monthly_day_layout_day_off, weekRow, false);
        weekRow.addView(relativeLayout);

        return relativeLayout;
    }

    private ViewGroup newWeekRow() {
        LinearLayout weekRowView = (LinearLayout) LayoutInflater.from(root.getContext()).inflate(R.layout.monthly_weekrow_layout, weekRowContainer, false);
        weekRowContainer.addView(weekRowView);
        return weekRowView;
    }

    private void addPastDays(ViewGroup weekRow) {
        int monthWeekDayStart = getWeekDayMonthStart();
        for (int i = 1; i < monthWeekDayStart; i++) {
            LinearLayout invalidDayView = (LinearLayout) LayoutInflater.from(root.getContext()).inflate(R.layout.monthly_invalid_day, weekRow, false);
            weekRow.addView(invalidDayView);
        }
    }

    private int getWeekDayMonthStart() {
        Calendar calendar = (Calendar) monthPicker.getCurrMonth().clone();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        return calendar.get(Calendar.DAY_OF_WEEK);
    }

    private void fetchDaysOff() {
        if (job != null) {
            showLoadingOverlay();
            DaysOffManager daysOffManager = DaysOffManager.getInstance();
            daysOffManager.getDaysOff(job, daysOff -> {
                hideLoadingOverlay();
                this.daysOff = daysOff;
                markDaysOff();
                markView(getViewForCalendar(selectedDate));
            });
        }

    }

    private void showLoadingOverlay() {
        if (loadingOverlay != null) {
            loadingOverlay.show();
        }
    }

    private void hideLoadingOverlay() {
        if (loadingOverlay != null) {
            loadingOverlay.hide();
        }
    }

    private void markDaysOff() {
        if (daysOff != null) {
            for (DayOff dayOff : daysOff) {
                Map.Entry<ViewGroup, Calendar> view = getViewForCalendar(dayOff.getDate());
                if (view != null) {
                    markDateAsDisabled(view);
                }
            }
        }
    }

    private void markView(Map.Entry<ViewGroup, Calendar> v) {
        Context context = root.getContext();
        TextView dayNumberTextView = v.getKey().findViewById(R.id.textViewDayNumber);
        dayNumberTextView.setBackground(context.getDrawable(R.drawable.day_picker_oval_white));

        if (compareCalendars(v.getValue(), Calendar.getInstance()) == 0) {
            setDateTextColor(v.getKey(), R.color.colorPrimary);
            dayNumberTextView.setTypeface(null, Typeface.BOLD);
        } else {
            setDateTextColor(v.getKey(), R.color.black);
        }

        updateCalendarDate(v.getValue());
    }

    private void unMarkView(Map.Entry<ViewGroup, Calendar> v) {
        Context context = root.getContext();

        ViewGroup dayView = v.getKey();
        TextView dayNumberTextView = dayView.findViewById(R.id.textViewDayNumber);

        Calendar today = Calendar.getInstance();
        dayNumberTextView.setBackgroundColor(context.getColor(R.color.black));

        if (!providesServicesInDay(v.getValue())) {
            markViewAsNoServiceProvided(v.getKey());
        } else {
            int comparisonResult = compareCalendars(v.getValue(), today);
            if (comparisonResult < 0) {
                dayView.setEnabled(pastDaysSelectable);
                setDateTextColor(dayView, R.color.darker_grey);
            } else if (comparisonResult > 0) {
                setDateTextColor(dayView, R.color.white);
            } else {
                setDateTextColor(dayView, R.color.colorPrimary);
                dayNumberTextView.setTypeface(null, Typeface.BOLD);
            }
        }


        updateCalendarDate(v.getValue());
    }

    private void updateCalendarDate(Calendar selectedDay) {
        DayOff dayOff = findDayOff(selectedDay);

        if (dayOff != null) {
            markDateAsDisabled(getViewForCalendar(selectedDay));
        } else {
            markDateAsEnabled(getViewForCalendar(selectedDay));
        }
    }

    private DayOff findDayOff(Calendar date) {
        if (daysOff != null) {
            for (DayOff dayOff : daysOff) {
                if (dayOff.hasDate(date)) return dayOff;
            }
        }
        return null;
    }

    private void markDateAsDisabled(Map.Entry<ViewGroup, Calendar> v) {
        v.getKey().findViewById(R.id.ivOffIndicator).setVisibility(View.VISIBLE);
    }

    private void markDateAsEnabled(Map.Entry<ViewGroup, Calendar> v) {
        v.getKey().findViewById(R.id.ivOffIndicator).setVisibility(View.GONE);
    }

    private void onDayClicked(View view) {
        unMarkView(getViewForCalendar(selectedDate));
        selectedDate = mapViewCalendar.get(view);
        markView(getViewForCalendar(selectedDate));
        notifyListener();
    }

    public Calendar getSelectedDate() {
        return selectedDate;
    }

    public void setSelectedDate(Calendar calendar) {
        selectedDate = calendar;

        monthPicker.setCurrMonth((Calendar) selectedDate.clone());
        populateMonth();

        if (job != null) {
            markDaysOff();
            disableDaysWithNoServiceProvided();
        }
        markView(getViewForCalendar(selectedDate));
    }

    private void notifyListener() {
        if (onDateSelectedListener != null) {
            onDateSelectedListener.onDateSelected((Calendar) selectedDate.clone());
        }
    }

    public interface OnDateSelectedListener {
        void onDateSelected(Calendar date);
    }
}
