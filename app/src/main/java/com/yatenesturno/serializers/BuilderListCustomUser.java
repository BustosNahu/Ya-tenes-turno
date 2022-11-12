package com.yatenesturno.serializers;

import com.yatenesturno.object_interfaces.CustomUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BuilderListCustomUser {

    public List<CustomUser> build(JSONObject json) throws JSONException {
        ArrayList<CustomUser> customUsersList = new ArrayList<>();
        JSONObject jsonUsers = json.getJSONObject("users");

        BuilderObjectCustomUser builderObjectCustomUser = new BuilderObjectCustomUser();
        for (Iterator<String> itUsers = jsonUsers.keys(); itUsers.hasNext(); ) {
            String userId = itUsers.next();
            JSONObject jsonServiceProvider = jsonUsers.getJSONObject(userId);
            customUsersList.add(builderObjectCustomUser.build(jsonServiceProvider));
        }
        return customUsersList;
    }

}
