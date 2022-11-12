package com.yatenesturno.object_interfaces;

import android.os.Parcelable;

import java.io.Serializable;
import java.util.Calendar;
import java.util.List;

public interface ServiceInstance extends Serializable, Parcelable {

    void setMaxAppsSimultaneously(int maxAppsSimultaneously);

    boolean isCanBookWithoutCredits();

    void setCanBookWithoutCredits(boolean canBookWithoutCredits);

    boolean isCredits();

    void setCredits(boolean credits);

    boolean isFixedSchedule();

    void setFixedSchedule(boolean fixedSchedule);

    boolean isClassType();

    void setClassType(boolean classType);

    Calendar getStartTime();

    void setStartTime(Calendar startTime);

    Calendar getEndTime();

    void setEndTime(Calendar endTime);

    List<Service> getConcurrentServices();

    void setConcurrentServices(List<Service> otherServiceConcurrency);

    String getId();

    Service getService();

    float getPrice();

    void setPrice(float cost);

    Calendar getDuration();

    void setDuration(Calendar c);

    int getConcurrency();

    void setConcurrency(int concurrency);

    ServiceInstance clone();

    List<Calendar> getClassTimes();

    void setClassTimes(List<Calendar> classTimes);

    Calendar getInterval();

    void setInterval(Calendar interval);

    Calendar getReminderInterval();

    void setReminderInterval(Calendar interval);

    boolean isReminderSet();

    void setReminderSet(boolean isSet);

    boolean isEmergency();

    void setEmergency(boolean emergency);

    int getMaxAppsPerDay();

    void setMaxAppsPerDay(int maxAppsPerDay);

    int getMaxAppsSimultaneously();
}
