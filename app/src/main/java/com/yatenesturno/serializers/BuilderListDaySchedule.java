package com.yatenesturno.serializers;

import com.yatenesturno.object_interfaces.DaySchedule;
import com.yatenesturno.object_interfaces.ServiceInstance;
import com.yatenesturno.objects.DayScheduleImpl;
import com.yatenesturno.utils.TimeZoneManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BuilderListDaySchedule {

    private Map<String, ServiceInstance> mapStringServiceInstance;

    public List<DaySchedule> build(JSONObject jsonObject) throws JSONException {
        JSONObject jsonDaySchedulesList = jsonObject.getJSONObject("day_schedules");
        JSONObject jsonServiceInstanceList = jsonObject.getJSONObject("service_instances");

        buildServiceInstances(jsonServiceInstanceList);
        List<DaySchedule> dayScheduleList = buildDaySchedules(jsonDaySchedulesList);

        return dayScheduleList;
    }

    private void buildServiceInstances(JSONObject jsonServiceInstanceList) throws JSONException {
        mapStringServiceInstance = new HashMap<>();
        for (Iterator<String> it = jsonServiceInstanceList.keys(); it.hasNext(); ) {
            String id = it.next();
            JSONObject jsonServiceInstance = jsonServiceInstanceList.getJSONObject(id);

            ServiceInstance serviceInstance = new BuilderObjectServiceInstance().build(jsonServiceInstance);

            mapStringServiceInstance.put(id, serviceInstance);
        }
    }

    private List<DaySchedule> buildDaySchedules(JSONObject jsonDaySchedulesList) throws JSONException {
        List<DaySchedule> dayScheduleList = new ArrayList<>();
        DaySchedule daySchedule;
        for (Iterator<String> it = jsonDaySchedulesList.keys(); it.hasNext(); ) {
            String dayOfWeek = it.next();
            JSONObject jsonDaySchedule = jsonDaySchedulesList.getJSONObject(dayOfWeek);
            daySchedule = buildDaySchedule(jsonDaySchedule);

            for (ServiceInstance si : mapStringServiceInstance.values()) {
                if (si.isEmergency()) {
                    daySchedule.getServiceInstances().add(si);
                }
            }
            dayScheduleList.add(daySchedule);
        }
        return dayScheduleList;
    }

    private DaySchedule buildDaySchedule(JSONObject json) throws JSONException {

        JSONObject jsonDaySchedule = json.getJSONObject("day_schedule");
        JSONArray jsonProvidesList = json.getJSONArray("provides");

        Calendar dayStart, dayEnd, pauseStart = null, pauseEnd = null;
        dayStart = parseUTCDate(jsonDaySchedule.getString("day_start"));
        dayEnd = parseUTCDate(jsonDaySchedule.getString("day_end"));

        if (!jsonDaySchedule.isNull("pause_start")) {
            pauseStart = parseUTCDate(jsonDaySchedule.getString("pause_start"));
            pauseEnd = parseUTCDate(jsonDaySchedule.getString("pause_end"));
        }

        List<ServiceInstance> providedServiceInstances = getServiceInstancesList(jsonProvidesList);

        return new DayScheduleImpl(
                jsonDaySchedule.getInt("id") + "",
                jsonDaySchedule.getInt("day_of_week"),
                dayStart,
                dayEnd,
                pauseStart,
                pauseEnd,
                providedServiceInstances
        );
    }

    private List<ServiceInstance> getServiceInstancesList(JSONArray jsonProvidesList) throws JSONException {
        List<ServiceInstance> providedServiceInstances = new ArrayList<>();
        for (int i = 0; i < jsonProvidesList.length(); i++) {
            int serviceInstanceId = jsonProvidesList.getInt(i);
            providedServiceInstances.add(mapStringServiceInstance.get(serviceInstanceId + ""));
        }
        return providedServiceInstances;
    }

    private Calendar parseUTCDate(String dateString) {
        try {
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateTimeFormat = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH);
            calendar.setTime(dateTimeFormat.parse(dateString));
            calendar = TimeZoneManager.fromUTC(calendar);
            return calendar;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

}
