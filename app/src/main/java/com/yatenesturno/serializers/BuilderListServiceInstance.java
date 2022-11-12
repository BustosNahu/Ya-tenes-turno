package com.yatenesturno.serializers;

import com.yatenesturno.object_interfaces.ServiceInstance;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class BuilderListServiceInstance {

    public List<ServiceInstance> build(JSONArray jsonArray) throws JSONException {
        List<ServiceInstance> out = new ArrayList<>();

        BuilderObjectServiceInstance builder = new BuilderObjectServiceInstance();
        for (int i = 0; i < jsonArray.length(); i++) {
            out.add(builder.build(jsonArray.getJSONObject(i)));
        }

        return out;
    }

}
