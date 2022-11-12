package com.yatenesturno.serializers;


import com.yatenesturno.object_interfaces.Service;
import com.yatenesturno.object_interfaces.ServiceInstance;
import com.yatenesturno.objects.ServiceInstanceImpl;
import com.yatenesturno.utils.TimeZoneManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class BuilderObjectServiceInstance implements ObjectBuilder<ServiceInstance>, Serializer<ServiceInstance> {

    @Override
    public ServiceInstance deserialize(JSONObject json) {
        try {
            return build(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public ServiceInstance build(JSONObject jsonServiceInstance) throws JSONException {
        ServiceInstance serviceInstance;

        Service service = new BuilderObjectService().build(jsonServiceInstance.getJSONObject("service"));
        Calendar duration = parseTime(jsonServiceInstance.getString("duration"));

        boolean isClassType = false;
        if (jsonServiceInstance.has("is_class_type") && !jsonServiceInstance.isNull("is_class_type")) {
            isClassType = jsonServiceInstance.getBoolean("is_class_type");
        }

        Calendar reminderInterval = null;
        if (jsonServiceInstance.has("reminder_interval") && !jsonServiceInstance.isNull("reminder_interval")) {
            reminderInterval = parseTime(jsonServiceInstance.getString("reminder_interval"));
        }

        boolean credits = false;
        if (jsonServiceInstance.has("uses_credits")) {
            credits = jsonServiceInstance.getBoolean("uses_credits");
        }
        boolean canBookWithoutCredits = false;
        if (jsonServiceInstance.has("can_book_without_credits")) {
            canBookWithoutCredits = jsonServiceInstance.getBoolean("can_book_without_credits");
        }

        int maxAppsSimultaneously = -1;
        if (jsonServiceInstance.has("max_apps_simultaneously")) {
            maxAppsSimultaneously = jsonServiceInstance.getInt("max_apps_simultaneously");
        }

        int maxAppsPerDay = -1;
        if (jsonServiceInstance.has("max_apps_per_day")) {
            maxAppsPerDay = jsonServiceInstance.getInt("max_apps_per_day");
        }

        boolean intervalSet = jsonServiceInstance.getBoolean("reminder_notification");
        boolean emergency = jsonServiceInstance.getBoolean("emergency");

        if (isClassType) {
            List<Calendar> classTimes = buildClassTimes(jsonServiceInstance.getJSONArray("class_times"));

            serviceInstance = new ServiceInstanceImpl(
                    jsonServiceInstance.getInt("id") + "",
                    service, true,
                    (float) jsonServiceInstance.getDouble("cost"),
                    duration,
                    jsonServiceInstance.getInt("concurrency"),
                    new BuilderConcurrentServices().buildConcurrentServices(
                            jsonServiceInstance.getJSONArray("concurrent_services")),
                    true,
                    null,
                    null,
                    classTimes,
                    null,
                    reminderInterval,
                    intervalSet,
                    emergency,
                    credits,
                    canBookWithoutCredits
            );

        } else {
            Calendar startTime = null, endTime = null;
            if (jsonServiceInstance.has("start_time") && !jsonServiceInstance.isNull("start_time")) {
                startTime = TimeZoneManager.fromUTC(parseTime(jsonServiceInstance.getString("start_time")));
                endTime = TimeZoneManager.fromUTC(parseTime(jsonServiceInstance.getString("end_time")));
            }

            boolean isFixedSchedule = false;
            if (jsonServiceInstance.has("fixed_schedule") && !jsonServiceInstance.isNull("fixed_schedule")) {
                isFixedSchedule = jsonServiceInstance.getBoolean("fixed_schedule");
            }

            Calendar interval = null;
            if (jsonServiceInstance.has("interval") && !jsonServiceInstance.isNull("interval")) {
                interval = parseTime(jsonServiceInstance.getString("interval"));
            }

            serviceInstance = new ServiceInstanceImpl(
                    jsonServiceInstance.getInt("id") + "",
                    service, false,
                    (float) jsonServiceInstance.getDouble("cost"),
                    duration,
                    jsonServiceInstance.getInt("concurrency"),
                    new BuilderConcurrentServices().buildConcurrentServices(
                            jsonServiceInstance.getJSONArray("concurrent_services")),
                    isFixedSchedule,
                    startTime,
                    endTime,
                    null,
                    interval,
                    reminderInterval,
                    intervalSet,
                    emergency,
                    credits,
                    canBookWithoutCredits
            );
        }

        serviceInstance.setMaxAppsPerDay(maxAppsPerDay);
        serviceInstance.setMaxAppsSimultaneously(maxAppsSimultaneously);

        return serviceInstance;
    }

    private List<Calendar> buildClassTimes(JSONArray jsonArrayClassTimes) throws JSONException {
        List<Calendar> out = new ArrayList<>();

        for (int i = 0; i < jsonArrayClassTimes.length(); i++) {
            out.add(TimeZoneManager.fromUTC(parseTime(jsonArrayClassTimes.getString(i))));
        }

        return out;
    }

    // todo use utils
    private Calendar parseTime(String dateString) {
        try {
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateTimeFormat = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH);
            calendar.setTime(dateTimeFormat.parse(dateString));
            return calendar;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public JSONObject serialize(ServiceInstance object) {
        return null;
    }

}
