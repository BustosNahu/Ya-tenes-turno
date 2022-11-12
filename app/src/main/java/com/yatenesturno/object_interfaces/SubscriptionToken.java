package com.yatenesturno.object_interfaces;

import android.os.Parcel;
import android.os.Parcelable;

public interface SubscriptionToken extends Parcelable {
    String getId();

    void setId(String id);

    String getToken();

    void setToken(String token);

    Place getPlace();

    void setPlace(Place place);

    CustomUser getUser();

    void setUser(CustomUser user);

    SubscriptionTemplate getTemplate();

    void setTemplate(SubscriptionTemplate template);

    @Override
    void writeToParcel(Parcel dest, int flags);

    void setValid(boolean valid);

    boolean isValid();

    boolean hasWarning();

    void setWarning(boolean warning);
}
