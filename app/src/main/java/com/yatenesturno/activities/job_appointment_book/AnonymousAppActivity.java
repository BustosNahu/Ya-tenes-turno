package com.yatenesturno.activities.job_appointment_book;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.google.android.material.snackbar.Snackbar;
import com.yatenesturno.Constants;
import com.yatenesturno.R;
import com.yatenesturno.activities.get_premium.GetPremiumActivity;
import com.yatenesturno.custom_views.LoadingButton;
import com.yatenesturno.custom_views.LoadingOverlay;
import com.yatenesturno.custom_views.NonSwipeableViewPager;
import com.yatenesturno.database.djangoImpl.DatabaseDjangoWrite;
import com.yatenesturno.listeners.DatabaseCallback;
import com.yatenesturno.object_interfaces.CustomUser;
import com.yatenesturno.object_interfaces.Job;
import com.yatenesturno.object_interfaces.Label;
import com.yatenesturno.object_interfaces.ServiceInstance;
import com.yatenesturno.user_auth.UserManagement;
import com.yatenesturno.utils.CalendarUtils;
import com.yatenesturno.utils.CustomAlertDialogBuilder;
import com.yatenesturno.utils.TimeZoneManager;
import com.yatenesturno.object_interfaces.Place;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class AnonymousAppActivity extends AppCompatActivity {

    /**
     * Constants
     */
    public static final String DATE_SELECTION_FRAGMENT = "dateSelectionFragment";
    public static final String CLIENT_INFO_SELECTION_FRAGMENT = "clientInfoSelectionFragment";
    public static final String SELECTED_SERVICES = "selectedServices";
    public static final String JOB = "job";
    public static final String SELECTED_CALENDAR = "selectedCalendar";
    public static final String CLIENT_LIST = "clientList";
    private static final String PLACE_ID = "placeId";

    /**
     * Instance Refs
     */
    private Job job;
    private String placeId;
    private Calendar selectedCalendar;
    private List<ServiceInstance> selectedServices;
    private ArrayList<CustomUser> clientList;
    private String dayScheduleId;
    private Label selectedLabel;

    /**
     * UI Refs
     */
    private AppointmentDateSelectionFragment dateSelectionFragment;
    private ClientInfoSelectionFragment clientInfoSelectionFragment;
    private NonSwipeableViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anonymous_app);

        if (savedInstanceState == null) {
            job = getIntent().getExtras().getParcelable(JOB);
            placeId = getIntent().getExtras().getString(PLACE_ID);
            clientList = getIntent().getExtras().getParcelableArrayList(CLIENT_LIST);
        } else {
            recoverState(savedInstanceState);
        }

        setUpActionBar();

        new Handler(Looper.myLooper()).postDelayed(() -> initViews(), 200);
    }

    private void setUpActionBar() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(getDrawable(R.color.white));
        getSupportActionBar().setElevation(0);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        setTitle(getString(R.string.new_anonymous_app_title));
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.putFragment(outState, DATE_SELECTION_FRAGMENT, dateSelectionFragment);
        fragmentManager.putFragment(outState, CLIENT_INFO_SELECTION_FRAGMENT, clientInfoSelectionFragment);

        outState.putAll(saveState());
    }

    private Bundle saveState() {
        Bundle out = new Bundle();

        out.putParcelableArrayList(SELECTED_SERVICES, (ArrayList<ServiceInstance>) selectedServices);
        out.putParcelable(JOB, job);
        out.putParcelableArrayList(CLIENT_LIST, clientList);
        out.putSerializable(SELECTED_CALENDAR, selectedCalendar);


        return out;
    }

    private void recoverState(Bundle bundle) {
        selectedServices = bundle.getParcelableArrayList(SELECTED_SERVICES);
        job = bundle.getParcelable(JOB);
        clientList = bundle.getParcelableArrayList(CLIENT_LIST);
        selectedCalendar = (Calendar) bundle.getSerializable(SELECTED_CALENDAR);

        FragmentManager fragmentManager = getSupportFragmentManager();
        dateSelectionFragment = (AppointmentDateSelectionFragment) fragmentManager.getFragment(bundle, DATE_SELECTION_FRAGMENT);
        clientInfoSelectionFragment = (ClientInfoSelectionFragment) fragmentManager.getFragment(bundle, CLIENT_INFO_SELECTION_FRAGMENT);

    }

    private void initViews() {
        viewPager = findViewById(R.id.viewPagerAnonymousAppointment);
        LoadingOverlay loadingOverlay = new LoadingOverlay(findViewById(R.id.root));

        initFragments();
    }

    private void initFragments() {

        if (dateSelectionFragment == null) {
            dateSelectionFragment = new AppointmentDateSelectionFragment(job, placeId);
            clientInfoSelectionFragment = new ClientInfoSelectionFragment(clientList);
        }

        List<Fragment> fragments = new ArrayList<>();
        fragments.add(dateSelectionFragment);
        fragments.add(clientInfoSelectionFragment);

        setFragmentsListeners();

        viewPager.setAdapter(
                new AppointmentConfigPagerAdapter(getSupportFragmentManager(), fragments)
        );
    }

    private void setFragmentsListeners() {
        dateSelectionFragment.setOnDateSelectedListener(new OnSelectedAppListener());
        dateSelectionFragment.setOnLabelSelectedListener(new OnLabelSelectedListener());
        clientInfoSelectionFragment.setListener(new OnClientInfoConfirmListener());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() == 0) {
            super.onBackPressed();
        } else {
            showDateSelectionFragment();
        }
    }

    public void showDateSelectionFragment() {
        viewPager.setCurrentItem(0);
    }

    private void changeToClientInfoSelection() {
        viewPager.setCurrentItem(1);
    }

    public void showClientInfoFragment() {
        viewPager.setCurrentItem(1);
    }

    /**
     * Method to show user a dialog with the client information, and checks when the user
     * clicks on the confirmButton, but it validates if the user is premium or not too.
     * @param name
     * @param email
     * @param phonenumber
     */
    private void onClientInfoConfirmed(final String name, final String email, final String phonenumber) {
        View view = getLayoutInflater().inflate(R.layout.dialog_anonymous_app_overview, null, false);
        CustomAlertDialogBuilder builder = new CustomAlertDialogBuilder(this)
                .setTitle(getString(R.string.app_overview))
                .setView(view);

        TextView labelClient = view.findViewById(R.id.labelClient);
        TextView labelDate = view.findViewById(R.id.labelDate);
        TextView labelTime = view.findViewById(R.id.labelTime);
        TextView labelServices = view.findViewById(R.id.labelServices);
        CheckBox checkBoxNotify = view.findViewById(R.id.checkBoxNotify);
        CheckBox checkBoxCredits = view.findViewById(R.id.checkBoxCredits);
        LoadingButton btnConfirm = view.findViewById(R.id.btnConfirm);

        if (shouldShowDiscountCredits()) {
            checkBoxCredits.setVisibility(View.VISIBLE);
        } else {
            checkBoxCredits.setVisibility(View.GONE);
        }

        labelClient.setText(name);
        labelDate.setText(CalendarUtils.formatDate(TimeZoneManager.fromUTC(selectedCalendar)));
        labelTime.setText(CalendarUtils.formatTime(TimeZoneManager.fromUTC(selectedCalendar)));
        labelServices.setText(getServicesString());
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(GetPremiumActivity.hasPremiumInPlaceOrShowScreen(AnonymousAppActivity.this, placeId, UserManagement.getInstance().getUser().getId())){
                    btnConfirm.showLoading();
                    postAppointment(name, email, phonenumber, checkBoxNotify.isChecked(), checkBoxCredits.isChecked(), btnConfirm, builder);
                }else{
                btnConfirm.hideLoading();}
            }
        });
        builder.show();
    }

    private boolean shouldShowDiscountCredits() {
        for (ServiceInstance si : selectedServices) {
            if (si.isCredits()) {
                return true;
            }
        }
        return false;
    }

    private String getServicesString() {
        StringBuilder servicesStringBuilder = new StringBuilder();
        for (int i = 0; i < selectedServices.size(); i++) {
            servicesStringBuilder.append(selectedServices.get(i).getService().getName());

            if (i < selectedServices.size() - 1) {
                servicesStringBuilder.append(", ");
            }
        }
        return servicesStringBuilder.toString();
    }

    private void postAppointment(String name, String email, String phonenumber, boolean notify, boolean discountCredits, LoadingButton btnConfirm, CustomAlertDialogBuilder builder) {
        JSONObject rootJsonObject = new JSONObject();

        try {
            rootJsonObject.put("job_id", job.getId());

            JSONObject clientInfoJsonObject = new JSONObject();
            clientInfoJsonObject.put("given_name", name);
            clientInfoJsonObject.put("email", email);
            if (phonenumber != null) {
                clientInfoJsonObject.put("phonenumber", phonenumber);
            }
            if (selectedLabel != null) {
                rootJsonObject.put("label", selectedLabel.getId());
            }
            rootJsonObject.put("client", clientInfoJsonObject);

            JSONArray appointmentJsonArray = new JSONArray();
            JSONObject appointmentJsonObject = new JSONObject();

            for (ServiceInstance si : selectedServices) {
                JSONObject jsonObjectService = new JSONObject();
                jsonObjectService.put("start", formatCalendarAsTimeStamp());
                jsonObjectService.put("day_schedule", dayScheduleId);
                appointmentJsonObject.put(si.getService().getId(), jsonObjectService);
            }

            appointmentJsonArray.put(appointmentJsonObject);

            JSONObject appointments = new JSONObject();
            appointments.put("appointment", appointmentJsonArray);
            appointments.put("local_time", getAsLocalTime());
            JSONArray appList = new JSONArray();
            appList.put(appointments);
            rootJsonObject.put("notify_client", notify);
            if (shouldShowDiscountCredits()) {
                rootJsonObject.put("discount_credits", discountCredits);
            }
            rootJsonObject.put("appointments", appList);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        DatabaseDjangoWrite.getInstance().POSTJSON(
                Constants.DJANGO_URL_NEW_ANONYMOUS_APP,
                rootJsonObject,
                new DatabaseCallback() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        btnConfirm.hideLoading();
                        builder.dismiss();
                        returnOK();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        btnConfirm.hideLoading();
                        builder.dismiss();
                        showBookingError();
                    }
                }
        );
    }

    private String getAsLocalTime() {
        Calendar localTime = TimeZoneManager.fromUTC(selectedCalendar);
        return localTime.get(Calendar.DAY_OF_MONTH) + "-" +
                localTime.get(Calendar.MONTH) + " " +
                String.format("%02d:%02d", localTime.get(Calendar.HOUR_OF_DAY),
                        localTime.get(Calendar.MINUTE));
    }

    private void returnOK() {
        setResult(RESULT_OK);
        finish();
    }

    private void showBookingError() {
        Snackbar.make(findViewById(R.id.root), getString(R.string.booking_error), Snackbar.LENGTH_SHORT).show();
    }

    private String formatCalendarAsTimeStamp() {
        return
                selectedCalendar.get(Calendar.YEAR) + "-" +
                        (selectedCalendar.get(Calendar.MONTH) + 1) + "-" +
                        selectedCalendar.get(Calendar.DAY_OF_MONTH) + " " +
                        selectedCalendar.get(Calendar.HOUR_OF_DAY) + ":" +
                        selectedCalendar.get(Calendar.MINUTE) + ":" +
                        selectedCalendar.get(Calendar.SECOND);

    }

    public interface ListenerSelectedDateAndTime {
        void onDateAndTimeSelected(Calendar calendar, List<ServiceInstance> selectedServices, String dayScheduleId);
    }

    public interface ListenerSelectedLabel {
        void onSelected(Label label);
    }

    private static class AppointmentConfigPagerAdapter extends FragmentPagerAdapter {

        private final List<Fragment> fragmentList;

        public AppointmentConfigPagerAdapter(FragmentManager fm, List<Fragment> frags) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
            fragmentList = frags;
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }
    }

    public class OnLabelSelectedListener implements ListenerSelectedLabel {

        @Override
        public void onSelected(Label label) {
            selectedLabel = label;
        }
    }

    private class OnSelectedAppListener implements ListenerSelectedDateAndTime {

        @Override
        public void onDateAndTimeSelected(Calendar calendar, List<ServiceInstance> services, String selectedDayScheduleId) {
            selectedCalendar = calendar;
            selectedServices = services;
            dayScheduleId = selectedDayScheduleId;
            showClientInfoFragment();
            changeToClientInfoSelection();
        }
    }

    private class OnClientInfoConfirmListener implements ClientInfoSelectionFragment.OnConfirmedListener {
        @Override
        public void onConfirm(String name, String email, String phonenumber) {
            onClientInfoConfirmed(name, email, phonenumber);
        }
    }
}