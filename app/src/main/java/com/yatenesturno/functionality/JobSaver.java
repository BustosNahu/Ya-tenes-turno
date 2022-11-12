package com.yatenesturno.functionality;

import android.util.Log;

import com.yatenesturno.Constants;
import com.yatenesturno.database.DatabaseDjangoWrite;
import com.yatenesturno.listeners.DatabaseCallback;
import com.yatenesturno.listeners.RepositorySaveListener;
import com.yatenesturno.object_interfaces.DaySchedule;
import com.yatenesturno.object_interfaces.Job;
import com.yatenesturno.object_interfaces.Service;
import com.yatenesturno.object_interfaces.ServiceInstance;
import com.yatenesturno.utils.TimeZoneManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cz.msebera.android.httpclient.Header;

public class JobSaver {

    public JobSaver() {

    }

    public void saveJob(Job job, RepositorySaveListener listener) {
        if (listener != null) {
            saveDaySchedulesForJob(job, listener);
        }
    }

    private void saveDaySchedulesForJob(final Job job, final RepositorySaveListener listener) {
        JSONObject body = new JSONObject();

        try {
            body.put("job_id", job.getId());

            JSONObject days = new JSONObject();
            for (DaySchedule d : job.getDaySchedules()) {

                String dayStart = formatTime(TimeZoneManager.toUTC(d.getDayStart()));
                String dayEnd = formatTime(TimeZoneManager.toUTC(d.getDayEnd()));

                String pauseEnd = null, pauseStart = null;
                if (d.getPauseStart() != null) {
                    pauseStart = formatTime(TimeZoneManager.toUTC(d.getPauseStart()));
                    pauseEnd = formatTime(TimeZoneManager.toUTC(d.getPauseEnd()));
                }

                JSONObject jsonObject = new JSONObject();

                jsonObject.put("day_start", dayStart);
                jsonObject.put("day_end", dayEnd);
                jsonObject.put("pause_start", pauseStart);
                jsonObject.put("pause_end", pauseEnd);
                days.put(d.getDayOfWeek() + "", jsonObject);
            }
            body.put("days", days);

            DatabaseDjangoWrite.getInstance().POSTJSON(
                    Constants.DJANGO_URL_NEW_DAY_SCHEDULE,
                    body,
                    new DatabaseCallback() {
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            saveServiceInstanceForJob(job, listener);
                            Log.d("JobSaver Success", body.toString());
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                            listener.onFailure();
                            Log.d("JobSaver Failure", responseString.toString());
                        }
                    }
            );
            DayScheduleManager.getInstance().invalidateJob(job.getId());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void saveServiceInstanceForJob(Job job, RepositorySaveListener listener) {
        JSONObject body = new JSONObject();
        JSONObject days = new JSONObject();
        JSONObject instances = new JSONObject();

        Set<ServiceInstance> serviceInstanceSet = new HashSet<>();

        try {
            for (DaySchedule ds : job.getDaySchedules()) {
                serviceInstanceSet.addAll(ds.getServiceInstances());

                JSONArray servicesArray = new JSONArray();
                for (ServiceInstance p : ds.getServiceInstances()) {
                    servicesArray.put(p.getService().getId());
                }
                days.put(ds.getDayOfWeek() + "", servicesArray);
            }

            for (ServiceInstance si : serviceInstanceSet) {
                JSONObject jsonServiceInstance = new JSONObject();

                JSONArray concurrentServicesIdArray = getConcurrentServicesIdForService(si.getConcurrentServices());

                String duration = formatTime(si.getDuration());
                String startTime = null, endTime = null;
                if (si.getStartTime() != null) {
                    startTime = formatTime(TimeZoneManager.toUTC(si.getStartTime()));
                    endTime = formatTime(TimeZoneManager.toUTC(si.getEndTime()));
                }

                jsonServiceInstance.put("cost", si.getPrice());
                jsonServiceInstance.put("duration", duration);
                jsonServiceInstance.put("concurrency", si.getConcurrency());
                jsonServiceInstance.put("concurrent_services", concurrentServicesIdArray);
                jsonServiceInstance.put("reminder_notification", si.isReminderSet());

                jsonServiceInstance.put("emergency", si.isEmergency());
                jsonServiceInstance.put("uses_credits", si.isCredits());
                jsonServiceInstance.put("can_book_without_credits", si.isCanBookWithoutCredits());
                jsonServiceInstance.put("max_apps_per_day", si.getMaxAppsPerDay());
                jsonServiceInstance.put("max_apps_simultaneously", si.getMaxAppsSimultaneously());

                if (si.getReminderInterval() != null) {
                    jsonServiceInstance.put("reminder_interval", formatTime((si.getReminderInterval())));
                }

                if (si.isClassType()) {
                    jsonServiceInstance.put("fixed_schedule", true);
                    jsonServiceInstance.put("is_class_type", true);
                    JSONArray jsonArrayClassTimes = getClassTimesAsJsonArray(si);
                    jsonServiceInstance.put("class_times", jsonArrayClassTimes);
                } else {
                    jsonServiceInstance.put("fixed_schedule", si.isFixedSchedule());
                    if (si.getStartTime() != null) {
                        jsonServiceInstance.put("start_time", startTime);
                        jsonServiceInstance.put("end_time", endTime);
                    }
                }

                if (!si.isFixedSchedule() && si.getInterval() != null) {
                    jsonServiceInstance.put("interval", formatTime((si.getInterval())));
                }

                instances.put(si.getService().getId(), jsonServiceInstance);
            }

            body.put("job_id", job.getId());
            body.put("days", days);
            body.put("instances", instances);

            saveServiceInstanceToDatabase(job, body, listener);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private JSONArray getClassTimesAsJsonArray(ServiceInstance si) {
        JSONArray out = new JSONArray();
        for (Calendar c : si.getClassTimes()) {
            out.put(formatTime(TimeZoneManager.toUTC(c)));
        }
        return out;
    }

    private JSONArray getConcurrentServicesIdForService(List<Service> concurrentServices) {
        JSONArray out = new JSONArray();

        for (int i = 0; i < concurrentServices.size(); i++) {
            out.put(concurrentServices.get(i).getId());
        }

        return out;
    }

    private void saveServiceInstanceToDatabase(final Job job, final JSONObject body, final RepositorySaveListener listener) {
        DatabaseDjangoWrite.getInstance().POSTJSON(
                Constants.DJANGO_URL_NEW_SERVICE_INSTANCE,
                body,
                new DatabaseCallback() {
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        saveJobConfig(job, listener);
                        Log.d("JobSaver Success saveServiceInstanceToDatabase", body.toString());

                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        Log.d("JobSaver FAILURE", responseString.toString());

                    }
                }
        );
    }

    private void saveJobConfig(Job job, final RepositorySaveListener listener) {
        Map<String, String> body = new HashMap<>();
        body.put("job_id", job.getId());
        body.put("user_cancellable_apps", String.valueOf(job.canUserCancelApps()));
        body.put("max_apps_per_day", String.valueOf(job.getMaxAppsPerDay()));
        body.put("max_apps_simultaneously", String.valueOf(job.getMaxAppsSimultaneusly()));

        DatabaseDjangoWrite.getInstance().POST(
                Constants.DJANGO_URL_JOB_CONFIG,
                body,
                new DatabaseCallback() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        listener.onSuccess();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        listener.onFailure();
                    }
                }
        );
    }

    private String formatTime(Calendar duration) {
        return String.format("%02d:%02d:%02d", duration.get(Calendar.HOUR_OF_DAY), duration.get(Calendar.MINUTE), 0);
    }

}