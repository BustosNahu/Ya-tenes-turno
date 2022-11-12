package com.yatenesturno.object_interfaces;

import android.os.Parcelable;

import java.io.Serializable;

public interface JobRequest extends Serializable, Parcelable {

    Place getPlace();

    CustomUser getCustomUser();

}
