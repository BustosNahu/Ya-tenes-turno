package com.yatenesturno.object_interfaces;

import android.os.Parcelable;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.yatenesturno.activities.appointment_view.DailyCalendar;
import com.yatenesturno.view_builder.DailyCalendarView;
import com.yatenesturno.view_builder.OverView;

import java.io.Serializable;
import java.util.Calendar;

public interface Appointment extends Serializable, Parcelable {

    Calendar getTimeStampStart();

    Calendar getTimeStampEnd();

    String getName();

    Fragment getInnerViewFragment(String placeId, String jobId);

    OverView getOverView();

    DailyCalendarView addSelfViewToContainer(ViewGroup container, DailyCalendar.OnAppointmentClickListener listener);

    Label getLabel();

    void setLabel(Label label);

    boolean didAttend();

    void setDidAttend(boolean didAttend);

    String getId();

    String getObservation();

    void setObservation(String obs);

    boolean bookedWithoutCredits();

    boolean usesCredits();
}
