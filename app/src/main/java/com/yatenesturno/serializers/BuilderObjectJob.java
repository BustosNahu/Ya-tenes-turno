package com.yatenesturno.serializers;


import com.yatenesturno.object_interfaces.CustomUser;
import com.yatenesturno.object_interfaces.Job;
import com.yatenesturno.objects.JobImpl;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class BuilderObjectJob implements ObjectBuilder<Job> {


    @Override
    public Job build(JSONObject json) throws JSONException {
        Job out;

        JSONObject jsonJob = json.getJSONObject("job");

        JSONObject jsonServiceProvider = jsonJob.getJSONObject("service_provider");
        CustomUser serviceProvider = new BuilderObjectCustomUser().build(jsonServiceProvider);

        out = new JobImpl(
                jsonJob.getInt("id") + "",
                serviceProvider,
                null,
                jsonJob.getBoolean("edit_mode"),
                jsonJob.getBoolean("chat_mode")
        );
        out.setUserCanCancelApps(jsonJob.getBoolean("user_cancellable_apps"));

        return out;
    }


}
