package com.yatenesturno.serializers;

import com.yatenesturno.object_interfaces.Service;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BuilderListService {

    public List<Service> build(JSONObject jsonObject) {
        List<Service> out = new ArrayList<>();
        BuilderObjectService builder = new BuilderObjectService();

        try {
            for (Iterator<String> it = jsonObject.keys(); it.hasNext(); ) {
                JSONArray servicesArray = jsonObject.getJSONArray(it.next());
                for (int i = 0; i < servicesArray.length(); i++) {
                    out.add(builder.build(servicesArray.getJSONObject(i)));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return out;
    }
}
