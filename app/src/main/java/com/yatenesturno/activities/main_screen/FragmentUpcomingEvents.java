package com.yatenesturno.activities.main_screen;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.LAYOUT_INFLATER_SERVICE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.yatenesturno.Constants;
import com.yatenesturno.R;
import com.yatenesturno.activities.appointment_view.MonthViewActivity;
import com.yatenesturno.activities.appointment_view.ViewAppointmentActivity;
import com.yatenesturno.activities.job_appointment_book.AnonymousAppActivity;
import com.yatenesturno.activities.services.CreateServiceActivity;
import com.yatenesturno.custom_views.AdapterRecyclerViewUpcomingEvents;
import com.yatenesturno.custom_views.LoadingOverlay;
import com.yatenesturno.database.djangoImpl.DatabaseDjangoRead;
import com.yatenesturno.functionality.DayScheduleManager;
import com.yatenesturno.functionality.ManagerAppointment;
import com.yatenesturno.listeners.DatabaseCallback;
import com.yatenesturno.object_interfaces.Appointment;
import com.yatenesturno.object_interfaces.CustomUser;
import com.yatenesturno.object_interfaces.Job;
import com.yatenesturno.object_interfaces.Place;
import com.yatenesturno.object_interfaces.ServiceInstance;
import com.yatenesturno.serializers.BuilderListCustomUser;
import com.yatenesturno.serializers.BuilderListServiceInstance;
import com.yatenesturno.user_auth.UserManagement;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import cz.msebera.android.httpclient.Header;

public class FragmentUpcomingEvents extends Fragment {

    private static final Map<Integer, String> mapNumberDay = new HashMap<Integer, String>() {{
        put(1, "Dom");
        put(2, "Lun");
        put(3, "Mar");
        put(4, "Mie");
        put(5, "Jue");
        put(6, "Vie");
        put(7, "Sab");
    }};

    private static final int RC_VIEW_APPOINTMENT = 1, RC_MONTH_VIEW = 2;

    private LoadingOverlay loadingOverlay;
    private RecyclerView recyclerViewUpcomingEvents;
    private LinearLayout linearLayoutDays;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout noAppointmentsContainer;
    private LinearLayout noServicesContainer;
    private TextView tvUpcomingAppointments;
    private FloatingActionButton scheduleAppointmentFAB;
    private CardView popUpBtnCreateService;
    private CardView popUpBtnDecline;
    CardView createServiceBtn;
    private List<CustomUser> clientList;

    private Job editedJob;
    private Job job;
    private Place place;
    private List<Appointment> appointmentList;
    public FragmentUpcomingEvents() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_job_view, container, false);

        initViews(view);
        appointmentList = new ArrayList<>();

        if (savedInstanceState == null) {
            showLoadingOverlay();
            initUI();
        } else {
            recoverState();
        }

        Log.d("ACTIVITYSTATE", "onCreateView");
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Boolean fromCreatedService = requireActivity().getIntent().getBooleanExtra("fromCreatedService", false);
        if (job != null && fromCreatedService){
            hasServices();
        }
    }

    private void fetchDaySchedules() {
        Log.d("fragmentEvents", "fetchDaySchedules");
        showLoadingOverlay();
        new Handler(Objects.requireNonNull(Looper.myLooper())).postDelayed(() -> DayScheduleManager.getInstance().getDaySchedules(job.getId(), dayScheduleList -> {
            hideLoadingOverlay();
            job.setDaySchedules(dayScheduleList);
            loadingOverlay.hide();
            Intent i = new Intent( requireActivity(), CreateServiceActivity.class);
            i.putExtra("job", (Serializable) job);
            i.putExtra("place", (Serializable) place);
            requireActivity().overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_right);
            startActivity(i);
        }), 300);
    }

    private void navigateToNewService(){
        fetchDaySchedules();

    }

    private void initUI() {
        showLoadingOverlay();
        if (hasJobs()){
            getClients();
            hasServices();
        }
        if (swipeRefreshLayout != null){
            swipeRefreshLayout.setOnRefreshListener(this::onRefresh);
        }

    }

    public List<Appointment> getAppointmentList() {
        return appointmentList;
    }

    public void setState(Bundle bundle) {
        job = bundle.getParcelable("selectedJob");
        place = bundle.getParcelable("selectedPlace");
        appointmentList = bundle.getParcelableArrayList("appointmentList");
    }

    public void setPlace(Place place){
        this.place = place;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        recoverState();
    }

    private void recoverState() {
        if (hasJobs()){
            initUI();
            onAppointmentsFetch();
        }else {
            displayNoJobs();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState == null) {
            showLoadingOverlay();
        } else {
            recoverState();
        }
    }

    public void initViews(View view) {
        swipeRefreshLayout = view.findViewById(R.id.swiperRefresh);
        linearLayoutDays = view.findViewById(R.id.linearLayoutDays);
        recyclerViewUpcomingEvents = view.findViewById(R.id.recyclerViewUpcomingEvents);
        loadingOverlay = new LoadingOverlay(view.findViewById(R.id.root));
        noAppointmentsContainer = view.findViewById(R.id.noAppointmentsContainer);
        noServicesContainer = view.findViewById(R.id.noServicesContainer);
        tvUpcomingAppointments = view.findViewById(R.id.tvUpcomingAppointments);
        scheduleAppointmentFAB = view.findViewById(R.id.scheduleAppointmentsFAB);
        createServiceBtn      = view.findViewById(R.id.create_first_service_Btn);
    }

    List<ServiceInstance> providedServices;

     void hasServices(){
            Map<String, String> body = new HashMap<>();
            body.put("job_id", job.getId());
        showLoadingOverlay();

            DatabaseDjangoRead.getInstance().GET(
                    Constants.DJANGO_URL_SERVICE_INSTANCES,
                    body,
                    new DatabaseCallback() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            try {
                                providedServices = new BuilderListServiceInstance().build(response.getJSONArray("service_instance"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                                providedServices = new ArrayList<>();

                            }

                            if (providedServices.isEmpty()){
                                //hasn't services
                                dislpayNoServices();
                                setNoServicesFloatingBtn();
                                setNoServicesBtn();
                            }else{

                                fetchAppointments();
                                populateDays();
                                setServicesFloatingBtn();
                                displayServices();
                            }

                            hideLoadingOverlay();
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                            loadingOverlay.hide();
                            showConnectionError();
                        }
                    }
            );
    }


    public void setServicesFloatingBtn(){
        Log.d("fragmentEvents", "setServicesFloatingBtn");
        scheduleAppointmentFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ( canEditJob()   ) navigateToAppointments();  else inflatePopupBtn();
            }
        });
    }


    public void setNoServicesFloatingBtn(){
        Log.d("fragmentEvents", "setServicesFloatingBtn");
        scheduleAppointmentFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ( canEditJob()   ) createServiceBtn(view); else inflatePopupBtn();
            }
        });
    }

    private void setNoServicesBtn(){
        createServiceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ( canEditJob()) navigateToNewService(); else inflatePopupBtn();
            }
        });
    }


    private void navigateToAppointments(){
        Intent intent = new Intent(getContext(), AnonymousAppActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable("job", job);
        bundle.putParcelableArrayList("clientList", (ArrayList<CustomUser>) clientList);
        bundle.putString("placeId", place.getId());
        intent.putExtras(bundle);
        requireActivity().startActivityForResult(intent, Constants.RC_NEW_ANONYMOUS_APP);
    }

    public void onClientsFetch(JSONObject response) {
        handleResponse(response);
    }

    private void getClients() {
        Map<String, String> body = getBody();
        DatabaseDjangoRead.getInstance().GET(
                Constants.DJANGO_URL_CLIENTS_OF_JOB,
                body,
                new DatabaseCallback() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        onClientsFetch(response);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        hideLoadingOverlay();
                        showConnectionError();
                    }
                }
        );
    }

    private Map<String, String> getBody() {
        Map<String, String> body = new HashMap<>();
        body.put("job_id", job.getId());
        return body;
    }

    private void handleResponse(JSONObject response) {
        BuilderListCustomUser builderListCustomUser = new BuilderListCustomUser();
        try {
            clientList = builderListCustomUser.build(response);
        } catch (JSONException e) {
            e.printStackTrace();
            clientList = new ArrayList<>();
        }
        hideLoadingOverlay();

    }

    private boolean canEditJob() {
        boolean isPlaceOwner = place.getOwner().getId().equals(UserManagement.getInstance().getUser().getId());
        return job.canEdit() || isPlaceOwner;
    }


    private void dislpayNoServices(){
        noAppointmentsContainer.setVisibility(View.GONE);
        linearLayoutDays.setVisibility(View.GONE);
        noServicesContainer.setVisibility(View.VISIBLE);
    }

    private void displayServices(){
        noAppointmentsContainer.setVisibility(View.VISIBLE);
        linearLayoutDays.setVisibility(View.VISIBLE);
        recyclerViewUpcomingEvents.setVisibility(View.VISIBLE);
        noServicesContainer.setVisibility(View.GONE);
    }

    private void displayNoJobs() {
        noAppointmentsContainer.setVisibility(View.INVISIBLE);
        tvUpcomingAppointments.setVisibility(View.GONE);
        linearLayoutDays.setVisibility(View.INVISIBLE);
        noServicesContainer.setVisibility(View.VISIBLE);
    }


    private boolean hasJobs() {
       return place.getJobList().size() > 0;
    }

    public SwipeRefreshLayout.OnRefreshListener  onRefresh() {
         Log.d("refreshed?", "refresehd");
        initUI();
        hasServices();
        invalidateJob();
        fetchAppointments();
        return null;
    }

    private void fetchAppointments() {
        showLoadingOverlay();
        ManagerAppointment.getInstance().getUpcomingAppointments(
                job,
                new ManagerAppointment.OnAppointmentFetchListener() {
                    @Override
                    public void onFetch(List<Appointment> fetchedAppointmentList) {
                        appointmentList = fetchedAppointmentList;
                        onAppointmentsFetch();
                        hideLoadingOverlay();
                    }

                    @Override
                    public void onFailure() {
                        showConnectionError();
                    }
                });
    }

    public void showConnectionError() {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }
        Toast.makeText(getActivity(), requireContext().getString(R.string.error_de_conexion), Toast.LENGTH_SHORT).show();
        hideLoadingOverlay();
    }

    public void onAppointmentsFetch() {
        hideLoadingOverlay();

        if (appointmentList.size() > 0) {
            displayHasUpcomingAppointments(appointmentList);
        } else {
            displayNoUpcomingAppointments();
        }
        swipeRefreshLayout.setRefreshing(false);
        hideLoadingOverlay();
    }

    public void hideLoadingOverlay() {
        if (loadingOverlay != null) {
            loadingOverlay.hide();
        }
    }

    private void showLoadingOverlay() {
        if (loadingOverlay != null) {
            loadingOverlay.show();
        }
    }

    private void displayNoUpcomingAppointments() {
        noAppointmentsContainer.setVisibility(View.VISIBLE);
        tvUpcomingAppointments.setVisibility(View.GONE);
        recyclerViewUpcomingEvents.setVisibility(View.GONE);
    }

    private void displayHasUpcomingAppointments(List<Appointment> appointmentsToday) {
        noAppointmentsContainer.setVisibility(View.GONE);
        tvUpcomingAppointments.setVisibility(View.VISIBLE);
        recyclerViewUpcomingEvents.setVisibility(View.VISIBLE);

        recyclerViewUpcomingEvents.setAdapter(new AdapterRecyclerViewUpcomingEvents(
                appointmentsToday,
                this::startViewAppointmentActivity));
    }

    private void startViewAppointmentActivity(Appointment appointment) {
        Intent intent = new Intent(getContext(), ViewAppointmentActivity.class);
        Bundle bundle = new Bundle();

        bundle.putSerializable("appointment", appointment);
        bundle.putString("placeId", place.getId());
        bundle.putString("jobId", job.getId());

        intent.putExtras(bundle);
        startActivityForResult(intent, RC_VIEW_APPOINTMENT);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if ((requestCode == RC_VIEW_APPOINTMENT || requestCode == RC_MONTH_VIEW) && resultCode == RESULT_OK) {
            onRefresh();
        }
    }

    public void invalidateJob() {
        ManagerAppointment.getInstance().invalidateJob(job.getId());
    }

    private void populateDays() {
        linearLayoutDays.removeAllViews();
        for (int day = 1; day < 8; day++) {
            View v = inflateDayView(day);
            linearLayoutDays.addView(v);
        }
        RecyclerView.LayoutManager layoutManagerUpcoming = new LinearLayoutManager(getContext());
        recyclerViewUpcomingEvents.setLayoutManager(layoutManagerUpcoming);
    }


    @SuppressLint("SetTextI18n")
    private View inflateDayView(final int position) {
        Calendar today = Calendar.getInstance();
        Calendar selectedDay = getCalendar(position);

        View v = LayoutInflater.from(requireContext())
                .inflate(R.layout.week_day_layout, linearLayoutDays, false);

        ((TextView) v.findViewById(R.id.labelDay)).setText(mapNumberDay.get(position));
        ((TextView) v.findViewById(R.id.labelDate)).setText(selectedDay.get(Calendar.DAY_OF_MONTH) + "");

        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMonthViewActivity(position);
            }
        });

        if (selectedDay.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH)) {
            markAsToday(v);
        } else if (hasPassed(today, selectedDay)) {
            markAsPassed(v);
        }

        return v;
    }

    private boolean hasPassed(Calendar today, Calendar selectedDay) {
        return selectedDay.compareTo(today) < 0;
    }

    private void markAsToday(View v) {
        ((TextView) v.findViewById(R.id.labelDay)).setTextColor(getActivity().getColor(R.color.colorPrimary));
        ((TextView) v.findViewById(R.id.labelDate)).setTextColor(getActivity().getColor(R.color.colorPrimary));
    }

    private void markAsPassed(View v) {
        ((TextView) v.findViewById(R.id.labelDay)).setTextColor(getActivity().getColor(R.color.darker_grey));
        ((TextView) v.findViewById(R.id.labelDate)).setTextColor(getActivity().getColor(R.color.darker_grey));
    }

    private void startMonthViewActivity(int position) {
        Calendar selectedDay = getCalendar(position);

        Intent intent = new Intent(requireContext(), MonthViewActivity.class);
        Bundle bundle = new Bundle();

        bundle.putSerializable("selectedDay", selectedDay);
        bundle.putSerializable("jobId", job.getId());
        bundle.putSerializable("placeId", place.getId());

        intent.putExtras(bundle);
        startActivityForResult(intent, RC_MONTH_VIEW);
    }

    private Calendar getCalendar(int position) {
        Calendar selectedDay = Calendar.getInstance();
        int today = selectedDay.get(Calendar.DAY_OF_WEEK);
        selectedDay.add(Calendar.DAY_OF_MONTH, position - today);
        return selectedDay;
    }

    public void setJob(Place place, Job selectedJob) {
        job = selectedJob;
        this.place = place;
        if (hasJobs()){
            initUI();
        }else {
            hideLoadingOverlay();
            displayNoJobs();
        }

    }

    private void createServiceBtn(View view){
        Log.d("fragmentEvents", "createServiceBtn");

        LayoutInflater inflater = (LayoutInflater)
                requireActivity().getSystemService(LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") View popupView = inflater.inflate(R.layout.create_service_popup, null);

        // create the popup window
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.MATCH_PARENT;
        boolean focusable = true;
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);


        popUpBtnCreateService = popupView.findViewById(R.id.create_first_service_popupBtn);
        popUpBtnDecline = popupView.findViewById(R.id.decline_first_service_popupBtn);

        popupView.setElevation(10f);
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);


        popUpBtnCreateService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( canEditJob()) navigateToNewService(); else inflatePopupBtn();
                popupWindow.dismiss();
            }
        });
// dismiss the popup window when touched
        popUpBtnDecline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (popupWindow.isShowing()) popupWindow.dismiss();
            }
        });
    }

    void inflatePopupBtn() {
        LayoutInflater inflater = (LayoutInflater) requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") View popupView = inflater.inflate(R.layout.info_btn_pupup, null);
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.MATCH_PARENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
        popupWindow.showAtLocation(requireView(), Gravity.CENTER, 0, 0);
        ImageButton closePopUp = popupView.findViewById(R.id.imageButton2);
        TextView popupTitle = popupView.findViewById(R.id.info_popup_title);
        TextView popupDescription = popupView.findViewById(R.id.info_popUp_description);
        popupTitle.setText("Error");
        popupDescription.setText("No tienes acceso a esta sección, pídele al dueño de la tienda que te de acceso o que configure los servicios.");
        popupView.setElevation(10f);
        closePopUp.setOnClickListener( view1 -> {
            popupWindow.dismiss();
        });
    }

}