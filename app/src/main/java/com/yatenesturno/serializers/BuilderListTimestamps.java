package com.yatenesturno.serializers;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class BuilderListTimestamps {

    public static List<Calendar> build(JSONObject response) {
        List<Calendar> out = new ArrayList<>();

        try {
            JSONArray divisions = response.getJSONArray("divisions");

            for (int i = 0; i < divisions.length(); i++) {
                JSONObject timestamp = divisions.getJSONArray(i).getJSONObject(0);
                JSONObject timestamp1 = timestamp.getJSONObject(timestamp.keys().next());

                out.add(parseDateTime(timestamp1.getString("start")));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return out;
    }

    private static Calendar parseDateTime(String dateString) {
        try {
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
            calendar.setTime(dateTimeFormat.parse(dateString));
            return calendar;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }


}
