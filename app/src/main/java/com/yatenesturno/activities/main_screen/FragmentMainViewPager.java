package com.yatenesturno.activities.main_screen;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.yatenesturno.Constants;
import com.yatenesturno.R;
import com.yatenesturno.activities.job_edit.EditJobActivity;
import com.yatenesturno.custom_views.LoadingOverlay;
import com.yatenesturno.custom_views.NonSwipeableViewPager;
import com.yatenesturno.database.djangoImpl.DatabaseDjangoRead;
import com.yatenesturno.database.interfaces.ImageLoaderRead;
import com.yatenesturno.database.ImageLoaderReadImpl;
import com.yatenesturno.functionality.ManagerPlace;
import com.yatenesturno.functionality.PlacePremiumManager;
import com.yatenesturno.listeners.DatabaseCallback;
import com.yatenesturno.object_interfaces.Appointment;
import com.yatenesturno.object_interfaces.CustomUser;
import com.yatenesturno.object_interfaces.Job;
import com.yatenesturno.object_interfaces.Place;
import com.yatenesturno.user_auth.UserManagement;
import com.yatenesturno.utils.CalendarUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import cz.msebera.android.httpclient.Header;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Main Fragment containing upcoming appointments, employee administration
 * and clients sub fragments
 */
public class FragmentMainViewPager extends Fragment {

    /**
     * Constants
     */

    public static final String UPCOMING_EVENTS_FRAGMENT = "fragment_upcoming_events";
    public static final String CHAT_FRAGMENT = "chat_fragment";
    public static final String CLIENTS_FRAGMENT = "clients_fragment";
    public static final String PLACE_LIST = "placeList";
    public static final String SELECTED_PLACE = "selectedPlace";
    public static final String SELECTED_JOB = "selectedJob";
    public static final String CLIENT_LIST = "clientList";
    public static final String MAP_JOB_ID_USER = "mapJobIdUser";
    public static final String JOB_IDS = "jobIds";
    public static final String APPOINTMENT_LIST = "appointmentList";
    public static final String PRIMARY_COLOR = "#FF8672";
    public static final String BLACK_COLOR = "#9D9D9D";
    public static final String TAG = "fragmentviewpager";

    /**
     * UI References
     */
    private Toolbar toolbar;
    private BottomSheetDialogFragment dialogChangePlace;
    private NonSwipeableViewPager viewPager;
    private FragmentUpcomingEvents upcomingEventsFragment;
    private FragmentAdminClients chatFragment;
    private FragmentEmployees employeesFragment;
    private LoadingOverlay loadingOverlay;
    private TextView labelTitle;
    private CardView addEmployeesBtn;

    /**
     * Instance variables
     */
    private List<Place> placeList;
    private Place selectedPlace;
    private Job selectedJob;
    private boolean firstTimeListenerBeingCalled;

    Boolean tabLayoutSetted = false;
    Boolean tabSetted = false;
    public FragmentMainViewPager() {

    }

    public Place getSelectedPlace() {
        return selectedPlace;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        if (upcomingEventsFragment != null) {
            FragmentManager fragmentManager = getChildFragmentManager();
            fragmentManager.putFragment(outState, UPCOMING_EVENTS_FRAGMENT, upcomingEventsFragment);
            fragmentManager.putFragment(outState, CHAT_FRAGMENT, chatFragment);
            fragmentManager.putFragment(outState, CLIENTS_FRAGMENT, employeesFragment);
        }

        outState.putAll(saveState());
        hideLoadingOverlay();
    }

    public Bundle saveState() {
        Bundle bundle = new Bundle();

        bundle.putParcelableArrayList(PLACE_LIST, (ArrayList<Place>) placeList);
        bundle.putParcelable(SELECTED_PLACE, selectedPlace);
        bundle.putParcelable(SELECTED_JOB, selectedJob);

        if (chatFragment != null) {
            bundle.putParcelableArrayList(CLIENT_LIST, (ArrayList<CustomUser>) chatFragment.getClientList());
            bundle.putSerializable(MAP_JOB_ID_USER, (HashMap<String, CustomUser>) employeesFragment.getMapJobIdUser());
            bundle.putSerializable(JOB_IDS, (ArrayList<String>) employeesFragment.getJobIds());
            bundle.putParcelableArrayList(APPOINTMENT_LIST, (ArrayList<Appointment>) upcomingEventsFragment.getAppointmentList());
        }

        return bundle;
    }

    private void recoverState(Bundle bundle) {

        FragmentManager fragmentManager = getChildFragmentManager();
        upcomingEventsFragment = (FragmentUpcomingEvents) fragmentManager.getFragment(bundle, UPCOMING_EVENTS_FRAGMENT);
        chatFragment = (FragmentAdminClients) fragmentManager.getFragment(bundle, CHAT_FRAGMENT);
        employeesFragment = (FragmentEmployees) fragmentManager.getFragment(bundle, CLIENTS_FRAGMENT);

        if (chatFragment != null) {
            upcomingEventsFragment.setState(bundle);
            chatFragment.setState(bundle);
            employeesFragment.setState(bundle);
        }
        placeList = bundle.getParcelableArrayList(PLACE_LIST);
        selectedPlace = bundle.getParcelable(SELECTED_PLACE);
        selectedJob = bundle.getParcelable(SELECTED_JOB);
        initUI();
    }



    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_main_view_pager, container, false);
    }
        Boolean inited = false;
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews();

        if (savedInstanceState != null) {
            recoverState(savedInstanceState);
        } else {
            getPlaces();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG,"onResumeonResumeonResume");
        if (inited){
            ManagerPlace.getInstance().invalidate();
            getPlacesNoLoading();
        }

    }

    private void getPlacesNoLoading() {

        ManagerPlace.getInstance().getPlaces(new ManagerPlace.OnPlaceListFetchListener() {
            @Override
            public void onFetch(List<Place> places) {
                placeList = places;
                getPreviouslySelected();
                onPlacesFetch();
                //test now and test invalidate
            }

            @Override
            public void onFailure() {
                Toast.makeText(getContext(), getString(R.string.error_de_conexion), Toast.LENGTH_SHORT).show();
                hideLoadingOverlay();
            }
        });
    }


    public void initViews() {
        loadingOverlay = new LoadingOverlay((CoordinatorLayout) getView());
        viewPager = getView().findViewById(R.id.recyclerViewJobs);
        labelTitle = getView().findViewById(R.id.labelMainViewPagerTitle);
        addEmployeesBtn = getView().findViewById(R.id.addemployeesfragmentMain);
        addEmployeesBtn.setOnClickListener(view -> ((MainActivity) requireActivity()).startJobRequestsActivity());

    }

    public void showLoadingOverlay() {
        if (loadingOverlay != null) {
            loadingOverlay.show();
        }
    }

    private void getPlaces() {
        showLoadingOverlay();
        ManagerPlace.getInstance().getPlaces(new ManagerPlace.OnPlaceListFetchListener() {
            @Override
            public void onFetch(List<Place> places) {
                placeList = places;
                getPreviouslySelected();
                onPlacesFetch();
                //test now and test invalidate
            }

            @Override
            public void onFailure() {
                Toast.makeText(getContext(), getString(R.string.error_de_conexion), Toast.LENGTH_SHORT).show();
                hideLoadingOverlay();
            }
        });
    }

    /**
     * Get previously selected place, whether from notification, or after refreshing
     */
    public void getPreviouslySelected() {
        String jobId = null, placeId = null;
        if (selectedJob != null) {
            jobId = selectedJob.getId();
            placeId = selectedPlace.getId();
        } else if (requireActivity().getIntent().getExtras() != null) {
            Bundle extras = requireActivity().getIntent().getExtras();
            placeId = extras.getString("placeId");
            jobId = extras.getString("jobId");
            Log.d("placeId?", placeId);
        }

        if (jobId != null) {
            selectedPlace = getPlaceForId(placeId);
            if (selectedPlace != null) {
                selectedJob = selectedPlace.getJobById(jobId);
                if (selectedJob == null) {
                    selectedPlace = null;
                }
            }
        }
    }

    private Place getPlaceForId(String id) {
        for (Place place : placeList) {
            if (place.getId().equals(id)) {
                return place;
            }
        }
        return null;
    }



    public void onPlacesFetch() {
        initUI();
        if (hasPlaces()) {
            setSelectedJobToFragments();
        }
        inited = true;

    }

    public void showChangePlaceDialog() {
        dialogChangePlace.show(getChildFragmentManager(), "CHANGE_PLACE");
    }

    private void startEditJobActivity() {

        Intent intent = new Intent(requireContext(), EditJobActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable("job", selectedJob);
        bundle.putParcelable("place", selectedPlace);
        intent.putExtras(bundle);
        requireActivity().startActivityForResult(intent, Constants.RC_EDIT_JOB);

    }

    private void initUI() {
        hideLoadingOverlay();
        toolbar = ((MainActivity) requireActivity()).getToolbar();
        if (hasPlaces()) {
            dialogChangePlace = new FragmentDialogChangePlace(this);
            if (selectedPlace == null) {
                selectedPlace = getInitialPlace();
            }
            displayHasPlaces();
        } else {
            displayNoPlaces();
        }
        initTrialIndicator();
    }

    /**
     * Display or hide trial indicator depending on user trial status
     * <p>
     * If corresponds, display remaining days
     */
    private void initTrialIndicator() {
//        Fetch user trial state
        DatabaseDjangoRead.getInstance().GET(
                "/auth/trial/",
                null,
                new DatabaseCallback() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        CustomUser user = UserManagement.getInstance().getUser();

                        int userTrialRemainingDays = -1;
                        if (response.has("trial") && !response.isNull("trial")) {
                            try {
                                JSONObject trialJson = response.getJSONObject("trial");

                                Calendar trialValidUntil = CalendarUtils.parseDate(trialJson.getString("valid_until"));

                                userTrialRemainingDays = getRemainingDays(trialValidUntil);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        user.setTrialRemainingDays(userTrialRemainingDays);

                        boolean isInTrial = userTrialRemainingDays > -1;

                        CardView trialIndicatorWrapper = getActivity().findViewById(R.id.trialIndicatorWrapper);
                        AppCompatTextView trialIndicator = getActivity().findViewById(R.id.trialIndicator);

                        boolean hasPremiumInPlace = selectedPlace != null && PlacePremiumManager.getInstance().getIsPremium(selectedPlace.getId(), UserManagement.getInstance().getUser().getId());

                        if (isInTrial) {
                            if (userTrialRemainingDays == 0) {
                                trialIndicator.setText(getString(R.string.last_trial_day));
                            } else {
                                trialIndicator.setText(String.format(getString(R.string.trial_period_main_act), userTrialRemainingDays));
                            }
                        } else if (!hasPremiumInPlace) {
                            trialIndicatorWrapper.setCardBackgroundColor(getResources().getColor(R.color.darker_grey, null));
                            trialIndicator.setText(R.string.non_premium_message);
                            trialIndicator.setCompoundDrawables(null, null, null, null);
                        } else {
                            trialIndicator.setText(R.string.premium_message);
                        }

                        trialIndicatorWrapper.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

                    }
                }
        );

    }

    private int getRemainingDays(Calendar validUntil) {
        long millisDiff = validUntil.getTimeInMillis() - Calendar.getInstance().getTimeInMillis();

        return (int) ((millisDiff / 1000) / 60 / 60 / 24);
    }

    private void hideLoadingOverlay() {
        if (loadingOverlay != null) {
            loadingOverlay.hide();
        }
    }

    private boolean hasPlaces() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("firstShop", MODE_PRIVATE);
        sharedPreferences.edit().putBoolean("firstShop", false).apply();
        return placeList.size() > 0;
    }

    private Place getInitialPlace() {
        return placeList.get(0);
    }

    private void displayHasPlaces() {
        initPlaceView();
        toolbar.findViewById(R.id.btnSharePlace).setVisibility(View.VISIBLE);
        toolbar.findViewById(R.id.headerPlace).setVisibility(View.VISIBLE);
        if (selectedPlaceHasJobs()){
            viewPager.setVisibility(View.VISIBLE);
            getView().findViewById(R.id.tabLayout).setVisibility(View.VISIBLE);
        }
        Log.d(TAG,"viewPagerVisibility displayHasPlaces is: " + viewPager.getVisibility() );
        getView().findViewById(R.id.noPlaces).setVisibility(View.GONE);
    }

    private void displayNoPlaces() {

        viewPager.setVisibility(View.GONE);
        Log.d(TAG,"viewPagerVisibility displayNoPlaces is: " + viewPager.getVisibility() );
        TextView labelSlogan = toolbar.findViewById(R.id.labelBusinessName);
        labelSlogan.setVisibility(View.INVISIBLE);
        getView().findViewById(R.id.tabLayout).setVisibility(View.INVISIBLE);
        toolbar.findViewById(R.id.btnSharePlace).setVisibility(View.GONE);
        getView().findViewById(R.id.noPlaces).setVisibility(View.VISIBLE);
        getView().findViewById(R.id.noJobsInPlaceView).setVisibility(View.GONE);
        CardView btnRegisterPlace, btnJobRequest;
        btnRegisterPlace = getView().findViewById(R.id.btnRegisterPlace);
        btnRegisterPlace.setOnClickListener(view -> ((MainActivity) requireActivity()).startNewPlaceActivity());
        btnJobRequest = getView().findViewById(R.id.btnJobRequest);
        btnJobRequest.setOnClickListener(view -> ((MainActivity) requireActivity()).startJobRequestsActivity());
        hideTitle();
    }

    private void hideTitle() {
        labelTitle.setVisibility(View.INVISIBLE);
    }


    private void initPlaceView() {
        if (selectedPlaceHasJobs()) {
            if (selectedJob == null) {
                selectedJob = getFirstJobForPlace();
            }

            if (!tabSetted){
                initJobView(); //Only one
                displayTabLayout();
            }
            displayJobNoTab();
            updateProfileImage();
        } else {
            Log.d(TAG, "displayNoJobsInPlace");
            displayNoJobsInPlace();
        }

        showPlaceInfo();

    }

    /**
     * Setup selected place info in toolbar interface
     */
    private void showPlaceInfo() {
        if (upcomingEventsFragment != null) {
            upcomingEventsFragment.setJob(selectedPlace,selectedJob);
        }
        // Business name
        TextView labelBusinessName = toolbar.findViewById(R.id.labelBusinessName);
        labelBusinessName.setVisibility(View.VISIBLE);
        labelBusinessName.setText(selectedPlace.getBusinessName());
        toolbar.findViewById(R.id.labelBusinessName).setOnClickListener(v -> showChangePlaceDialog());
        labelBusinessName.setSelected(true);
    }

    private boolean selectedPlaceHasJobs() {
        return selectedPlace.getJobList().size() > 0;
    }



    @SuppressLint("SetTextI18n")

    private void displayTabLayout() {
        getView().findViewById(R.id.noJobsInPlaceView).setVisibility(View.GONE);
        LayoutInflater inflater = (LayoutInflater) requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View tab0 = (View) inflater.inflate(R.layout.tablayout_custom_view, null);
        ImageView icon0 = (ImageView) tab0.findViewById(R.id.icon_tab_layout_cv);
        TextView title0 = (TextView) tab0.findViewById(R.id.text_tab_layout);
        icon0.setImageDrawable(requireActivity().getDrawable(R.drawable.ic_turnos_diarios));
        title0.setText("Turnos diarios");
        title0.setTextColor(Color.parseColor(PRIMARY_COLOR));


        View tab1 = (View) inflater.inflate(R.layout.tablayout_custom_view, null);
        ImageView icon1 = (ImageView) tab1.findViewById(R.id.icon_tab_layout_cv);
        TextView title1 = (TextView) tab1.findViewById(R.id.text_tab_layout);
        icon1.setImageDrawable(requireActivity().getDrawable(R.drawable.ic_clients_tablayout));
        title1.setText("Clientes");


        View tab2 = (View) inflater.inflate(R.layout.tablayout_custom_view, null);
        ImageView icon2 = (ImageView) tab2.findViewById(R.id.icon_tab_layout_cv);
        TextView title2 = (TextView) tab2.findViewById(R.id.text_tab_layout);
        icon2.setImageDrawable(requireActivity().getDrawable(R.drawable.ic_profesionales_tablayout));
        title2.setText("Profesionales");
        final TabLayout tabLayout = requireView().findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);
        if(!tabLayoutSetted){
            tabLayout.getTabAt(0).setCustomView(tab0);
            tabLayout.getTabAt(1).setCustomView(tab1);
            tabLayout.getTabAt(2).setCustomView(tab2);
        }

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {

                    case 0:

                        icon0.setImageTintList(ColorStateList.valueOf(Color.parseColor(PRIMARY_COLOR)));
                        title0.setTextColor(Color.parseColor(PRIMARY_COLOR));
                        title1.setTextColor(Color.parseColor(BLACK_COLOR));
                        title2.setTextColor(Color.parseColor(BLACK_COLOR));
                        icon1.setImageTintList(ColorStateList.valueOf(Color.parseColor(BLACK_COLOR)));
                        icon2.setImageTintList(ColorStateList.valueOf(Color.parseColor(BLACK_COLOR)));
                        if (tabLayout.getTabAt(1).getCustomView() == null) {
                            tabLayout.getTabAt(1).setCustomView(tab1);
                        } else if (tabLayout.getTabAt(2).getCustomView() == null) {
                            tabLayout.getTabAt(2).setCustomView(tab2);
                        }else if(tab.getCustomView() == null){
                            tab.setCustomView(tab0);
                        }
                        break;
                    case 1:


                        icon1.setImageTintList(ColorStateList.valueOf(Color.parseColor(PRIMARY_COLOR)));
                        title1.setTextColor(Color.parseColor(PRIMARY_COLOR));


                        title0.setTextColor(Color.parseColor(BLACK_COLOR));
                        title2.setTextColor(Color.parseColor(BLACK_COLOR));
                        icon0.setImageTintList(ColorStateList.valueOf(Color.parseColor(BLACK_COLOR)));
                        icon2.setImageTintList(ColorStateList.valueOf(Color.parseColor(BLACK_COLOR)));


                        if (tabLayout.getTabAt(0).getCustomView() == null) {
                            tabLayout.getTabAt(0).setCustomView(tab0);
                        } else if (tabLayout.getTabAt(2).getCustomView() == null) {
                            tabLayout.getTabAt(2).setCustomView(tab2);
                        }else if(Objects.requireNonNull(tabLayout.getTabAt(1)).getCustomView() == null){
                            tab.setCustomView(tab1);
                        }
                        break;
                    case 2:

                        title2.setTextColor(Color.parseColor(PRIMARY_COLOR));
                        icon2.setImageTintList(ColorStateList.valueOf(Color.parseColor(PRIMARY_COLOR)));
                        title0.setTextColor(Color.parseColor(BLACK_COLOR));
                        title1.setTextColor(Color.parseColor(BLACK_COLOR));
                        icon0.setImageTintList(ColorStateList.valueOf(Color.parseColor(BLACK_COLOR)));
                        icon1.setImageTintList(ColorStateList.valueOf(Color.parseColor(BLACK_COLOR)));

                        if (tabLayout.getTabAt(0).getCustomView() == null) {
                            tabLayout.getTabAt(0).setCustomView(tab0);
                        } else if (tabLayout.getTabAt(1).getCustomView() == null) {
                            tabLayout.getTabAt(1).setCustomView(tab1);
                        }else if(tabLayout.getTabAt(2) == null){
                            tab.setCustomView(tab2);
                        }


                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        tabLayoutSetted = true;
        tabSetted = true;
    }


    public void displayJobNoTab() {

        initJobsSpinner();
        employeesFragment.setPlace(selectedPlace);
        viewPager.setVisibility(View.VISIBLE);
        Log.d(TAG,"viewPagerVisibility displayJobNoTab is: " + viewPager.getVisibility() );
        requireView().findViewById(R.id.noJobsInPlaceView).setVisibility(View.GONE);
        requireView().findViewById(R.id.headerJob).setVisibility(View.VISIBLE);
        requireView().findViewById(R.id.tabLayout).setVisibility(View.VISIBLE);
        requireView().findViewById(R.id.labelJob).setVisibility(View.VISIBLE);
        requireView().findViewById(R.id.headerJob).setVisibility(View.VISIBLE);
        if (canEditJob()) {
            showEditJobView();
        } else {
            hideEditJobView();
        }


    }

    private void displayNoJobsInPlace() {
        viewPager.setVisibility(View.GONE);
        Log.d(TAG,"viewPagerVisibility displayNoJobsInPlace is: " + viewPager.getVisibility() );

        getView().findViewById(R.id.tabLayout).setVisibility(View.INVISIBLE);
        getView().findViewById(R.id.noJobsInPlaceView).setVisibility(View.VISIBLE);
        getView().findViewById(R.id.noPlaces).setVisibility(View.GONE);

        getView().findViewById(R.id.labelJob).setVisibility(View.GONE);
        getView().findViewById(R.id.spinnerJob).setVisibility(View.GONE);
        getView().findViewById(R.id.btnEditJob).setVisibility(View.GONE);
        getView().findViewById(R.id.headerJob).setVisibility(View.GONE);
    }

    private void initJobView() {
        Log.d(TAG,"INITJOBVIEW CANT BE MORE 2 VECES");
        initViewPager();
        initEditJob();
        updateProfileImage();
        setCopyLinkBtnListener();
    }


    private void initEditJob() {
        if (canEditJob()) {
            showEditJobView();
        } else {
            hideEditJobView();
        }
    }

    private void hideEditJobView() {
        if (selectedJob.canEdit()) {
            getView().findViewById(R.id.spinnerJob).setVisibility(View.GONE);
            getView().findViewById(R.id.btnEditJob).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.btnEditJob).setOnClickListener(v -> startEditJobActivity());
        } else {
            getView().findViewById(R.id.btnEditJob).setVisibility(View.GONE);
            getView().findViewById(R.id.spinnerJob).setVisibility(View.GONE);
        }
    }

    private void showEditJobView() {
        //getView().findViewById(R.id.spinnerJob).setVisibility(View.VISIBLE);

        getView().findViewById(R.id.btnEditJob).setVisibility(View.VISIBLE);
        getView().findViewById(R.id.btnEditJob).setOnClickListener(v -> startEditJobActivity());
    }


    private boolean canEditJob() {
        Boolean isPlaceOwner = selectedPlace.getOwner().getId().equals(UserManagement.getInstance().getUser().getId());

        Log.d(TAG, isPlaceOwner.toString());

        return isPlaceOwner;

    }


    private void initJobsSpinner() {
        Log.d(TAG, "initJobsSpinner");

        TextView labelJob = getView().findViewById(R.id.labelJob);
        Spinner spinner = getView().findViewById(R.id.spinnerJob);

        if (selectedPlace.getJobList().size() == 1) {
            Log.d(TAG, "obsSpinner size 1");

            spinner.setVisibility(View.GONE);
            labelJob.setText(selectedJob.getEmployee().getName());
        } else {
            Log.d(TAG, "obsSpinner size != 1");
            spinner.setVisibility(View.VISIBLE);
            spinner.setAdapter(new JobsArrayAdapter(getContext(), selectedPlace.getJobList()));
            setSpinnerLabelWithSelectedJob(spinner);
            firstTimeListenerBeingCalled = true;
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (!firstTimeListenerBeingCalled) {
                        selectedJob = selectedPlace.getJobList().get(position);
                        onSelectedJobChanged();
                    } else {
                        firstTimeListenerBeingCalled = false;
                    }
                    labelJob.setText(selectedJob.getEmployee().getName());
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    labelJob.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    private void setSpinnerLabelWithSelectedJob(Spinner spinner) {
        int selectedIndex = 0;
        for (Job job : selectedPlace.getJobList()) {
            if (job.getId().equals(selectedJob.getId())) {
                break;
            }
            selectedIndex++;
        }
        spinner.setSelection(selectedIndex);
    }

    public void onSelectedJobChanged() {

        initEditJob();
        updateProfileImage();
        setSelectedJobToFragments();

        if (canEditJob()) {
            showEditJobView();
        } else {
            hideEditJobView();
        }
        hideLoadingOverlay();
    }

    public void changePlace(Place place) {
        if (hasChanged(place)) {
            Log.e(TAG + "oldPlace", selectedPlace.getBusinessName());
            Log.e(TAG +"newPlace", place.getBusinessName());
            selectedPlace = place;
            selectedJob = getFirstJobForPlace();
            initPlaceView();
            setSelectedJobToFragments();
        }
    }

    private Job getFirstJobForPlace() {
        if (selectedPlaceHasJobs()) {
            CustomUser loggedUser = UserManagement.getInstance().getUser();
            for (Job job : selectedPlace.getJobList()) {
                if (job.getEmployee().getId().equals(loggedUser.getId())) {
                    return job;
                }
            }
            return selectedPlace.getJobList().get(0);
        }
        return null;
    }

    private void initViewPager() {
        viewPager.setEnableSmoothAnimation(false);
        viewPager.setOffscreenPageLimit(4);

        MainFragmentsPagerAdapter pagerAdapter = new MainFragmentsPagerAdapter(getChildFragmentManager());
        if (upcomingEventsFragment == null) {
            upcomingEventsFragment = new FragmentUpcomingEvents();
            employeesFragment = new FragmentEmployees();
            chatFragment = new FragmentAdminClients();
        }

        pagerAdapter.addFragment(upcomingEventsFragment);
        pagerAdapter.addFragment(chatFragment);
        pagerAdapter.addFragment(employeesFragment);

        viewPager.setAdapter(pagerAdapter);
    }

    private void setSelectedJobToFragments() {
        if (selectedJob != null) {
            if (upcomingEventsFragment != null) {
                upcomingEventsFragment.setJob(selectedPlace, selectedJob);
            }
            if (chatFragment != null) {
                chatFragment.setJob(selectedPlace, selectedJob);
            }
        }
    }

    public void refresh() {
        //job
        ManagerPlace.getInstance().invalidate();
        getPlaces();
        if (upcomingEventsFragment != null) {
            upcomingEventsFragment.setJob(selectedPlace,selectedJob);
            upcomingEventsFragment.hasServices();
        }

        Log.e(TAG +"refreshPlace", selectedPlace.getBusinessName());
    }

    private void copyLinkToClipBoard() {
        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("link del lugar", getPlaceLink());
        clipboard.setPrimaryClip(clip);
        Snackbar.make(requireView(), "Enlace copiado al portapapeles", Snackbar.LENGTH_SHORT).show();
    }

    private CharSequence getPlaceLink() {
        return "https://yatenesturno.com.ar/place/" + selectedPlace.getId();
    }

    private void updateProfileImage() {
        ImageLoaderRead imageLoaderRead = new ImageLoaderReadImpl();
        imageLoaderRead.getImage(requireContext(), selectedJob.getEmployee(), new ImageLoaderRead.OnGetImageListener() {
            @Override
            public void onSuccess(Bitmap bitmap) {
                CircleImageView circleImageView = getView().findViewById(R.id.ivJob);
                circleImageView.setImageBitmap(bitmap);
            }

            @Override
            public void onFailure() {

            }
        });
    }

    public void setCopyLinkBtnListener() {
        toolbar.findViewById(R.id.btnSharePlace).setOnClickListener(v -> copyLinkToClipBoard());
    }


    private boolean hasChanged(Place placeFrom) {
        return selectedPlace != placeFrom;
    }
    private static class MainFragmentsPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> fragmentList;

        public MainFragmentsPagerAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
            fragmentList = new ArrayList<>();
        }

        public void addFragment(Fragment fragment) {
            fragmentList.add(fragment);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }
    }

    private class JobsArrayAdapter extends ArrayAdapter<Job> {

        private final List<Job> jobs;

        public JobsArrayAdapter(Context context, List<Job> jobs) {
            super(context, R.layout.spinner_job_layout, jobs);
            this.jobs = jobs;
        }

        @Override
        public View getDropDownView(int position, View convertView,
                                    ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        public View getCustomView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.spinner_job_layout, parent, false);
            }
            convertView.setPadding(0, convertView.getPaddingTop(), convertView.getPaddingRight(), convertView.getPaddingBottom());
            TextView label = convertView.findViewById(R.id.labelJobName);
            label.setText(jobs.get(position).getEmployee().getName());

            return convertView;
        }

    }
}