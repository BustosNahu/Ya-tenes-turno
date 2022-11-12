package com.yatenesturno.serializers;

import com.yatenesturno.functionality.emergency.EmergencyLocation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class BuilderListEmergencyLocation {

    public static EmergencyLocation parseEmergencyLocation(JSONObject json) throws JSONException {
        EmergencyLocation out = new EmergencyLocation();

        out.setName(json.getString("name"));
        out.setId(json.getString("id"));
        out.setLat((float) json.getDouble("lat"));
        out.setLon((float) json.getDouble("lon"));
        out.setActive(json.getBoolean("active"));

        return out;
    }

    public List<EmergencyLocation> build(JSONObject json) {
        List<EmergencyLocation> out = new ArrayList<>();
        try {
            JSONArray locationsJson = json.getJSONArray("locations");
            for (int i = 0; i < locationsJson.length(); i++) {
                out.add(parseEmergencyLocation(locationsJson.getJSONObject(i)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return out;
    }
}
