package com.yatenesturno.objects;

import android.os.Parcel;
import android.os.Parcelable;

import com.yatenesturno.object_interfaces.Service;

import java.util.Objects;

/**
 *
 */
public class ServiceImpl implements Service {

    public static final Parcelable.Creator<ServiceImpl> CREATOR = new Parcelable.Creator<ServiceImpl>() {

        public ServiceImpl createFromParcel(Parcel in) {
            return new ServiceImpl(in);
        }

        public ServiceImpl[] newArray(int size) {
            return new ServiceImpl[size];
        }
    };

    private final String jobtype;
    private final String id;
    private final String name;

    public ServiceImpl(String id, String jobtype, String name) {
        this.jobtype = jobtype;
        this.id = id;
        this.name = name;
    }

    public ServiceImpl(Parcel in) {
        jobtype = in.readString();
        id = in.readString();
        name = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(jobtype);
        dest.writeString(id);
        dest.writeString(name);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public String getJobType() {
        return jobtype;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServiceImpl that = (ServiceImpl) o;
        return Objects.equals(id, that.id);
    }


}