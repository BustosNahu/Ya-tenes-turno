package com.yatenesturno.functionality.days_off;

import com.yatenesturno.Constants;
import com.yatenesturno.Parser.Parser;
import com.yatenesturno.Parser.ParserGeneric;
import com.yatenesturno.database.djangoImpl.DatabaseDjangoRead;
import com.yatenesturno.database.djangoImpl.DatabaseDjangoWrite;
import com.yatenesturno.listeners.DatabaseCallback;
import com.yatenesturno.object_interfaces.Job;
import com.yatenesturno.utils.CalendarUtils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;

public class DaysOffManager {

    private static DaysOffManager instance;
    private Map<String, List<DayOff>> jobDayOffMap;

    private DaysOffManager() {
        jobDayOffMap = new HashMap<>();
    }

    public static DaysOffManager getInstance() {
        if (instance == null) {
            instance = new DaysOffManager();
        }
        return instance;
    }

    public void getDaysOff(Job job, OnDayOffFetchListener listener) {
        if (jobDayOffMap.containsKey(job.getId())) {
            listener.onFetch(jobDayOffMap.get(job.getId()));
            return;
        }
        fetchFromRemote(job, listener);
    }

    private void fetchFromRemote(Job job, OnDayOffFetchListener listener) {
        DatabaseDjangoRead.getInstance().GET(
                String.format(Constants.DJANGO_URL_DAYS_OFF, job.getId()),
                null,
                new DatabaseCallback() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        Parser<DayOff> parser = new ParserGeneric<>(DayOff.class, "data");

                        List<DayOff> dayOffList = new ArrayList<>(parser.parseByList(response));

                        saveToLocal(job, dayOffList);
                        listener.onFetch(dayOffList);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        listener.onFetch(new ArrayList<>());
                    }
                }
        );
    }

    public void invalidate() {
        jobDayOffMap = new HashMap<>();
    }

    public void setDayOff(Job job, Calendar date, OnUpdateDayOffListener listener) {
        DayOff dayOff = new DayOff();
        dayOff.setParsedDate(date);

        postToRemote(job, dayOff, listener);
    }

    private void postToRemote(Job job, DayOff dayOff, OnUpdateDayOffListener listener) {
        String dateString = CalendarUtils.formatDate(dayOff.getDate());

        DatabaseDjangoWrite.getInstance().POST(
                String.format(Constants.DJANGO_URL_DAYS_OFF_UPDATE, job.getId(), dateString),
                null,
                new DatabaseCallback() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        saveToLocal(job, dayOff);
                        listener.onUpdate();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        listener.onFailure();
                    }
                }
        );
    }

    public void removeFromDayOff(Job job, Calendar date, OnUpdateDayOffListener listener) {
        if (!jobDayOffMap.containsKey(job.getId())) {
            listener.onUpdate();
            return;
        }

        DayOff dayOff = findDayOff(job, date);
        removeFromRemote(job, dayOff, listener);
    }

    private DayOff findDayOff(Job job, Calendar date) {
        List<DayOff> jobDayOffList = jobDayOffMap.get(job.getId());
        if (jobDayOffList != null) {
            for (DayOff dayOff : jobDayOffList) {
                if (dayOff.hasDate(date)) {
                    return dayOff;
                }
            }
        }
        return null;
    }

    private void removeFromRemote(Job job, DayOff dayOff, OnUpdateDayOffListener listener) {
        String dateString = CalendarUtils.formatDate(dayOff.getDate());
        DatabaseDjangoWrite.getInstance().DELETE(
                String.format(Constants.DJANGO_URL_DAYS_OFF_UPDATE, job.getId(), dateString),
                new DatabaseCallback() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        jobDayOffMap.get(job.getId()).remove(dayOff);
                        listener.onUpdate();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        listener.onFailure();
                    }
                }
        );
    }

    private void saveToLocal(Job job, List<DayOff> dayOffList) {
        jobDayOffMap.put(job.getId(), dayOffList);
    }

    private void saveToLocal(Job job, DayOff dayOff) {
        if (!jobDayOffMap.containsKey(job.getId())) {
            jobDayOffMap.put(job.getId(), new ArrayList<>());
        }
        jobDayOffMap.get(job.getId()).add(dayOff);
    }

    public interface OnDayOffFetchListener {
        void onFetch(List<DayOff> days);
    }

    public interface OnUpdateDayOffListener {
        void onUpdate();

        void onFailure();
    }

}
