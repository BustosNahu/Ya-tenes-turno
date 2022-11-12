package com.yatenesturno.serializers;

import com.yatenesturno.object_interfaces.Service;
import com.yatenesturno.objects.ServiceImpl;

import org.json.JSONException;
import org.json.JSONObject;

public class BuilderObjectService implements ObjectBuilder<Service> {
    @Override
    public Service build(JSONObject json) throws JSONException {

        String serviceName = json.getString("name");
        String serviceJobType = json.getString("job_type");
        String id = json.getInt("id") + "";

        return new ServiceImpl(id, serviceJobType, serviceName);
    }
}
