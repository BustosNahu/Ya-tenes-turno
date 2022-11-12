package com.yatenesturno.object_interfaces;

import android.os.Parcelable;

import java.io.Serializable;
import java.util.List;

/**
 *
 */
public interface Place extends Serializable, Parcelable {

    String getId();

    String getBusinessName();

    String getAddress();

    List<Job> getJobList();

    CustomUser getOwner();

    String getSlogan();

    String getPhoneNumber();

    List<JobType> getJobTypes();

    Job getJobById(String id);

    String getCategory();

    void setCategory(String category);

}