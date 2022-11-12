package com.yatenesturno.serializers;

import com.yatenesturno.object_interfaces.CustomUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class BuilderListCoJobs {

    public Map<String, CustomUser> build(JSONObject json) throws JSONException {
        Map<String, CustomUser> mapJobIdUser = new HashMap<>();

        JSONObject jsonJobs = json.getJSONObject("jobs");

        BuilderObjectCustomUser builderObjectCustomUser = new BuilderObjectCustomUser();
        for (Iterator<String> it = jsonJobs.keys(); it.hasNext(); ) {
            String jobId = it.next();
            JSONObject jsonObject = jsonJobs.getJSONObject(jobId);
            CustomUser customUser = builderObjectCustomUser.build(jsonObject);
            mapJobIdUser.put(jobId, customUser);
        }
        return mapJobIdUser;
    }

}
