package com.yatenesturno.objects;

import android.os.Parcel;
import android.os.Parcelable;

import com.yatenesturno.object_interfaces.CustomUser;
import com.yatenesturno.object_interfaces.JobRequest;
import com.yatenesturno.object_interfaces.Place;

public class JobRequestImpl implements JobRequest {

    public static final Parcelable.Creator<JobRequestImpl> CREATOR = new Parcelable.Creator<JobRequestImpl>() {

        public JobRequestImpl createFromParcel(Parcel in) {
            return new JobRequestImpl(in);
        }

        public JobRequestImpl[] newArray(int size) {
            return new JobRequestImpl[size];
        }
    };
    private final Place place;
    private final CustomUser customUser;


    public JobRequestImpl(Place place, CustomUser customUser) {
        this.place = place;
        this.customUser = customUser;
    }

    public JobRequestImpl(Parcel in) {
        place = (Place) in.readValue(Place.class.getClassLoader());
        customUser = (CustomUser) in.readValue(CustomUser.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(place);
        dest.writeValue(customUser);
    }

    @Override
    public Place getPlace() {
        return place;
    }

    @Override
    public CustomUser getCustomUser() {
        return customUser;
    }


}
