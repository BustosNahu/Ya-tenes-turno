package com.yatenesturno.object_interfaces;

import android.os.Parcelable;

import java.io.Serializable;

public interface JobType extends Serializable, Parcelable {

    String getType();

    String getId();
}
