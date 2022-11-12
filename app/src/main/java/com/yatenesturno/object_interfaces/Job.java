package com.yatenesturno.object_interfaces;

import android.os.Parcelable;

import java.io.Serializable;
import java.util.List;

public interface Job extends Serializable, Cloneable, Parcelable {

    int getMaxAppsPerDay();

    void setMaxAppsPerDay(int maxAppsPerDay);

    int getMaxAppsSimultaneusly();

    void setMaxAppsSimultaneusly(int maxAppsSimultaneusly);

    CustomUser getEmployee();

    String getId();

    List<DaySchedule> getDaySchedules();

    void setDaySchedules(List<DaySchedule> daySchedules);

    boolean canEdit();

    boolean canChat();

    void setCanChat(boolean c);

    void setCanEdit(boolean c);

    ServiceInstance getServiceInstanceForServiceId(String serviceId);

    DaySchedule getDaySchedule(int dayOfWeek);

    void addDaySchedule(DaySchedule daySchedule);

    Job clone();

    boolean canUserCancelApps();

    void setUserCanCancelApps(boolean canCancel);
}