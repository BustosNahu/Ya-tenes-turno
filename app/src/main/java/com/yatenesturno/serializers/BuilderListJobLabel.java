package com.yatenesturno.serializers;

import com.yatenesturno.object_interfaces.Label;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class BuilderListJobLabel {

    public List<Label> buildLabelList(JSONObject json) {
        try {
            JSONArray labelsArray = json.getJSONArray("content");

            List<Label> out = new ArrayList<>();
            BuilderObjectJobLabel builderObjectLabel = new BuilderObjectJobLabel();
            for (int i = 0; i < labelsArray.length(); i++) {
                out.add(builderObjectLabel.build(labelsArray.getJSONObject(i)));
            }

            return out;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

}
