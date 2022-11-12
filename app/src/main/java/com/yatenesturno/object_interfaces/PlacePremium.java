package com.yatenesturno.object_interfaces;

import android.os.Parcelable;

public interface PlacePremium extends Parcelable {
    String getPreapprovalId();

    void setPreapprovalId(String preapprovalId);

    String getId();

    void setId(String id);

    Place getPlace();

    void setPlace(Place placeId);

    CustomUser getUser();

    void setUser(CustomUser user);

    SubscriptionTemplate getTemplate();

    void setTemplate(SubscriptionTemplate template);
}
