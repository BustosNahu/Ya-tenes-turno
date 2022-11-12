package com.yatenesturno.serializers;


import com.yatenesturno.object_interfaces.SubscriptionTemplate;
import com.yatenesturno.objects.SubscriptionTemplateImpl;

import org.json.JSONException;
import org.json.JSONObject;

public class BuilderObjectSubscriptionTemplate implements ObjectBuilder<SubscriptionTemplate> {

    @Override
    public SubscriptionTemplate build(JSONObject json) throws JSONException {

        SubscriptionTemplate subscriptionTemplate = new SubscriptionTemplateImpl();

        subscriptionTemplate.setUrl(json.getString("url"));
        subscriptionTemplate.setEmployeeCount(json.getInt("employee_count"));
        subscriptionTemplate.setDescription(json.getString("description"));
        subscriptionTemplate.setId(json.getString("id"));
        subscriptionTemplate.setIsCombo(json.getBoolean("is_combo"));

        return subscriptionTemplate;
    }

}
