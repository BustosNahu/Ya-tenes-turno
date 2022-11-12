package com.yatenesturno.objects;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.yatenesturno.object_interfaces.DaySchedule;
import com.yatenesturno.object_interfaces.ServiceInstance;
import com.yatenesturno.utils.CalendarUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

public class DayScheduleImpl implements DaySchedule {

    public static final Parcelable.Creator<DayScheduleImpl> CREATOR = new Parcelable.Creator<DayScheduleImpl>() {

        public DayScheduleImpl createFromParcel(Parcel in) {
            return new DayScheduleImpl(in);
        }

        public DayScheduleImpl[] newArray(int size) {
            return new DayScheduleImpl[size];
        }
    };

    private final Calendar dayStart;
    private final Calendar dayEnd;
    private final String id;
    private final int dayOfWeek;
    private Calendar pauseStart;
    private Calendar pauseEnd;
    private List<ServiceInstance> serviceInstanceList;

    public DayScheduleImpl(String id, int dayOfWeek, Calendar dayStart, Calendar dayEnd, Calendar pauseStart, Calendar pauseEnd, List<ServiceInstance> serviceInstanceList) {
        this.dayStart = dayStart;
        this.dayEnd = dayEnd;
        this.pauseStart = pauseStart;
        this.pauseEnd = pauseEnd;
        this.id = id;
        this.dayOfWeek = dayOfWeek;
        this.serviceInstanceList = serviceInstanceList;
        if (serviceInstanceList == null) {
            this.serviceInstanceList = new ArrayList<>();
        }
    }

    public DayScheduleImpl(Parcel in) {
        dayStart = CalendarUtils.parseDateTime(in.readString());
        dayEnd = CalendarUtils.parseDateTime(in.readString());
        id = in.readString();
        dayOfWeek = in.readInt();
        pauseStart = CalendarUtils.parseDateTime(in.readString());
        pauseEnd = CalendarUtils.parseDateTime(in.readString());
        serviceInstanceList = (List<ServiceInstance>) in.readValue(ServiceInstance.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(CalendarUtils.formatCalendar(dayStart));
        dest.writeString(CalendarUtils.formatCalendar(dayEnd));
        dest.writeString(id);
        dest.writeInt(dayOfWeek);
        dest.writeString(CalendarUtils.formatCalendar(pauseStart));
        dest.writeString(CalendarUtils.formatCalendar(pauseEnd));
        dest.writeValue(serviceInstanceList);
    }

    @Override
    public Calendar getDayStart() {
        return dayStart;
    }

    @Override
    public Calendar getDayEnd() {
        return dayEnd;
    }

    @Override
    public Calendar getPauseStart() {
        return pauseStart;
    }

    @Override
    public void setPauseStart(Calendar pauseStart) {
        this.pauseStart = pauseStart;
    }

    @Override
    public Calendar getPauseEnd() {
        return pauseEnd;
    }

    @Override
    public void setPauseEnd(Calendar pauseEnd) {
        this.pauseEnd = pauseEnd;
    }

    @Override
    public boolean hasPause() {
        return pauseStart != null;
    }

    @Override
    public List<ServiceInstance> getServiceInstances() {
        return serviceInstanceList;
    }

    @Override
    public ServiceInstance getServiceInstanceForService(String id) {
        for (ServiceInstance p : serviceInstanceList) {
            if (p.getService().getId().equals(id)) {
                return p;
            }
        }

        return null;
    }

    @Override
    public void addServiceInstance(ServiceInstance p) {
        serviceInstanceList.add(p);
    }

    public void removeServiceInstance(ServiceInstance p) {
        serviceInstanceList.remove(p);
    }

    @Override
    public int getDayOfWeek() {
        return dayOfWeek;
    }

    @Override
    public String getId() {
        return id;
    }

    @NonNull
    @Override
    public DayScheduleImpl clone() {
        DayScheduleImpl out = new DayScheduleImpl(id, dayOfWeek, (Calendar) dayStart.clone(), (Calendar) dayEnd.clone(), pauseStart, pauseEnd, null);
        for (ServiceInstance p : serviceInstanceList) {
            out.addServiceInstance(p.clone());
        }
        return out;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DayScheduleImpl that = (DayScheduleImpl) o;

        if (!Objects.equals(id, that.id)) {
            return false;
        }

        if (that.getServiceInstances().size() != getServiceInstances().size()) return false;

        boolean foundEqualServiceInstance;
        for (ServiceInstance s1 : serviceInstanceList) {
            foundEqualServiceInstance = false;
            for (ServiceInstance s2 : that.getServiceInstances()) {
                if (s1.equals(s2)) {
                    foundEqualServiceInstance = true;
                    break;
                }
            }

            if (!foundEqualServiceInstance) {
                return false;
            }
        }

        return dayOfWeek == that.dayOfWeek &&
                calendarEquals(dayStart, that.dayStart) &&
                calendarEquals(dayEnd, that.dayEnd) &&
                calendarEquals(pauseStart, that.pauseStart) &&
                calendarEquals(pauseEnd, that.pauseEnd);
    }

    public boolean calendarEquals(Calendar c1, Calendar c2) {
        if (c1 == null && c2 == null) {
            return true;
        } else if (c1 == null || c2 == null) {
            return false;
        }

        return c1.get(Calendar.HOUR_OF_DAY) == c2.get(Calendar.HOUR_OF_DAY) &&
                c1.get(Calendar.MINUTE) == c2.get(Calendar.MINUTE);
    }
}
