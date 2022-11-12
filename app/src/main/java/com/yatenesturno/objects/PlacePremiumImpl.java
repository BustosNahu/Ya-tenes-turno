package com.yatenesturno.objects;

import android.os.Parcel;
import android.os.Parcelable;

import com.yatenesturno.object_interfaces.CustomUser;
import com.yatenesturno.object_interfaces.Place;
import com.yatenesturno.object_interfaces.PlacePremium;
import com.yatenesturno.object_interfaces.SubscriptionTemplate;

public class PlacePremiumImpl implements PlacePremium {

    public static final Parcelable.Creator<PlacePremiumImpl> CREATOR = new Parcelable.Creator<PlacePremiumImpl>() {

        public PlacePremiumImpl createFromParcel(Parcel in) {
            return new PlacePremiumImpl(in);
        }

        public PlacePremiumImpl[] newArray(int size) {
            return new PlacePremiumImpl[size];
        }
    };
    private String id;
    private String preapprovalId;
    private CustomUser user;
    private SubscriptionTemplate template;
    private Place place;

    public PlacePremiumImpl() {
    }

    public PlacePremiumImpl(Parcel in) {
        id = in.readString();
        place = in.readParcelable(Place.class.getClassLoader());
        user = in.readParcelable(CustomUser.class.getClassLoader());
        template = in.readParcelable(SubscriptionTemplate.class.getClassLoader());
        preapprovalId = in.readString();
    }

    @Override
    public String getPreapprovalId() {
        return preapprovalId;
    }

    @Override
    public void setPreapprovalId(String preapprovalId) {
        this.preapprovalId = preapprovalId;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeParcelable(place, flags);
        dest.writeParcelable(user, flags);
        dest.writeParcelable(template, flags);
        dest.writeString(preapprovalId);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public Place getPlace() {
        return place;
    }

    @Override
    public void setPlace(Place place) {
        this.place = place;
    }

    @Override
    public CustomUser getUser() {
        return user;
    }

    @Override
    public void setUser(CustomUser user) {
        this.user = user;
    }

    @Override
    public SubscriptionTemplate getTemplate() {
        return template;
    }

    @Override
    public void setTemplate(SubscriptionTemplate template) {
        this.template = template;
    }

    @Override
    public int describeContents() {
        return 0;
    }


}
