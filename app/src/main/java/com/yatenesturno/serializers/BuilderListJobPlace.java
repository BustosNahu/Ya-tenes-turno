package com.yatenesturno.serializers;

import com.yatenesturno.object_interfaces.Place;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BuilderListJobPlace {

    public List<Place> build(JSONObject json) throws JSONException {

        List<Place> out = new ArrayList<>();
        JSONObject jsonPlaces = json.getJSONObject("places");
        JSONObject jsonJobs = json.getJSONObject("jobs");

        BuilderObjectPlace builderPlace = new BuilderObjectPlace();
        BuilderListJob builderListJob = new BuilderListJob();

        Place place;
        JSONObject jsonSinglePlace;
        JSONArray jobsArray;
        for (Iterator<String> it = jsonPlaces.keys(); it.hasNext(); ) {
            String id = it.next();
            jsonSinglePlace = jsonPlaces.getJSONObject(id);
            place = builderPlace.build(jsonSinglePlace);

            jobsArray = jsonJobs.getJSONArray(id);
            place.getJobList().addAll(builderListJob.build(jobsArray));
            out.add(place);
        }

        return out;
    }

}
