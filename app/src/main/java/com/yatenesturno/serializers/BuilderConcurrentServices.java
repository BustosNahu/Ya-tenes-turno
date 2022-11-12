package com.yatenesturno.serializers;

import com.yatenesturno.object_interfaces.Service;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class BuilderConcurrentServices {

    public List<Service> buildConcurrentServices(JSONArray servicesArray) {
        List<Service> out = new ArrayList<>();

        try {
            BuilderObjectService builderObjectService = new BuilderObjectService();
            for (int i = 0; i < servicesArray.length(); i++) {
                out.add(builderObjectService.build(servicesArray.getJSONObject(i)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return out;
    }

}
