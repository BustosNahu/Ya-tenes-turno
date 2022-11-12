package com.yatenesturno.object_interfaces;

import android.os.Parcelable;

import java.io.Serializable;
import java.util.Calendar;
import java.util.List;

public interface DaySchedule extends Serializable, Parcelable {

    int getDayOfWeek();

    String getId();

    Calendar getDayStart();

    Calendar getDayEnd();

    Calendar getPauseStart();

    void setPauseStart(Calendar calendar);

    Calendar getPauseEnd();

    void setPauseEnd(Calendar calendar);

    boolean hasPause();

    List<ServiceInstance> getServiceInstances();

    ServiceInstance getServiceInstanceForService(String id);

    void addServiceInstance(ServiceInstance p);

    void removeServiceInstance(ServiceInstance p);

    DaySchedule clone();
}
