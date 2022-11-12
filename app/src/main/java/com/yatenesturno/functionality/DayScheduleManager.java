package com.yatenesturno.functionality;

import android.util.Log;

import com.yatenesturno.Constants;
import com.yatenesturno.database.DatabaseDjangoRead;
import com.yatenesturno.listeners.DatabaseCallback;
import com.yatenesturno.object_interfaces.DaySchedule;
import com.yatenesturno.object_interfaces.Job;
import com.yatenesturno.serializers.BuilderListDaySchedule;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;

public class DayScheduleManager {

    private static DayScheduleManager instance;
    private Map<String, List<DaySchedule>> jobDayScheduleMap;

    private DayScheduleManager() {
        jobDayScheduleMap = new HashMap<>();
    }

    public static DayScheduleManager getInstance() {
        if (instance == null) {
            instance = new DayScheduleManager();
        }
        return instance;
    }

    public void getDaySchedules(String jobId, OnDaySchedulesFetchListener listener) {
        if (jobDayScheduleMap.get(jobId) != null) {
            listener.onFetch(jobDayScheduleMap.get(jobId));
            return;
        }

        fetchFromRemote(jobId, listener);
    }

    private void fetchFromRemote(String jobId, OnDaySchedulesFetchListener listener) {

        Map<String, String> body = new HashMap<>();
        body.put("job_id", jobId);

        DatabaseDjangoRead.getInstance().GET(
                Constants.DJANGO_URL_GET_DAY_SCHEDULES,
                body,
                new DatabaseCallback() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        BuilderListDaySchedule builder = new BuilderListDaySchedule();
                        try {
                            List<DaySchedule> dayScheduleList = builder.build(response);
                            jobDayScheduleMap.put(jobId, dayScheduleList);
                            listener.onFetch(dayScheduleList);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        Log.e("ASDD", "asdsda");
                    }
                }
        );
    }

    public void invalidateJob(String jobId) {
        jobDayScheduleMap.remove(jobId);
    }

    public void invalidateJob(Job job) {
        invalidateJob(job.getId());
    }

    public interface OnDaySchedulesFetchListener {
        void onFetch(List<DaySchedule> dayScheduleList);
    }

}
