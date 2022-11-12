package com.yatenesturno.serializers;


import com.yatenesturno.object_interfaces.CustomUser;
import com.yatenesturno.object_interfaces.JobType;
import com.yatenesturno.object_interfaces.Place;
import com.yatenesturno.objects.PlaceImpl;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class BuilderObjectPlace implements ObjectBuilder<Place> {

    @Override
    public Place build(JSONObject json) throws JSONException {

        Place out;

        JSONObject jsonOwner = json.getJSONObject("service_provider");
        CustomUser customUser = new BuilderObjectCustomUser().build(jsonOwner);

        List<JobType> jobTypeList = new BuilderListJobType().build(json);

        String info = null;
        if (json.has("info")) {
            info = json.getString("info");
        }

        out = new PlaceImpl(
                json.getInt("id") + "",
                customUser,
                json.getString("businessname"),
                json.getString("address"),
                json.getString("slogan"),
                json.getString("phonenumber"),
                jobTypeList,
                info
        );

        return out;
    }

}
