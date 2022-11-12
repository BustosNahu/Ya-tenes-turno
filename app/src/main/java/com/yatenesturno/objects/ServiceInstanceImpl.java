package com.yatenesturno.objects;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yatenesturno.object_interfaces.Service;
import com.yatenesturno.object_interfaces.ServiceInstance;
import com.yatenesturno.utils.CalendarUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

/**
 * Service provied by a job. Contains info about its provided setup such as price, duration...
 */
public class ServiceInstanceImpl implements ServiceInstance {

    public static final Parcelable.Creator<ServiceInstanceImpl> CREATOR = new Parcelable.Creator<ServiceInstanceImpl>() {

        public ServiceInstanceImpl createFromParcel(Parcel in) {
            return new ServiceInstanceImpl(in);
        }

        public ServiceInstanceImpl[] newArray(int size) {
            return new ServiceInstanceImpl[size];
        }
    };

    private final String id;
    private final Service service;
    private boolean canBookWithoutCredits;
    private boolean fixedSchedule;
    private Calendar startTime, endTime;
    private float price;
    private Calendar duration;
    private int concurrency;
    private List<Service> otherServiceConcurrency;
    private boolean isClassType;
    private List<Calendar> classTimes;
    private Calendar interval;
    private Calendar reminderInterval;
    private boolean reminderSet;
    private boolean emergency;
    private boolean credits;
    private int maxAppsPerDay;
    private int maxAppsSimultaneously;

    public ServiceInstanceImpl(String id, Service service) {
        this.id = id;
        this.service = service;
    }

    public ServiceInstanceImpl(String id,
                               Service service,
                               boolean isClassType,
                               float price,
                               Calendar duration,
                               int concurrency,
                               List<Service> otherServiceConcurrency,
                               boolean fixedSchedule,
                               Calendar startTime,
                               Calendar endTime,
                               List<Calendar> classTimes,
                               Calendar interval,
                               Calendar reminderInterval,
                               boolean reminderSet,
                               boolean emergency,
                               boolean credits,
                               boolean canBookWithoutCredits) {

        this.id = id;
        this.isClassType = isClassType;
        this.service = service;
        this.price = price;
        this.duration = duration;
        this.concurrency = concurrency;
        this.otherServiceConcurrency = otherServiceConcurrency;
        this.fixedSchedule = fixedSchedule;
        this.startTime = startTime;
        this.endTime = endTime;
        this.classTimes = classTimes;
        this.interval = interval;
        this.reminderInterval = reminderInterval;
        this.reminderSet = reminderSet;
        this.emergency = emergency;
        this.credits = credits;
        this.canBookWithoutCredits = canBookWithoutCredits;
    }

    public ServiceInstanceImpl(Parcel in) {
        id = in.readString();
        isClassType = in.readInt() == 1;
        service = (Service) in.readValue(Service.class.getClassLoader());
        price = in.readFloat();
        duration = CalendarUtils.parseDateTime(in.readString());
        concurrency = in.readInt();
        otherServiceConcurrency = (List<Service>) in.readValue(Service.class.getClassLoader());
        fixedSchedule = in.readInt() == 1;
        startTime = CalendarUtils.parseDateTime(in.readString());
        endTime = CalendarUtils.parseDateTime(in.readString());
        interval = CalendarUtils.parseDateTime(in.readString());

        if (isClassType) {
            Type calendarListType = new TypeToken<ArrayList<Calendar>>() {
            }.getType();
            classTimes = new Gson().fromJson(in.readString(), calendarListType);
        }

        reminderInterval = CalendarUtils.parseDateTime(in.readString());
        reminderSet = in.readInt() == 1;
        emergency = in.readInt() == 1;
        credits = in.readInt() == 1;
        canBookWithoutCredits = in.readInt() == 1;

        maxAppsPerDay = in.readInt();
        maxAppsSimultaneously = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeInt(isClassType ? 1 : 0);
        dest.writeValue(service);
        dest.writeFloat(price);
        dest.writeString(CalendarUtils.formatCalendar(duration));
        dest.writeInt(concurrency);
        dest.writeValue(otherServiceConcurrency);
        dest.writeInt(fixedSchedule ? 1 : 0);

        dest.writeString(CalendarUtils.formatCalendar(startTime));
        dest.writeString(CalendarUtils.formatCalendar(endTime));
        dest.writeString(CalendarUtils.formatCalendar(interval));

        if (isClassType) {
            dest.writeString(new Gson().toJson(classTimes));
        }

        dest.writeString(CalendarUtils.formatCalendar(reminderInterval));
        dest.writeInt(reminderSet ? 1 : 0);

        dest.writeInt(emergency ? 1 : 0);
        dest.writeInt(credits ? 1 : 0);
        dest.writeInt(canBookWithoutCredits ? 1 : 0);

        dest.writeInt(maxAppsPerDay);
        dest.writeInt(maxAppsSimultaneously);
    }

    @Override
    public int getMaxAppsPerDay() {
        return maxAppsPerDay;
    }

    @Override
    public void setMaxAppsPerDay(int maxAppsPerDay) {
        this.maxAppsPerDay = maxAppsPerDay;
    }

    @Override
    public int getMaxAppsSimultaneously() {
        return maxAppsSimultaneously;
    }

    @Override
    public void setMaxAppsSimultaneously(int maxAppsSimultaneously) {
        this.maxAppsSimultaneously = maxAppsSimultaneously;
    }

    @Override
    public boolean isCanBookWithoutCredits() {
        return canBookWithoutCredits;
    }

    @Override
    public void setCanBookWithoutCredits(boolean canBookWithoutCredits) {
        this.canBookWithoutCredits = canBookWithoutCredits;
    }

    @Override
    public boolean isCredits() {
        return credits;
    }

    @Override
    public void setCredits(boolean credits) {
        this.credits = credits;
    }


    @Override
    public boolean isFixedSchedule() {
        return fixedSchedule;
    }

    @Override
    public void setFixedSchedule(boolean fixedSchedule) {
        this.fixedSchedule = fixedSchedule;
    }

    @Override
    public boolean isClassType() {
        return this.isClassType;
    }

    @Override
    public void setClassType(boolean classType) {
        isClassType = classType;
    }

    @Override
    public Calendar getStartTime() {
        return startTime;
    }

    @Override
    public void setStartTime(Calendar startTime) {
        this.startTime = startTime;
    }

    @Override
    public Calendar getEndTime() {
        return endTime;
    }

    @Override
    public void setEndTime(Calendar endTime) {
        this.endTime = endTime;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public List<Service> getConcurrentServices() {
        return otherServiceConcurrency;
    }

    @Override
    public void setConcurrentServices(List<Service> otherServiceConcurrency) {
        this.otherServiceConcurrency = otherServiceConcurrency;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Service getService() {
        return service;
    }

    @Override
    public float getPrice() {
        return price;
    }

    @Override
    public void setPrice(float cost) {
        this.price = cost;
    }

    @Override
    public Calendar getDuration() {
        return duration;
    }

    @Override
    public void setDuration(Calendar duration) {
        this.duration = duration;
    }

    @Override
    public int getConcurrency() {
        return concurrency;
    }

    @Override
    public void setConcurrency(int concurrency) {
        this.concurrency = concurrency;
    }

    @Override
    public ServiceInstanceImpl clone() {
        ServiceInstanceImpl out =
                new ServiceInstanceImpl(
                        id,
                        service, isClassType,
                        price,
                        (Calendar) duration.clone(),
                        concurrency,
                        new ArrayList<>(otherServiceConcurrency),
                        fixedSchedule,
                        startTime != null ? (Calendar) startTime.clone() : null,
                        endTime != null ? (Calendar) endTime.clone() : null,
                        cloneClassTimes(),
                        interval,
                        reminderInterval,
                        reminderSet,
                        emergency,
                        credits,
                        canBookWithoutCredits
                );

        out.setMaxAppsPerDay(maxAppsPerDay);
        out.setMaxAppsSimultaneously(maxAppsSimultaneously);

        return out;
    }

    private List<Calendar> cloneClassTimes() {
        if (classTimes != null) {
            List<Calendar> out = new ArrayList<>();

            for (Calendar c : classTimes) {
                out.add((Calendar) c.clone());
            }
            return out;
        }
        return null;
    }

    @Override
    public List<Calendar> getClassTimes() {
        return this.classTimes;
    }

    @Override
    public void setClassTimes(List<Calendar> classTimes) {
        this.classTimes = classTimes;
    }

    @Override
    public Calendar getInterval() {
        return this.interval;
    }

    @Override
    public void setInterval(Calendar interval) {
        this.interval = interval;
    }

    @Override
    public Calendar getReminderInterval() {
        return this.reminderInterval;
    }

    @Override
    public void setReminderInterval(Calendar interval) {
        this.reminderInterval = interval;
    }

    @Override
    public boolean isReminderSet() {
        return this.reminderSet;
    }

    @Override
    public void setReminderSet(boolean isSet) {
        this.reminderSet = isSet;
    }

    @Override
    public boolean isEmergency() {
        return emergency;
    }

    @Override
    public void setEmergency(boolean emergency) {
        this.emergency = emergency;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServiceInstanceImpl that = (ServiceInstanceImpl) o;

        if (!Objects.equals(id, that.id)) {
            return false;
        }

        if (isClassType != that.isClassType) {
            return false;
        }

        for (Service service1 : otherServiceConcurrency) {
            boolean foundEqualService = false;
            for (Service service2 : that.otherServiceConcurrency) {
                if (service1.equals(service2)) {
                    foundEqualService = true;
                    break;
                }
            }

            if (!foundEqualService) {
                return false;
            }
        }

        if (classTimes != null && that.classTimes != null) {
            if (classTimes.size() != that.classTimes.size()) {
                return false;
            }

            for (Calendar c : classTimes) {
                boolean foundEqualClass = false;
                for (Calendar c1 : that.classTimes) {
                    if (calendarEquals(c1, c)) {
                        foundEqualClass = true;
                        break;
                    }
                }

                if (!foundEqualClass) {
                    return false;
                }
            }
        } else if (classTimes != null || that.classTimes != null) {
            return false;
        }

        return Float.compare(that.price, price) == 0 &&
                reminderSet == that.reminderSet &&
                calendarEquals(reminderInterval, that.reminderInterval) &&
                concurrency == that.concurrency &&
                Objects.equals(service, that.service) &&
                calendarEquals(duration, that.duration) &&
                calendarEquals(interval, that.interval) &&
                fixedSchedule == that.fixedSchedule &&
                calendarEquals(startTime, that.startTime) &&
                calendarEquals(endTime, that.endTime) &&
                emergency == that.emergency &&
                canBookWithoutCredits == that.canBookWithoutCredits &&
                credits == that.credits &&
                maxAppsSimultaneously == that.maxAppsSimultaneously &&
                maxAppsPerDay == that.maxAppsPerDay;
    }

    public boolean calendarEquals(Calendar c1, Calendar c2) {
        if (c1 != null && c2 != null) {
            return c1.get(Calendar.HOUR_OF_DAY) == c2.get(Calendar.HOUR_OF_DAY) &&
                    c1.get(Calendar.MINUTE) == c2.get(Calendar.MINUTE);
        }

        return c1 == null && c2 == null;
    }
}
