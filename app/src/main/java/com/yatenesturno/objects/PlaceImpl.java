package com.yatenesturno.objects;

import android.os.Parcel;
import android.os.Parcelable;

import com.yatenesturno.object_interfaces.CustomUser;
import com.yatenesturno.object_interfaces.Job;
import com.yatenesturno.object_interfaces.JobType;
import com.yatenesturno.object_interfaces.Place;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PlaceImpl implements Place {
    public static final Parcelable.Creator<PlaceImpl> CREATOR = new Parcelable.Creator<PlaceImpl>() {

        public PlaceImpl createFromParcel(Parcel in) {
            return new PlaceImpl(in);
        }

        public PlaceImpl[] newArray(int size) {
            return new PlaceImpl[size];
        }
    };

    private final String slogan;
    private final String id;
    private final String businessName;
    private final CustomUser owner;
    private final String address;
    private final String phonenumber;
    private final List<Job> jobList;
    private final List<JobType> jobTypes;
    private String info;
    private boolean hasTrial;

    public PlaceImpl(String id, CustomUser owner, String businessName, String address, String slogan, String phonenumber, List<JobType> jobTypes, String info) {
        this.id = id;
        this.owner = owner;
        this.businessName = businessName;
        this.address = address;
        this.phonenumber = phonenumber;
        this.slogan = slogan;
        this.jobTypes = jobTypes;
        this.info = info;

        jobList = new ArrayList<>();
    }

    public PlaceImpl(Parcel in) {
        slogan = in.readString();
        id = in.readString();
        businessName = in.readString();
        owner = (CustomUser) in.readValue(CustomUser.class.getClassLoader());
        address = in.readString();
        phonenumber = in.readString();
        jobTypes = (List<JobType>) in.readValue(JobType.class.getClassLoader());
        jobList = (List<Job>) in.readValue(Job.class.getClassLoader());
    }

    @Override
    public List<JobType> getJobTypes() {
        return jobTypes;
    }

    @Override
    public Job getJobById(String id) {
        if (jobList != null) {
            for (Job job : jobList) {
                if (job.getId().equals(id)) {
                    return job;
                }
            }
        }
        return null;
    }

    @Override
    public String getCategory() {
        return info;
    }

    @Override
    public void setCategory(String info) {
        this.info = info;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(slogan);
        dest.writeString(id);
        dest.writeString(businessName);
        dest.writeValue(owner);
        dest.writeString(address);
        dest.writeString(phonenumber);
        dest.writeValue(jobTypes);
        dest.writeValue(jobList);
    }

    public String getId() {
        return id;
    }

    public String getBusinessName() {
        return businessName;
    }

    public String getAddress() {
        return address;
    }

    @Override
    public List<Job> getJobList() {
        return jobList;
    }

    @Override
    public CustomUser getOwner() {
        return owner;
    }

    @Override
    public String getSlogan() {
        return slogan;
    }

    @Override
    public String getPhoneNumber() {
        return phonenumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlaceImpl that = (PlaceImpl) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(businessName, that.businessName) &&
                Objects.equals(owner, that.owner) &&
                Objects.equals(address, that.address);
    }
}
