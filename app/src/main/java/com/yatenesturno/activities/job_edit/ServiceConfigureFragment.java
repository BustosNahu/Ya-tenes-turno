package com.yatenesturno.activities.job_edit;

import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.yatenesturno.R;
import com.yatenesturno.activities.job_edit.service_configs.ServiceConfigCoordinator;
import com.yatenesturno.activities.job_edit.service_configs.ValidationResult;
import com.yatenesturno.activities.job_edit.service_configs.configurations.BookingConfiguration;
import com.yatenesturno.activities.job_edit.service_configs.configurations.ClassConfiguration;
import com.yatenesturno.activities.job_edit.service_configs.configurations.ConcurrencyConfiguration;
import com.yatenesturno.activities.job_edit.service_configs.configurations.ConcurrentServicesConfiguration;
import com.yatenesturno.activities.job_edit.service_configs.configurations.CreditsConfiguration;
import com.yatenesturno.activities.job_edit.service_configs.configurations.DaysAndDurationConfiguration;
import com.yatenesturno.activities.job_edit.service_configs.configurations.EmergencyConfiguration;
import com.yatenesturno.activities.job_edit.service_configs.configurations.FixedScheduleConfiguration;
import com.yatenesturno.activities.job_edit.service_configs.configurations.IntervalConfiguration;
import com.yatenesturno.activities.job_edit.service_configs.configurations.PriceConfiguration;
import com.yatenesturno.activities.job_edit.service_configs.configurations.ReminderConfiguration;
import com.yatenesturno.activities.job_edit.service_configs.configurations.ServiceConfiguration;
import com.yatenesturno.activities.job_edit.service_configs.configurations.TimeSlotConfiguration;
import com.yatenesturno.custom_views.LoadingButton;
import com.yatenesturno.object_interfaces.DaySchedule;
import com.yatenesturno.object_interfaces.Job;
import com.yatenesturno.object_interfaces.Place;
import com.yatenesturno.object_interfaces.Service;
import com.yatenesturno.object_interfaces.ServiceInstance;
import com.yatenesturno.objects.DayScheduleImpl;
import com.yatenesturno.objects.ServiceInstanceImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

public class ServiceConfigureFragment extends Fragment implements ServiceConfigCoordinator {

    /**
     * Argument keys
     */
    public static final String JOB_KEY = "job";
    public static final String PLACE_KEY = "place";
    public static final String SERVICE_KEY = "service";

    /**
     * Instance variables
     */
    private Job job;
    private Place place;
    private List<ServiceConfiguration> serviceConfigurationList;

    private ListenerConfirm listenerBtnConfirm;
    private Service service;

    private boolean hasReachedBottomOfScrollView;
    private LoadingButton btnConfirm;
    private ScrollView scrollView;
    private ConcurrentServicesConfiguration concurrentServicesConfiguration;
    private PriceConfiguration priceConfiguration;
    private ConcurrencyConfiguration concurrencyConfiguration;
    private EmergencyConfiguration emergencyConfiguration;
    private CreditsConfiguration creditsConfiguration;
    private DaysAndDurationConfiguration daysAndDurationConfiguration;
    private ReminderConfiguration reminderConfiguration;
    private FixedScheduleConfiguration fixedScheduleConfiguration;
    private ClassConfiguration classConfiguration;
    private IntervalConfiguration intervalConfiguration;
    private TimeSlotConfiguration timeSlotConfiguration;
    private BookingConfiguration bookingConfiguration;

    public ServiceConfigureFragment(Place place, Job job) {
        this.job = job;
        this.place = place;
    }

    public ServiceConfigureFragment() {

    }

    public void setBtnConfirmListener(ListenerConfirm listenerBtnConfirm) {
        this.listenerBtnConfirm = listenerBtnConfirm;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_service_configure, container, false);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putAll(saveState());
    }

    private Bundle saveState() {
        Bundle bundle = new Bundle();

        bundle.putParcelable(JOB_KEY, job);
        bundle.putParcelable(PLACE_KEY, place);
        bundle.putParcelable(SERVICE_KEY, service);

        return bundle;
    }

    private void recoverState(Bundle bundle) {
        job = bundle.getParcelable(JOB_KEY);
        place = bundle.getParcelable(PLACE_KEY);
        service = bundle.getParcelable(SERVICE_KEY);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        new Handler(Looper.myLooper()).postDelayed(() -> {
            initViews();
            if (savedInstanceState != null) {
                recoverState(savedInstanceState);
            }
            initConfigurations();

        }, 200);
    }

    private void initConfigurations() {
        serviceConfigurationList = new ArrayList<>();

        priceConfiguration = getView().findViewById(R.id.priceConfiguration);
        concurrencyConfiguration = getView().findViewById(R.id.concurrencyConfiguration);
        emergencyConfiguration = getView().findViewById(R.id.emergencyConfiguration);
        creditsConfiguration = getView().findViewById(R.id.creditsConfiguration);
        daysAndDurationConfiguration = getView().findViewById(R.id.daysAndDurationConfiguration);
        reminderConfiguration = getView().findViewById(R.id.reminderConfiguration);
        concurrentServicesConfiguration = getView().findViewById(R.id.concurrentServicesConfiguration);
        fixedScheduleConfiguration = getView().findViewById(R.id.fixedScheduleConfiguration);
        classConfiguration = getView().findViewById(R.id.classConfiguration);
        intervalConfiguration = getView().findViewById(R.id.intervalConfiguration);
        timeSlotConfiguration = getView().findViewById(R.id.timeSlotConfiguration);
        bookingConfiguration = getView().findViewById(R.id.bookingConfiguration);

        daysAndDurationConfiguration.setJob(job);
        reminderConfiguration.setPlaceId(place.getId());

        serviceConfigurationList.add(priceConfiguration);
        serviceConfigurationList.add(concurrencyConfiguration);
        serviceConfigurationList.add(emergencyConfiguration);
        serviceConfigurationList.add(creditsConfiguration);
        serviceConfigurationList.add(daysAndDurationConfiguration);
        serviceConfigurationList.add(reminderConfiguration);
        serviceConfigurationList.add(concurrentServicesConfiguration);
        serviceConfigurationList.add(fixedScheduleConfiguration);
        serviceConfigurationList.add(classConfiguration);
        serviceConfigurationList.add(intervalConfiguration);
        serviceConfigurationList.add(timeSlotConfiguration);
        serviceConfigurationList.add(bookingConfiguration);

        for (ServiceConfiguration sc : serviceConfigurationList) {
            sc.setConfigCoordinator(this);
        }

        for (ServiceConfiguration sc : serviceConfigurationList) {
            sc.reset();
        }
    }

    @Override
    public List<ServiceConfiguration> getServiceConfigurationList() {
        return serviceConfigurationList;
    }

    public void initViews() {
        scrollView = getView().findViewById(R.id.scrollViewConfigService);
        btnConfirm = getView().findViewById(R.id.btnConfirmServiceConfig);
        btnConfirm.setMatchParent();

        setBtnConfirmListener();
        setScrollViewScrollListener();
        initMoreInfo();
    }


    private void initMoreInfo() {
        new MoreInfoHelper((ViewGroup) getView(), getChildFragmentManager());
    }


    private void setScrollViewScrollListener() {
        hasReachedBottomOfScrollView = false;
        scrollView.setVerticalFadingEdgeEnabled(true);
        if (isScrollViewAtBottom() || !canScroll()) {
            hasReachedBottomOfScrollView = true;
            setBtnToConfirm();
        } else {
            setBtnToScrollDown();
            scrollView.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                if (isScrollViewAtBottom()) {
                    setBtnToConfirm();
                    hasReachedBottomOfScrollView = true;
                }
            });
        }
    }

    private boolean canScroll() {
        View child = scrollView.getChildAt(0);

        if (child != null) {
            int childHeight = child.getHeight();
            return scrollView.getHeight() < childHeight + scrollView.getPaddingTop() + scrollView.getPaddingBottom() + 10;
        }

        return false;
    }

    public boolean isScrollViewAtBottom() {
        Rect scrollBounds = new Rect();
        scrollView.getHitRect(scrollBounds);
        return getView().findViewById(R.id.labelSchedule).getLocalVisibleRect(scrollBounds);
    }

    private void setBtnToScrollDown() {
        btnConfirm.setText(getString(R.string.scroll_down));
    }

    private void setBtnToConfirm() {
        btnConfirm.setText(getString(R.string.confirm));
    }

    public void setBtnConfirmListener() {
        btnConfirm.setOnClickListener(view -> onBtnConfirmClicked());
    }

    public void onBtnConfirmClicked() {
        btnConfirm.hideLoading();
        if (hasReachedBottomOfScrollView) {
            configureJob();
        } else {
            hasReachedBottomOfScrollView = true;
            setBtnToConfirm();
            scrollDown();
        }
    }

    private void scrollDown() {
        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
    }

    private void scrollUp() {
        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_UP));
    }

    private void refreshUI() {
        setScrollViewScrollListener();

        ServiceInstance serviceInstance = getServiceInstance();
        if (serviceInstance != null) {
            for (ServiceConfiguration sc : serviceConfigurationList) {
                sc.initFromServiceInstance(serviceInstance);
            }
        } else {
            for (ServiceConfiguration sc : serviceConfigurationList) {
                sc.reset();
            }
        }
    }

    private ServiceInstance getServiceInstance() {
        return job.getServiceInstanceForServiceId(service.getId());
    }

    public void setServiceToConfigure(Service service, List<ServiceInstance> otherProvidedServices) {
        this.service = service;
        concurrentServicesConfiguration.setOtherProvidedServices(otherProvidedServices);
        this.hasReachedBottomOfScrollView = false;

        refreshUI();
    }

    private void configureJob() {
        if (validateData()) {
            createOrUpdateServiceInstance();
        }
    }

    private boolean validateData() {
        boolean error = false;

        for (ServiceConfiguration sc : serviceConfigurationList) {
            if (!error & sc.isVisible()) {
                ValidationResult result = sc.validate();

                if (!result.getResult()) {
                    showErrorForConfiguration(sc, result);
                    error = true;

                } else {
                    sc.removeError();
                }
            } else {
                sc.removeError();
            }
        }

        return !error;
    }

    private void showErrorForConfiguration(ServiceConfiguration configuration, ValidationResult result) {
        scrollView.smoothScrollTo(0, configuration.getTop());

        showSnackBar(result.getMessageResourceId());
        configuration.setError();
    }

    private void createOrUpdateServiceInstance() {
        ServiceInstance serviceInstance = getServiceInstance();
        if (serviceInstance == null) {
            serviceInstance = createServiceInstance();
        }

        updateServiceInstance(serviceInstance);

        List<Integer> selectedDays;
        selectedDays = getSelectedDays();
        Log.d("step3serviceOLDCONFIGURE", selectedDays.toString());

        for (Integer day : selectedDays) {
            DaySchedule ds = getOrCreateDaySchedule(day);
            updateServiceInstanceInDaySchedule(serviceInstance, ds);
            Log.d("step3serviceOLDCONFIGURE DS", ds.toString());

        }

        finishConfiguration();
    }

    private List<Integer> getSelectedDays() {
        if (emergencyConfiguration.isEmergency()) {
            return Arrays.asList(1, 2, 3, 4, 5, 6, 7);
        }
        return daysAndDurationConfiguration.getSelectedDays();
    }

    private DaySchedule getOrCreateDaySchedule(Integer day) {
        DaySchedule ds = job.getDaySchedule(day);
        if (ds == null) {
            ds = createDaySchedule(day);
            job.addDaySchedule(ds);
        }
        return ds;
    }

    private void updateServiceInstanceInDaySchedule(ServiceInstance serviceInstance, DaySchedule ds) {
        Iterator<ServiceInstance> it = ds.getServiceInstances().iterator();
        ServiceInstance currentSI;

        while (it.hasNext()) {
            currentSI = it.next();
            if (currentSI.getService().getId().equals(serviceInstance.getService().getId())) {
                it.remove();
                break;
            }
        }

        ds.getServiceInstances().add(serviceInstance);
    }

    public void removeService() {
        for (DaySchedule ds : job.getDaySchedules()) {
            ServiceInstance si = ds.getServiceInstanceForService(service.getId());
            if (si != null) {
                ds.removeServiceInstance(si);
            }
        }
        removeEmptyDaySchedules();
        listenerBtnConfirm.onConfirmServiceConfiguration();
    }

    private DaySchedule createDaySchedule(int day) {
        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();

        start.set(Calendar.HOUR_OF_DAY, 8);
        start.set(Calendar.MINUTE, 0);
        end.set(Calendar.HOUR_OF_DAY, 20);
        end.set(Calendar.MINUTE, 0);

        return new DayScheduleImpl(
                null, day, start, end, null, null, null
        );
    }

    private ServiceInstance createServiceInstance() {
        return new ServiceInstanceImpl(null, service);
    }

    /**
     * Update service instance configuration by retrieving configurations setup
     *
     * @param si ServiceInstance to update
     */
    private void updateServiceInstance(ServiceInstance si) {
        si.setPrice(Float.parseFloat(priceConfiguration.getPrice()));
        si.setDuration(daysAndDurationConfiguration.getDuration());
        si.setConcurrency(concurrencyConfiguration.getConcurrency());
        si.setConcurrentServices(concurrentServicesConfiguration.getSelectedConcurrentServices());

        si.setReminderSet(reminderConfiguration.isReminder());
        si.setReminderInterval(reminderConfiguration.getReminderInterval());
        si.setEmergency(emergencyConfiguration.isEmergency());
        si.setCredits(creditsConfiguration.isCredits());
        si.setCanBookWithoutCredits(creditsConfiguration.getCanBookWithoutCredits());

        if (si.isEmergency()) {
            si.setMaxAppsSimultaneously(-1);
            si.setMaxAppsPerDay(-1);
        } else {
            si.setMaxAppsSimultaneously(bookingConfiguration.getMaxAppsSimultaneously());
            si.setMaxAppsPerDay(bookingConfiguration.getMaxAppsPerDay());
        }

        if (classConfiguration.isClass()) {
            si.setFixedSchedule(true);
            si.setClassType(true);
            si.setStartTime(null);
            si.setEndTime(null);
            si.setClassTimes(classConfiguration.getClassTimes());
        } else {
            si.setClassType(false);
            si.setFixedSchedule(fixedScheduleConfiguration.isFixedSchedule());
            si.setStartTime(timeSlotConfiguration.getStartTime());
            si.setEndTime(timeSlotConfiguration.getEndTime());
            si.setClassTimes(null);
        }

        if (!fixedScheduleConfiguration.isFixedSchedule()) {
            si.setInterval(intervalConfiguration.getInterval());
        }
    }

    private void finishConfiguration() {
        applyConcurrencyToOtherProvidedServices();
        removeUnselectedDays();
        removeEmptyDaySchedules();
        listenerBtnConfirm.onConfirmServiceConfiguration();
    }

    @Override
    public void onPause() {
        super.onPause();
        scrollUp();
    }

    private void applyConcurrencyToOtherProvidedServices() {
        for (Service concurrentService : concurrentServicesConfiguration.getSelectedConcurrentServices()) {
            for (ServiceInstance si : concurrentServicesConfiguration.getOtherProvidedServices()) {
                if (si.getService().equals(concurrentService)) {
                    if (!si.getConcurrentServices().contains(service)) {
                        si.getConcurrentServices().add(service);
                    }
                    break;
                }
            }
        }

        for (ServiceInstance serviceInstance : concurrentServicesConfiguration.getOtherProvidedServices()) {
            if (serviceInstance.getConcurrentServices().contains(service) &&
                    !concurrentServicesConfiguration.getSelectedConcurrentServices().contains(serviceInstance.getService())) {

                serviceInstance.getConcurrentServices().remove(service);
            }
        }

    }

    private void removeEmptyDaySchedules() {
        List<DaySchedule> toRemove = new ArrayList<>();
        for (DaySchedule ds : job.getDaySchedules()) {
            if (ds.getServiceInstances().isEmpty()) {
                toRemove.add(ds);
            }
        }

        for (DaySchedule ds : toRemove) {
            job.getDaySchedules().remove(ds);
        }
    }

    private void removeUnselectedDays() {
        for (DaySchedule ds : job.getDaySchedules()) {
            ServiceInstance si = ds.getServiceInstanceForService(service.getId());
            if (si != null && !getSelectedDays().contains(ds.getDayOfWeek())) {
                ds.removeServiceInstance(si);
            }
        }
    }

    private void showSnackBar(int stringResourceId) {
        Snackbar.make(requireView(), getString(stringResourceId), Snackbar.LENGTH_SHORT).show();
    }

    public interface ListenerConfirm {
        void onConfirmServiceConfiguration();
    }

}