package com.yatenesturno.serializers;

import com.yatenesturno.object_interfaces.Appointment;
import com.yatenesturno.object_interfaces.AppointmentService;
import com.yatenesturno.object_interfaces.AppointmentServiceImpl;
import com.yatenesturno.object_interfaces.CustomUser;
import com.yatenesturno.object_interfaces.Label;
import com.yatenesturno.object_interfaces.ServiceInstance;
import com.yatenesturno.objects.AppointmentClassImpl;
import com.yatenesturno.objects.AppointmentImpl;
import com.yatenesturno.utils.TimeZoneManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class BuilderListAppointment {


    public List<Appointment> build(JSONObject json) throws JSONException {
        List<Appointment> out = new ArrayList<>();


        JSONObject jsonAppointments = json.getJSONObject("appointments");
        Appointment appointment;
        for (Iterator<String> itAppointments = jsonAppointments.keys(); itAppointments.hasNext(); ) {
            String appointmentId = itAppointments.next();
            JSONObject jsonAppointmentData = jsonAppointments.getJSONObject(appointmentId);
            appointment = buildAppointmentImpl(jsonAppointmentData);
            out.add(appointment);
        }

        JSONObject jsonClassAppointments = json.getJSONObject("class_appointments");
        for (Iterator<String> itClassAppointments = jsonClassAppointments.keys(); itClassAppointments.hasNext(); ) {

            String classAppKey = itClassAppointments.next();
            JSONObject jsonObjectClassApp = jsonClassAppointments.getJSONObject(classAppKey);

            JSONObject appointments = jsonObjectClassApp.getJSONObject("appointments");

            ServiceInstance serviceInstance = new BuilderObjectServiceInstance().build(jsonObjectClassApp.getJSONObject("service_instance"));

            List<AppointmentImpl> innerAppsList = new ArrayList<>();
            for (Iterator<String> itInnerApps = appointments.keys(); itInnerApps.hasNext(); ) {
                JSONObject innerAppJson = appointments.getJSONObject(itInnerApps.next());
                AppointmentImpl innerAppointment = buildAppointmentImpl(innerAppJson);
                innerAppsList.add(innerAppointment);
            }

            Appointment classAppointment = new AppointmentClassImpl(
                    innerAppsList.get(0).getTimeStampStart(),
                    serviceInstance,
                    innerAppsList
            );

            out.add(classAppointment);
        }

        Collections.sort(out, new ComparatorAppointment());
        return out;
    }

    private AppointmentImpl buildAppointmentImpl(JSONObject jsonAppointmentData) throws JSONException {
        AppointmentImpl appointment;

        JSONObject jsonAppointment = jsonAppointmentData.getJSONObject("appointment");
        JSONObject jsonServicesAppointment = jsonAppointmentData.getJSONObject("services_appointment");

        Label label = null;
        if (!jsonAppointment.isNull("label")) {
            JSONObject jsonLabel = jsonAppointment.getJSONObject("label");
            BuilderObjectJobLabel builderLabel = new BuilderObjectJobLabel();
            label = builderLabel.build(jsonLabel);
        }

        String appointmentId = jsonAppointment.getInt("id") + "";
        Calendar appointmentDate = parseDateTime(jsonAppointment.getString("timestamp_start"));
        CustomUser client = new BuilderObjectCustomUser().build(jsonAppointment.getJSONObject("client"));
        boolean attended = jsonAppointment.getBoolean("attended");

        String observation = null;
        if (jsonAppointment.has("observation") && !jsonAppointment.isNull("observation")) {
            observation = jsonAppointment.getString("observation");
        }

        List<AppointmentService> appointmentServiceList = new ArrayList<>();
        for (Iterator<String> itServices = jsonServicesAppointment.keys(); itServices.hasNext(); ) {
            String id = itServices.next();
            JSONObject serviceAppointmentJson = jsonServicesAppointment.getJSONObject(id);

            AppointmentService appointmentService = buildAppointmentService(serviceAppointmentJson);
            appointmentServiceList.add(appointmentService);
        }

        Collections.sort(appointmentServiceList, new ComparatorServiceInstance());
        appointment = new AppointmentImpl(
                client,
                appointmentId,
                appointmentDate,
                appointmentServiceList,
                label,
                attended,
                observation
        );

        return appointment;
    }

    private AppointmentService buildAppointmentService(JSONObject serviceAppointmentJson) throws JSONException {
        JSONObject jsonServiceInstance = serviceAppointmentJson.getJSONObject("service_instance");


        ServiceInstance serviceInstance = new BuilderObjectServiceInstance().build(jsonServiceInstance);

        Calendar timestamp = parseDateTime(serviceAppointmentJson.getString("timestamp"));
        boolean bookedWithoutCredits = serviceAppointmentJson.getBoolean("booked_without_credits");
        return new AppointmentServiceImpl(
                serviceAppointmentJson.getInt("id") + "",
                timestamp,
                serviceInstance,
                bookedWithoutCredits);
    }

    private Calendar parseDateTime(String dateString) {
        try {
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);
            calendar.setTime(dateTimeFormat.parse(dateString));
            calendar = TimeZoneManager.fromUTC(calendar);
            return calendar;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static class ComparatorAppointment implements java.util.Comparator<Appointment> {

        @Override
        public int compare(Appointment o1, Appointment o2) {
            return o1.getTimeStampStart().compareTo(o2.getTimeStampStart());
        }
    }

    private static class ComparatorServiceInstance implements Comparator<AppointmentService> {

        @Override
        public int compare(AppointmentService o1, AppointmentService o2) {
            return o1.getTimeStamp().compareTo(o2.getTimeStamp());
        }
    }
}
