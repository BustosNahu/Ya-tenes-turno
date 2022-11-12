package com.yatenesturno.object_interfaces;

import android.os.Parcel;
import android.os.Parcelable;

public interface SubscriptionTemplate extends Parcelable {
    String getDescription();

    void setDescription(String description);

    boolean isCombo();

    void setIsCombo(boolean combo);

    int getEmployeeCount();

    void setEmployeeCount(int employeeCount);

    String getId();

    void setId(String id);

    String getUrl();

    void setUrl(String url);

    @Override
    int describeContents();

    @Override
    void writeToParcel(Parcel dest, int flags);
}
