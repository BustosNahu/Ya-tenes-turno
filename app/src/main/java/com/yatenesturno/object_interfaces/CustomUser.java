package com.yatenesturno.object_interfaces;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 *
 */
public interface CustomUser extends Serializable, Parcelable {

    String getGivenName();

    String getFamilyName();

    String getName();

    String getId();

    String getEmail();

    String getProfilePicUrl();

    @Override
    int describeContents();

    @Override
    void writeToParcel(Parcel dest, int flags);

    int getTrialRemainingDays();

    void setTrialRemainingDays(int days);
}