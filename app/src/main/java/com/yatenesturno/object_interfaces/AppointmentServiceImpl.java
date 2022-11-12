package com.yatenesturno.object_interfaces;

import android.os.Parcel;
import android.os.Parcelable;

import com.yatenesturno.utils.CalendarUtils;

import java.util.Calendar;

public class AppointmentServiceImpl implements AppointmentService {

    public static final Parcelable.Creator<AppointmentServiceImpl> CREATOR = new Parcelable.Creator<AppointmentServiceImpl>() {

        public AppointmentServiceImpl createFromParcel(Parcel in) {
            return new AppointmentServiceImpl(in);
        }

        public AppointmentServiceImpl[] newArray(int size) {
            return new AppointmentServiceImpl[size];
        }
    };

    private final String id;
    private final Calendar timestamp;
    private final ServiceInstance serviceInstance;

    @Override
    public boolean isBookedWithoutCredits() {
        return bookedWithoutCredits;
    }

    private final boolean bookedWithoutCredits;

    public AppointmentServiceImpl(String id, Calendar timestamp, ServiceInstance serviceInstance, boolean bookedWithoutCredits) {
        this.id = id;
        this.timestamp = timestamp;
        this.serviceInstance = serviceInstance;
        this.bookedWithoutCredits = bookedWithoutCredits;
    }

    public AppointmentServiceImpl(Parcel in) {
        id = in.readString();
        timestamp = CalendarUtils.parseDateTime(in.readString());
        serviceInstance = (ServiceInstance) in.readValue(ServiceInstance.class.getClassLoader());
        bookedWithoutCredits = in.readInt() == 1;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(CalendarUtils.formatCalendar(timestamp));
        dest.writeValue(serviceInstance);
        dest.writeInt(bookedWithoutCredits ? 1 : 0);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public String getID() {
        return null;
    }

    @Override
    public Calendar getTimeStamp() {
        return timestamp;
    }

    @Override
    public ServiceInstance getServiceInstance() {
        return serviceInstance;
    }
}
