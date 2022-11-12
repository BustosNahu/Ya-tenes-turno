package com.yatenesturno.object_interfaces;

import android.os.Parcelable;

import java.io.Serializable;
import java.util.Calendar;

public interface AppointmentService extends Serializable, Parcelable {

    boolean isBookedWithoutCredits();

    String getID();

    Calendar getTimeStamp();

    ServiceInstance getServiceInstance();

}
