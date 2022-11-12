package com.yatenesturno.listeners;

import com.yatenesturno.object_interfaces.Job;

import java.util.List;

public interface RepositoryGetJobListListener {

    void onFetch(List<Job> jobList);

}
