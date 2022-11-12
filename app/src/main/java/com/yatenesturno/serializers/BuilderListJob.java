package com.yatenesturno.serializers;


import com.yatenesturno.object_interfaces.Job;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class BuilderListJob {

    public List<Job> build(JSONArray jobsArray) throws JSONException {

        List<Job> out = new ArrayList<>();

        Job job;
        JSONObject jsonJob;
        BuilderObjectJob builderObjectJob = new BuilderObjectJob();
        for (int i = 0; i < jobsArray.length(); i++) {
            jsonJob = jobsArray.getJSONObject(i);
            job = builderObjectJob.build(jsonJob);
            out.add(job);
        }

        return out;
    }
}