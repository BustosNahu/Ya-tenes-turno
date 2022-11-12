package com.yatenesturno.functionality;

import com.yatenesturno.Constants;
import com.yatenesturno.database.DatabaseDjangoRead;
import com.yatenesturno.listeners.DatabaseCallback;
import com.yatenesturno.object_interfaces.Appointment;
import com.yatenesturno.object_interfaces.Job;
import com.yatenesturno.serializers.BuilderListAppointment;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import cz.msebera.android.httpclient.Header;

public class ManagerAppointment {

    private static ManagerAppointment instance;
    private final Map<String, Map<Calendar, List<Appointment>>> jobAppointmentMap;

    private ManagerAppointment() {
        jobAppointmentMap = new HashMap<>();
    }

    public static ManagerAppointment getInstance() {
        if (instance == null) {
            instance = new ManagerAppointment();
        }
        return instance;
    }

    public void invalidateJob(String jobId) {
        jobAppointmentMap.remove(jobId);
    }

    public void getAppointmentForJob(Job job, Calendar date, final OnAppointmentFetchListener listener) {
        List<Appointment> out = findAppointmentsForDate(job, date);
        if (out != null) {
            listener.onFetch(out);
        } else {
            fetchFromRemote(job, date, listener);
        }
    }

    private List<Appointment> findAppointmentsForDate(Job job, Calendar date) {
        if (!jobAppointmentMap.containsKey(job.getId())) {
            return null;
        }

        for (Calendar p : jobAppointmentMap.get(job.getId()).keySet()) {
            if (isSameDay(date, p)) {
                return jobAppointmentMap.get(job.getId()).get(p);
            }
        }

        return null;
    }

    private boolean isSameDay(Calendar date1, Calendar date2) {
        return date1.get(Calendar.DAY_OF_YEAR) == date2.get(Calendar.DAY_OF_YEAR) &&
                date1.get(Calendar.YEAR) == date2.get(Calendar.YEAR);
    }

    private void fetchFromRemote(final Job job, final Calendar date, final OnAppointmentFetchListener listener) {
        Map<String, String> body = new HashMap<>();
        body.put("job_id", job.getId());

        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        body.put("date", dateTimeFormat.format(date.getTime()));

        body.put("tz_offset", getOffset());

        DatabaseDjangoRead.getInstance().GET(
                Constants.DJANGO_URL_APPOINTMENTS,
                body,
                new DatabaseCallback() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        buildAppointmentsResponse(response, job, date, listener);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        listener.onFailure();
                    }
                }
        );
    }

    private String getOffset() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"),
                Locale.getDefault());
        Date currentLocalTime = calendar.getTime();
        DateFormat date = new SimpleDateFormat("Z");

        return date.format(currentLocalTime);
    }

    public void buildAppointmentsResponse(JSONObject response, Job job, Calendar date, OnAppointmentFetchListener listener) {
        try {
            BuilderListAppointment builderListAppointment = new BuilderListAppointment();
            List<Appointment> appInDate = builderListAppointment.build(response);
            if (date != null) {
                saveInLocal(job, date, appInDate);
            }
            listener.onFetch(appInDate);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void saveInLocal(Job job, Calendar date, List<Appointment> appInDate) {
        if (!jobAppointmentMap.containsKey(job.getId())) {
            jobAppointmentMap.put(job.getId(), new HashMap<Calendar, List<Appointment>>());
        }

        boolean foundMatchingDate = false;
        Iterator<Map.Entry<Calendar, List<Appointment>>> it = jobAppointmentMap.get(job.getId()).entrySet().iterator();
        while (it.hasNext() && !foundMatchingDate) {
            Map.Entry<Calendar, List<Appointment>> e = it.next();
            if (isSameDay(e.getKey(), date)) {
                e.getValue().addAll(appInDate);
                foundMatchingDate = true;
            }
        }

        if (!foundMatchingDate) {
            jobAppointmentMap.get(job.getId()).put(date, appInDate);
        }
    }

    public void getUpcomingAppointments(final Job job, final OnAppointmentFetchListener listener) {
        Map<String, String> body = new HashMap<>();
        body.put("job_id", job.getId());

        DatabaseDjangoRead.getInstance().GET(
                Constants.DJANGO_URL_UPCOMING_APPOINTMENTS,
                body,
                new DatabaseCallback() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        invalidateJob(job.getId());
                        buildAppointmentsResponse(response, job, null, listener);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        listener.onFailure();
                    }
                }
        );
    }

    public interface OnAppointmentFetchListener {
        void onFetch(List<Appointment> appointmentList);

        void onFailure();
    }

}
