package com.yatenesturno.objects;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.yatenesturno.activities.appointment_view.DailyCalendar;
import com.yatenesturno.object_interfaces.Appointment;
import com.yatenesturno.object_interfaces.AppointmentService;
import com.yatenesturno.object_interfaces.CustomUser;
import com.yatenesturno.object_interfaces.Label;
import com.yatenesturno.utils.CalendarUtils;
import com.yatenesturno.view_builder.DailyCalendarView;
import com.yatenesturno.view_builder.DailyCalendarViewAppointment;
import com.yatenesturno.view_builder.InnerViewAppointment;
import com.yatenesturno.view_builder.OverView;
import com.yatenesturno.view_builder.OverviewAppointment;

import java.util.Calendar;
import java.util.List;

public class AppointmentImpl implements Appointment {

    public static final Parcelable.Creator<AppointmentImpl> CREATOR = new Parcelable.Creator<AppointmentImpl>() {

        public AppointmentImpl createFromParcel(Parcel in) {
            return new AppointmentImpl(in);
        }

        public AppointmentImpl[] newArray(int size) {
            return new AppointmentImpl[size];
        }
    };

    private final Calendar date;
    private final CustomUser client;
    private final String id;
    private final List<AppointmentService> serviceInstanceList;
    private Label label;
    private boolean didAttend;
    private String observation;

    public AppointmentImpl(CustomUser client,
                           String id,
                           Calendar date,
                           List<AppointmentService> list,
                           Label label,
                           boolean didAttend, String observation) {
        this.serviceInstanceList = list;
        this.id = id;
        this.client = client;
        this.date = date;
        this.label = label;
        this.didAttend = didAttend;
        this.observation = observation;
    }

    public AppointmentImpl(Parcel in) {
        date = CalendarUtils.parseDateTime(in.readString());
        client = (CustomUser) in.readValue(CustomUser.class.getClassLoader());
        id = in.readString();
        serviceInstanceList = (List<AppointmentService>) in.readValue(AppointmentService.class.getClassLoader());
        label = (LabelImpl) in.readValue(LabelImpl.class.getClassLoader());
        didAttend = in.readInt() == 1;
        observation = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(CalendarUtils.formatCalendar(date));
        dest.writeValue(client);
        dest.writeString(id);
        dest.writeValue(serviceInstanceList);
        dest.writeValue(label);
        dest.writeInt(didAttend ? 1 : 0);
        dest.writeString(observation);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getId() {
        return id;
    }

    @Override
    public String getObservation() {
        return this.observation;
    }

    @Override
    public void setObservation(String obs) {
        this.observation = obs;
    }

    @Override
    public boolean bookedWithoutCredits() {
        for (AppointmentService aService : getAppointmentServices()) {
            if (aService.getServiceInstance().isCredits()) {
                if (aService.isBookedWithoutCredits()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean usesCredits() {
        for (AppointmentService aService : getAppointmentServices()) {
            if (aService.getServiceInstance().isCredits()) {
                return true;
            }
        }
        return false;
    }

    public CustomUser getClient() {
        return client;
    }

    public List<AppointmentService> getAppointmentServices() {
        return serviceInstanceList;
    }

    @Override
    public Calendar getTimeStampStart() {
        return date;
    }

    @Override
    public Calendar getTimeStampEnd() {
        List<AppointmentService> services = getAppointmentServices();
        AppointmentService last = services.get(services.size() - 1);
        Calendar calendarEnd = getStartForService(last);

        addTimes(calendarEnd, last.getServiceInstance().getDuration());

        return calendarEnd;
    }

    @Override
    public String getName() {
        return client.getName();
    }

    @Override
    public Fragment getInnerViewFragment(String placeId, String jobId) {
        return new InnerViewAppointment(this, placeId, jobId);
    }

    @Override
    public OverView getOverView() {
        return new OverviewAppointment();
    }

    @Override
    public DailyCalendarView addSelfViewToContainer(ViewGroup container, DailyCalendar.OnAppointmentClickListener listener) {
        return new DailyCalendarViewAppointment(container, this, listener);
    }

    @Override
    public Label getLabel() {
        return label;
    }

    @Override
    public void setLabel(Label label) {
        this.label = label;
    }

    @Override
    public boolean didAttend() {
        boolean isNotAttendedLabel = label != null && !label.isNotAttendedLabel();
        return didAttend || isNotAttendedLabel;
    }

    @Override
    public void setDidAttend(boolean didAttend) {
        this.didAttend = didAttend;
    }

    private Calendar getStartForService(AppointmentService appointmentService) {
        Calendar serviceInstanceStart = (Calendar) getTimeStampStart().clone();
        List<AppointmentService> aServiceList = getAppointmentServices();

        int it = 0;
        AppointmentService current = aServiceList.get(it);
        while (current != appointmentService) {
            it++;
            addTimes(serviceInstanceStart, current.getServiceInstance().getDuration());
            current = aServiceList.get(it);
        }

        return serviceInstanceStart;
    }

    private void addTimes(Calendar c1, Calendar c2) {
        c1.add(Calendar.HOUR_OF_DAY, c2.get(Calendar.HOUR_OF_DAY));
        c1.add(Calendar.MINUTE, c2.get(Calendar.MINUTE));
    }

}