package com.yatenesturno.objects;

import android.os.Parcel;
import android.os.Parcelable;

import com.yatenesturno.object_interfaces.JobType;

import java.util.Objects;

/**
 *
 */
public class JobTypeImpl implements JobType {
    public static final Parcelable.Creator<JobTypeImpl> CREATOR = new Parcelable.Creator<JobTypeImpl>() {

        public JobTypeImpl createFromParcel(Parcel in) {
            return new JobTypeImpl(in);
        }

        public JobTypeImpl[] newArray(int size) {
            return new JobTypeImpl[size];
        }
    };
    private final String type;
    private final String id;

    /**
     * Default constructor
     */
    public JobTypeImpl(String id, String type) {
        this.id = id;
        this.type = type;
    }

    public JobTypeImpl(Parcel in) {
        type = in.readString();
        id = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(type);
        dest.writeString(id);
    }

    @Override
    public String getType() {
        return this.type;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JobTypeImpl)) return false;

        JobTypeImpl jobType = (JobTypeImpl) o;
        return Objects.equals(id, jobType.id);
    }

}