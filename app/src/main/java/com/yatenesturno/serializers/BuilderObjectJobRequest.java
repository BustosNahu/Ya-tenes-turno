package com.yatenesturno.serializers;


import com.yatenesturno.object_interfaces.CustomUser;
import com.yatenesturno.object_interfaces.JobRequest;
import com.yatenesturno.object_interfaces.Place;
import com.yatenesturno.objects.JobRequestImpl;

import org.json.JSONException;
import org.json.JSONObject;

public class BuilderObjectJobRequest implements ObjectBuilder<JobRequest> {

    @Override
    public JobRequest build(JSONObject json) throws JSONException {

        JobRequest out;

        JSONObject jsonPlace = json.getJSONObject("place");
        JSONObject jsonServiceProvider = json.getJSONObject("service_provider");

        Place place = new BuilderObjectPlace().build(jsonPlace);
        CustomUser serviceProvider = new BuilderObjectCustomUser().build(jsonServiceProvider);

        out = new JobRequestImpl(place, serviceProvider);

        return out;
    }
}
