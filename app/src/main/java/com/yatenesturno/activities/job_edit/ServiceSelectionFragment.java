package com.yatenesturno.activities.job_edit;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yatenesturno.Constants;
import com.yatenesturno.R;
import com.yatenesturno.custom_views.LoadingOverlay;
import com.yatenesturno.database.djangoImpl.DatabaseDjangoRead;
import com.yatenesturno.listeners.DatabaseCallback;
import com.yatenesturno.object_interfaces.Job;
import com.yatenesturno.object_interfaces.Service;
import com.yatenesturno.object_interfaces.ServiceInstance;
import com.yatenesturno.object_views.ServiceObject;
import com.yatenesturno.object_views.ViewService;
import com.yatenesturno.serializers.BuilderListService;
import com.yatenesturno.utils.TaskRunner;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;
import eu.davidea.flexibleadapter.FlexibleAdapter;


public class ServiceSelectionFragment extends Fragment {

    public static final String SERVICE_LIST_KEY = "serviceIsSelectedMap";
    public static final String JOB_KEY = "job";
    private Job job;

    private ServiceClickListener serviceClickListener;
    private FlexibleAdapter<ViewService> serviceListAdapter;
    private RecyclerView recyclerViewServices;
    private SearchView searchViewDoableServices;
    private LoadingOverlay loadingOverlay;
    private List<Service> serviceList;
    private List<ServiceObject> serviceObjectList;

    public ServiceSelectionFragment(Job job) {
        this.job = job;
    }



    public void setServiceClickListener(ServiceClickListener serviceClickListener) {
        this.serviceClickListener = serviceClickListener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_service_selection, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        if (savedInstanceState == null) {
            fetchServices();
        } else {
            recoverState(savedInstanceState);
        }

    }

    private void recoverState(Bundle bundle) {
        serviceList = (List<Service>) bundle.getSerializable(SERVICE_LIST_KEY);
        job = bundle.getParcelable(JOB_KEY);

        populateServices();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putAll(saveState());
    }

    private Bundle saveState() {
        Bundle bundle = new Bundle();

        ArrayList<Service> serviceList = new ArrayList<>();

        for (ViewService viewService : serviceListAdapter.getCurrentItems()) {
            serviceList.add(viewService.getService());
        }

        bundle.putParcelable(JOB_KEY, job);
        bundle.putSerializable(SERVICE_LIST_KEY, serviceList);

        return bundle;
    }

    public void initViews(View view) {
        searchViewDoableServices = view.findViewById(R.id.searchViewDoableServices);
        recyclerViewServices = view.findViewById(R.id.recyclerViewDoableServices);
        RecyclerView.LayoutManager layoutManagerServices = new LinearLayoutManager(requireContext());
        recyclerViewServices.setLayoutManager(layoutManagerServices);

        loadingOverlay = new LoadingOverlay(view.findViewById(R.id.containerServices));
    }

    public void showLoadingOverlay() {
        if (loadingOverlay != null) {
            loadingOverlay.show();
        }
    }

    private void fetchServices() {
        if (job != null) {
            showLoadingOverlay();
            new Handler(Looper.myLooper()).postDelayed(() -> {
                Map<String, String> body = new HashMap<>();
                body.put("job_id", job.getId());

                DatabaseDjangoRead.getInstance().GET(
                        Constants.DJANGO_URL_DOABLE_SERVICES,
                        body,
                        new CallbackGetDoableServices()
                );
            }, 300);
        }
    }

    private void populateServices() {
        List<ViewService> viewServiceList = new ArrayList<>();
        serviceObjectList = new ArrayList<>();

        Collections.sort(serviceList, new ComparatorIsServiceProvided());
        for (Service s : serviceList){
            serviceObjectList.add(new ServiceObject(
                    s,
                    false,
                    providesService(s),
                    isCredits(s),
                    false,
                    isEmergency(s)
            ));
        }
        for (ServiceObject s : serviceObjectList) {
            viewServiceList.add(new ViewService(s.service,  providesService(s.service), isEmergency(s.service), isCredits(s.service)));
        }
        Log.d("serviceList?", serviceList.toString());

        this.serviceListAdapter = new FlexibleAdapter<>(viewServiceList);
        recyclerViewServices.setAdapter(this.serviceListAdapter);
        this.searchViewDoableServices.setOnQueryTextListener(new ListenerQueryDoableServiceType());
        this.serviceListAdapter.addListener(new RecyclerViewClickListener());
    }

    private boolean providesService(Service s) {
        return getServiceInstanceForService(s) != null;
    }

    private boolean isEmergency(Service s) {
        ServiceInstance si = getServiceInstanceForService(s);
        if (si == null) {
            return false;
        }
        return si.isEmergency();
    }

    private boolean isCredits(Service s) {
        ServiceInstance si = getServiceInstanceForService(s);
        if (si == null) {
            return false;
        }
        return si.isCredits();
    }



    private ServiceInstance getServiceInstanceForService(Service s) {
        return job.getServiceInstanceForServiceId(s.getId());
    }

    public List<ServiceInstance> getProvidedServiceInstances() {
        List<ServiceInstance> servicesBeingDone = new ArrayList<>();
        for (ViewService si : serviceListAdapter.getCurrentItems()) {
            if (si.isProvided()) {
                servicesBeingDone.add(getServiceInstanceForService(si.getService()));
            }
        }
        return servicesBeingDone;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (serviceListAdapter != null) {
            checkForUpdates();
        }
        if (searchViewDoableServices != null) {
            searchViewDoableServices.clearFocus();
        }
    }

    public void checkForUpdates() {
        for (ViewService vs : serviceListAdapter.getCurrentItems()) {
            vs.setProvided(providesService(vs.getService()));
            vs.setEmergency(isEmergency(vs.getService()));
            vs.setCredits(isCredits(vs.getService()));
        }
        serviceListAdapter.notifyDataSetChanged();
    }

    public void hideLoadingOverlay() {
        if (loadingOverlay != null) {
            loadingOverlay.hide();
        }
    }

    public interface ServiceClickListener {
        void onServiceViewClick(Service service);
    }

    private class CallbackGetDoableServices extends DatabaseCallback {
        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
            hideLoadingOverlay();

            new TaskRunner().executeAsync(() -> new BuilderListService().build(response), result -> {
                serviceList = result;
                populateServices();
            });
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
            hideLoadingOverlay();
            Toast.makeText(requireActivity(), getString(R.string.error_de_conexion), Toast.LENGTH_SHORT).show();
        }
    }

    private class ListenerQueryDoableServiceType implements SearchView.OnQueryTextListener {

        @Override
        public boolean onQueryTextSubmit(String query) {
            return onQueryTextChange(query);
        }

        @Override
        public boolean onQueryTextChange(String newText) {

            if (serviceListAdapter.hasNewFilter(newText)) {
                serviceListAdapter.setFilter(newText);
                serviceListAdapter.filterItems(100);
            }

            return true;
        }
    }

    private class RecyclerViewClickListener implements FlexibleAdapter.OnItemClickListener {
        @Override
        public boolean onItemClick(View view, int position) {

            serviceClickListener.onServiceViewClick(serviceListAdapter.getItem(position).getService());
            return false;
        }

    }

    private class ComparatorIsServiceProvided implements java.util.Comparator<Service> {

        @Override
        public int compare(Service s1, Service s2) {
            boolean providesServiceS1 = providesService(s1);
            boolean providesServiceS2 = providesService(s2);

            if (providesServiceS1 && !providesServiceS2) {
                return -1;
            }
            if (providesServiceS2 && !providesServiceS1) {
                return 1;
            }

            return 0;
        }
    }
}