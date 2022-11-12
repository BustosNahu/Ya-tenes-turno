package com.yatenesturno.serializers;


import com.yatenesturno.functionality.ManagerPlace;
import com.yatenesturno.object_interfaces.SubscriptionTemplate;
import com.yatenesturno.object_interfaces.SubscriptionToken;
import com.yatenesturno.objects.SubscriptionTokenImpl;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class BuilderListSubscriptionToken {

    public List<SubscriptionToken> build(JSONArray response) throws JSONException {

        List<SubscriptionToken> out = new ArrayList<>();

        BuilderObjectSubscriptionTemplate builder = new BuilderObjectSubscriptionTemplate();
        BuilderObjectCustomUser builderUser = new BuilderObjectCustomUser();

        JSONObject subTokenObject;
        SubscriptionToken subscriptionToken;
        for (int i = 0; i < response.length(); i++) {
            subTokenObject = response.getJSONObject(i);
            subscriptionToken = new SubscriptionTokenImpl();

            SubscriptionTemplate subscriptionTemplate = builder.build(subTokenObject.getJSONObject("template"));
            subscriptionToken.setTemplate(subscriptionTemplate);

            subscriptionToken.setId(subTokenObject.getString("id"));
            subscriptionToken.setValid(subTokenObject.getBoolean("valid"));
            subscriptionToken.setWarning(
                    subTokenObject.has("employee_with_active_subscription") &&
                            subTokenObject.getBoolean("employee_with_active_subscription")
            );

            subscriptionToken.setUser(builderUser.build(subTokenObject.getJSONObject("user")));
            subscriptionToken.setPlace(ManagerPlace.getInstance().getPlaceById(subTokenObject.getString("place")));

            out.add(subscriptionToken);
        }

        return out;
    }
}