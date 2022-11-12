package com.yatenesturno.objects;

import android.os.Parcel;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.yatenesturno.activities.appointment_view.DailyCalendar;
import com.yatenesturno.object_interfaces.Appointment;
import com.yatenesturno.object_interfaces.AppointmentService;
import com.yatenesturno.object_interfaces.Label;
import com.yatenesturno.object_interfaces.ServiceInstance;
import com.yatenesturno.utils.CalendarUtils;
import com.yatenesturno.view_builder.DailyCalendarView;
import com.yatenesturno.view_builder.DailyCalendarViewAppointmentClass;
import com.yatenesturno.view_builder.InnerViewAppointmentClass;
import com.yatenesturno.view_builder.OverView;
import com.yatenesturno.view_builder.OverviewAppointmentClass;

import java.util.Calendar;
import java.util.List;

/**
 *
 */
public class AppointmentClassImpl implements Appointment {

    public static final Creator<AppointmentClassImpl> CREATOR = new Creator<AppointmentClassImpl>() {

        public AppointmentClassImpl createFromParcel(Parcel in) {
            return new AppointmentClassImpl(in);
        }

        public AppointmentClassImpl[] newArray(int size) {
            return new AppointmentClassImpl[size];
        }
    };

    private final Calendar date;
    private final ServiceInstance serviceInstance;
    private final List<AppointmentImpl> compositionApps;

    public AppointmentClassImpl(
            Calendar date,
            ServiceInstance serviceInstance,
            List<AppointmentImpl> concurrentApps) {
        this.serviceInstance = serviceInstance;
        this.date = date;
        this.compositionApps = concurrentApps;
    }

    public AppointmentClassImpl(Parcel in) {
        date = CalendarUtils.parseDateTime(in.readString());
        serviceInstance = in.readParcelable(ServiceInstance.class.getClassLoader());
        compositionApps = (List<AppointmentImpl>) in.readValue(AppointmentImpl.class.getClassLoader());
    }

    public ServiceInstance getServiceInstance() {
        return serviceInstance;
    }

    public List<AppointmentImpl> getCompositionApps() {
        return compositionApps;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(CalendarUtils.formatCalendar(date));
        dest.writeParcelable(serviceInstance, flags);
        dest.writeValue(compositionApps);
    }

    @Override
    public Calendar getTimeStampStart() {
        return date;
    }

    @Override
    public Calendar getTimeStampEnd() {
        return addTimes(date, serviceInstance.getDuration());
    }

    private Calendar addTimes(Calendar c1, Calendar c2) {
        Calendar out = (Calendar) c1.clone();

        out.add(Calendar.HOUR_OF_DAY, c2.get(Calendar.HOUR_OF_DAY));
        out.add(Calendar.MINUTE, c2.get(Calendar.MINUTE));

        return out;
    }

    @Override
    public String getName() {
        return serviceInstance.getService().getName();
    }

    @Override
    public Fragment getInnerViewFragment(String placeId, String jobId) {
        return new InnerViewAppointmentClass(this, placeId, jobId);
    }

    @Override
    public OverView getOverView() {
        return new OverviewAppointmentClass();
    }

    @Override
    public DailyCalendarView addSelfViewToContainer(ViewGroup container, DailyCalendar.OnAppointmentClickListener listener) {
        return new DailyCalendarViewAppointmentClass(container, this, listener);
    }

    @Override
    public Label getLabel() {
        return null;
    }

    @Override
    public void setLabel(Label label) {

    }

    @Override
    public boolean didAttend() {
        return false;
    }

    @Override
    public void setDidAttend(boolean didAttend) {

    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public String getObservation() {
        return null;
    }

    @Override
    public void setObservation(String obs) {

    }

    @Override
    public boolean bookedWithoutCredits() {
        for (AppointmentImpl app : getCompositionApps()) {
            for (AppointmentService appServ : app.getAppointmentServices()) {
                if (appServ.isBookedWithoutCredits()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean usesCredits() {
        for (AppointmentImpl app : getCompositionApps()) {
            for (AppointmentService appServ : app.getAppointmentServices()) {
                if (appServ.getServiceInstance().isCredits()) {
                    return true;
                }
            }
        }
        return false;
    }


    public int getPeopleCount() {
        return compositionApps.size();
    }
}