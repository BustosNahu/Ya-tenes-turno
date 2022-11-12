package com.yatenesturno.serializers;

import com.yatenesturno.object_interfaces.JobRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BuilderListJobRequest {

    public List<JobRequest> build(JSONObject response) throws JSONException {

        List<JobRequest> out = new ArrayList<>();

        JSONObject jsonJobRequest;

        JobRequest jobRequest;

        for (Iterator<String> itPlaces = response.keys(); itPlaces.hasNext(); ) {
            JSONArray jsonArray = response.getJSONArray(itPlaces.next());

            for (int i = 0; i < jsonArray.length(); i++) {

                jsonJobRequest = jsonArray.getJSONObject(i);
                jobRequest = new BuilderObjectJobRequest().build(jsonJobRequest);

                out.add(jobRequest);
            }

        }

        return out;
    }
}
