package com.yatenesturno.serializers;

import com.yatenesturno.object_interfaces.JobType;
import com.yatenesturno.objects.JobTypeImpl;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class BuilderListJobType {

    public List<JobType> build(JSONObject response) throws JSONException {

        List<JobType> out = new ArrayList<>();
        JSONArray jobtypesArray = (JSONArray) response.get("jobtypes");

        JobType jobtype;
        for (int i = 0; i < jobtypesArray.length(); i++) {
            JSONObject jsonObject = jobtypesArray.getJSONObject(i);
            jobtype = new JobTypeImpl(jsonObject.getString("id"), jsonObject.getString("type"));
            out.add(jobtype);
        }

        return out;
    }
}
