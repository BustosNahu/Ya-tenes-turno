package com.yatenesturno.serializers;


import com.yatenesturno.functionality.ManagerPlace;
import com.yatenesturno.object_interfaces.PlacePremium;
import com.yatenesturno.object_interfaces.SubscriptionTemplate;
import com.yatenesturno.objects.PlacePremiumImpl;
import com.yatenesturno.user_auth.UserManagement;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class BuilderListPlacePremium {

    public List<PlacePremium> build(JSONArray response) throws JSONException {

        List<PlacePremium> out = new ArrayList<>();

        BuilderObjectSubscriptionTemplate builder = new BuilderObjectSubscriptionTemplate();

        JSONObject placePremiumJson;
        PlacePremium placePremium;
        for (int i = 0; i < response.length(); i++) {
            placePremiumJson = response.getJSONObject(i);
            placePremium = new PlacePremiumImpl();

            SubscriptionTemplate subscriptionTemplate = builder.build(placePremiumJson.getJSONObject("template"));
            placePremium.setTemplate(subscriptionTemplate);

            placePremium.setId(placePremiumJson.getString("id"));

            placePremium.setUser(UserManagement.getInstance().getUser());
            placePremium.setPlace(ManagerPlace.getInstance().getPlaceById(placePremiumJson.getString("place")));

            placePremium.setPreapprovalId(placePremiumJson.getString("mp_preapproval_id"));

            out.add(placePremium);
        }

        return out;
    }
}