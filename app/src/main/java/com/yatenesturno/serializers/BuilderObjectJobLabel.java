package com.yatenesturno.serializers;

import com.yatenesturno.object_interfaces.Label;
import com.yatenesturno.objects.LabelImpl;

import org.json.JSONException;
import org.json.JSONObject;

public class BuilderObjectJobLabel implements ObjectBuilder<Label> {

    @Override
    public Label build(JSONObject json) throws JSONException {

        LabelImpl label = new LabelImpl();
        label.setColor(json.getString("color"));
        label.setName(json.getString("name"));
        label.setJobId(json.getString("job"));
        label.setId(json.getString("id"));
        return label;
    }

}
