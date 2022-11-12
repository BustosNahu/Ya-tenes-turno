package com.yatenesturno.serializers;

import com.yatenesturno.objects.PlaceCreditsImpl;
import com.yatenesturno.utils.CalendarUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class SerializerPlaceCredits implements Serializer<PlaceCreditsImpl> {

    public JSONObject serialize(PlaceCreditsImpl jobCredits) {
        return new JSONObject();
    }

    public PlaceCreditsImpl deserialize(JSONObject jobCreditsJson) {
        PlaceCreditsImpl out = new PlaceCreditsImpl();

        try {
            out.setCredits(jobCreditsJson.getInt("credits"));
            out.setCurrentCredits(jobCreditsJson.getInt("current_credits"));

            out.setValidFrom(CalendarUtils.parseDate(jobCreditsJson.getString("valid_from")));
            out.setValidUntil(CalendarUtils.parseDate(jobCreditsJson.getString("valid_until")));

            out.setId(jobCreditsJson.getString("id"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return out;
    }

}
