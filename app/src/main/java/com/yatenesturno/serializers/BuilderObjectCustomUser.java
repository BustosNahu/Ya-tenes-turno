package com.yatenesturno.serializers;

import com.yatenesturno.object_interfaces.CustomUser;
import com.yatenesturno.objects.CustomUserImpl;
import com.yatenesturno.utils.CalendarUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

public class BuilderObjectCustomUser implements ObjectBuilder<CustomUser> {

    @Override
    public CustomUser build(JSONObject json) throws JSONException {

        CustomUser out;

        out = new CustomUserImpl(
                json.getInt("id") + "",
                json.getString("given_name"),
                json.getString("family_name"),
                json.getString("email"),
                json.getString("profile_pic")
        );


        return out;
    }


    public String serialize(CustomUser user) {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", user.getId());
            jsonObject.put("given_name", user.getGivenName());
            jsonObject.put("family_name", user.getFamilyName());
            jsonObject.put("email", user.getEmail());
            jsonObject.put("profile_pic", user.getProfilePicUrl());
            jsonObject.put("has_trial", user.getTrialRemainingDays());

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }
}
