package com.yatenesturno.object_interfaces;

import android.os.Parcelable;

import java.io.Serializable;

/**
 *
 */
public interface Service extends Serializable, Parcelable {

    String getJobType();

    String getId();

    String getName();


}