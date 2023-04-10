package com.yatenesturno.activities.employee.client;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.yatenesturno.R;
import com.yatenesturno.activities.get_premium.GetPremiumActivity;
import com.yatenesturno.custom_views.JobDateSelectionButton;
import com.yatenesturno.custom_views.LoadingButton;
import com.yatenesturno.custom_views.ServiceSelectionView;
import com.yatenesturno.functionality.ManagerPlace;
import com.yatenesturno.functionality.place_credits.PlaceCreditsManager;
import com.yatenesturno.object_interfaces.CustomUser;
import com.yatenesturno.object_interfaces.Job;
import com.yatenesturno.object_interfaces.Place;
import com.yatenesturno.object_interfaces.PlaceCredits;
import com.yatenesturno.object_interfaces.ServiceInstance;
import com.yatenesturno.object_views.ViewJobCredit;
import com.yatenesturno.objects.PlaceCreditsImpl;
import com.yatenesturno.user_auth.UserManagement;
import com.yatenesturno.utils.CalendarUtils;
import com.yatenesturno.utils.CustomAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;

/**
 * A simple {@link Fragment} subclass.
 */
public class PlaceClientCreditsFragment extends Fragment {

    /**
     * Parameter arguments
     */
    private static final String ARG_PARAM_PLACE_ID = "place_id";
    private static final String ARG_PARAM_JOB_ID = "job_id";
    private static final String ARG_PARAM_CLIENT = "client";
    private static final String ARG_PARAM_EDIT_RIGHTS = "has_edit_rights";
    private static final String ARG_PARAM_SERVICE_INSTANCE_LIST = "service_instance_list";

    /**
     * Parameters
     */
    private String placeId;
    private String jobId;
    private CustomUser client;
    private boolean hasEditRights;

    /**
     * Instance parameters
     */
    private List<ServiceInstance> serviceInstancesWithCredits;
    private Job job;
    private Calendar selectedStartDate, selectedEndDate;

    /**
     * View references
     */
    private ViewGroup root;
    private ServiceSelectionView serviceSelectionView;
    private JobDateSelectionButton btnSelectStart, btnSelectEnd;
    private ExtendedFloatingActionButton btnAddCredits;
    private LoadingButton btnSearchCredits;
    private RecyclerView recyclerViewCredits;
    private AppCompatTextView labelNoCreditsFound;

    public PlaceClientCreditsFragment() {

    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param placeId       place Id
     * @param jobId         job Id
     * @param client        client
     * @param hasEditRights if viewing as owner or job owner
     * @return A new instance of fragment ClientJobCredits.
     */
    public static PlaceClientCreditsFragment newInstance(String placeId, String jobId, CustomUser client, boolean hasEditRights, List<ServiceInstance> serviceInstanceWithCredits) {
        PlaceClientCreditsFragment fragment = new PlaceClientCreditsFragment();

        Bundle args = new Bundle();

        args.putString(ARG_PARAM_PLACE_ID, placeId);
        args.putString(ARG_PARAM_JOB_ID, jobId);
        args.putParcelable(ARG_PARAM_CLIENT, client);
        args.putBoolean(ARG_PARAM_EDIT_RIGHTS, hasEditRights);
        args.putParcelableArrayList(ARG_PARAM_SERVICE_INSTANCE_LIST, (ArrayList<ServiceInstance>) serviceInstanceWithCredits);

        fragment.setArguments(args);

        return fragment;
    }

    public static PlaceClientCreditsFragment newInstance(String placeId) {
        PlaceClientCreditsFragment fragment = new PlaceClientCreditsFragment();

        Bundle args = new Bundle();

        args.putString(ARG_PARAM_PLACE_ID, placeId);
        args.putString(ARG_PARAM_JOB_ID, null);
        args.putParcelable(ARG_PARAM_CLIENT, null);
        args.putBoolean(ARG_PARAM_EDIT_RIGHTS, true);
        args.putParcelableArrayList(ARG_PARAM_SERVICE_INSTANCE_LIST, null);

        fragment.setArguments(args);

        return fragment;
    }

    public void setClient(CustomUser client) {
        this.client = client;
        reset();
    }

    /**
     * Set services that work with the credits system
     */
    public void setServiceInstancesWithCredits(List<ServiceInstance> serviceInstancesWithCredits) {
        this.serviceInstancesWithCredits = serviceInstancesWithCredits;

        if (serviceInstancesWithCredits.size() > 0) {
            serviceSelectionView.setServices(serviceInstancesWithCredits);
            reset();
        } else {
            showPopupNoServicesWithCredits();
            serviceSelectionView.disable();
        }
    }

    /**
     * Displays a warning acknowledging the user thath they dont
     * have any service provided with credits system
     */
    private void showPopupNoServicesWithCredits() {
        new CustomAlertDialogBuilder(getContext())
                .setTitle(getString(R.string.attention))
                .setMessage(getString(R.string.no_credit_services))
                .setPositiveButton(R.string.accept, (dialogInterface, i) -> getActivity().onBackPressed())
                .setOnDismissListener(dialogInterface -> getActivity().onBackPressed())
                .show();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = null;
        if (savedInstanceState != null) {
            bundle = savedInstanceState;
        } else if (getArguments() != null) {
            bundle = getArguments();
        }

        if (bundle != null) {
            recoverState(bundle);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_place_client_credits, container, false);

        root = view.findViewById(R.id.root);
        serviceSelectionView = view.findViewById(R.id.serviceSelection);
        btnSelectStart = view.findViewById(R.id.btnSelectStartDate);
        btnSelectEnd = view.findViewById(R.id.btnSelectEndDate);
        btnAddCredits = view.findViewById(R.id.btnAddCredits);
        btnSearchCredits = view.findViewById(R.id.btnSearchCredits);
        recyclerViewCredits = view.findViewById(R.id.recyclerViewCredits);
        labelNoCreditsFound = view.findViewById(R.id.labelNoCreditsFound);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews();
    }

    private void initViews() {
        if (serviceInstancesWithCredits != null) {
            serviceSelectionView.setServices(serviceInstancesWithCredits);
        }
        serviceSelectionView.setListener(this::reset);

        btnAddCredits.setOnClickListener(view -> onBtnAddCreditsClick());
        btnSearchCredits.setMatchParent();
        btnSearchCredits.setOnClickListener(view -> fetchCredits());
        recyclerViewCredits.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        if (jobId != null) {
            fetchJob();
        } else {
            job = null;
            initDatesSelection();
        }
    }

    private void fetchJob() {
        ManagerPlace.getInstance().getPlaces(new ManagerPlace.OnPlaceListFetchListener() {
            @Override
            public void onFetch(List<Place> placeList) {
                for (Place currPlace : placeList) {
                    for (Job currJob : currPlace.getJobList()) {
                        if (currJob.getId().equals(jobId)) {
                            job = currJob;
                            initDatesSelection();
                            return;
                        }
                    }
                }
            }

            @Override
            public void onFailure() {

            }
        });
    }

    /**
     * Initialize start and end period selection
     */
    private void initDatesSelection() {
        selectedStartDate = Calendar.getInstance();
        selectedEndDate = Calendar.getInstance();
        selectedEndDate.add(Calendar.MONTH, 1);

        btnSelectStart.setJob(job, true);
        btnSelectEnd.setJob(job, true);

        btnSelectStart.setDate(Calendar.getInstance());
        btnSelectEnd.setDate(selectedEndDate);

        btnSelectStart.setListener(date -> {
            if (isAfterEndDate(date)) {
                showWarningStart();
                btnSelectStart.setDate(selectedStartDate);
            } else {
                selectedStartDate = btnSelectStart.getDate();
                reset();
            }
        });
        btnSelectEnd.setListener(date -> {
            if (isBeforeStartDate(date)) {
                showWarningEnd();
                btnSelectEnd.setDate(selectedEndDate);
            } else {
                reset();
                selectedEndDate = btnSelectEnd.getDate();
            }
        });
    }

    /**
     * Reset credits view
     */
    private void reset() {
        recyclerViewCredits.setVisibility(View.GONE);
        labelNoCreditsFound.setVisibility(View.GONE);
        if (serviceSelectionView.getSelectedServices().size() > 0) {

            if (hasEditRights) {
                btnSearchCredits.setVisibility(View.VISIBLE);
            }

            if (btnSelectEnd.getDate().compareTo(Calendar.getInstance()) > 0) {
                if (hasEditRights) {
                    btnAddCredits.setVisibility(View.VISIBLE);
                }
            } else {
                btnAddCredits.setVisibility(View.GONE);
            }
        } else {
            btnSearchCredits.setVisibility(View.GONE);
            btnAddCredits.setVisibility(View.GONE);
        }
    }

    /**
     * Verify validity of start date
     */
    private boolean isAfterEndDate(Calendar date) {
        return date.compareTo(selectedEndDate) >= 0;
    }

    /**
     * Verify validity of end date
     */
    private boolean isBeforeStartDate(Calendar date) {
        return date.compareTo(selectedStartDate) <= 0;
    }

    private void showWarningStart() {
        Snackbar.make(root, "La fecha de comienzo debe ser anterior al fin del período", Snackbar.LENGTH_SHORT).show();
    }

    private void showWarningEnd() {
        Snackbar.make(root, "La fecha de fin debe ser posterior al comienzo del período", Snackbar.LENGTH_SHORT).show();
    }

    /**
     * Display alert dialog to chose the amount of credits to add to the user
     */
    private void onBtnAddCreditsClick() {
        View view = getLayoutInflater().inflate(R.layout.dialog_add_place_credits, null, false);

        CustomAlertDialogBuilder builder = new CustomAlertDialogBuilder(getContext())
                .setTitle("Cargar créditos")
                .setView(view);

        LoadingButton btnConfirm = view.findViewById(R.id.btnConfirm);
        view.findViewById(R.id.btnDelete).setVisibility(View.GONE);
        AppCompatTextView labelServices = view.findViewById(R.id.labelServices);
        AppCompatTextView labelPeriod = view.findViewById(R.id.labelPeriod);
        AppCompatEditText etCreditsCount = view.findViewById(R.id.editTextCreditsCount);
        TextInputLayout textInputEditText = view.findViewById(R.id.textInputCreditsCount);

        labelServices.setText(serviceSelectionView.getServicesString());
        labelPeriod.setText(
                String.format(
                        "%s a %s",
                        CalendarUtils.formatDate(btnSelectStart.getDate()),
                        CalendarUtils.formatDate(btnSelectEnd.getDate())
                )
        );

        btnConfirm.setOnClickListener(view1 -> {
            if (TextUtils.isEmpty(etCreditsCount.getText().toString())) {
                btnConfirm.hideLoading();
                textInputEditText.setError("Seleccione la cantidad de créditos a añadir");
            } else {
                if(GetPremiumActivity.hasPremiumInPlaceOrShowScreen(requireActivity(), placeId, UserManagement.getInstance().getUser().getId())) {
                    int inputNumber = (Integer.parseInt(etCreditsCount.getText().toString()));
                    textInputEditText.setError(null);
                    saveCredits(inputNumber, builder, btnConfirm);
                }
                btnConfirm.hideLoading();
            }
        });

        builder.show();
    }

    /**
     * Post credits updates to database
     */
    private void saveCredits(int count, CustomAlertDialogBuilder builder, LoadingButton btnConfirm) {

        PlaceCreditsManager.addCreditsToUser(
                placeId,
                client.getId(),
                selectedStartDate,
                selectedEndDate,
                serviceSelectionView.getSelectedServices(),
                count,
                new PlaceCreditsManager.UpdateCreditsListener() {
                    @Override
                    public void onUpdate() {
                        builder.dismiss();
                        fetchCredits();
                    }

                    @Override
                    public void onFailure() {
                        showConnectionError();
                        btnConfirm.hideLoading();
                    }
                }
        );
    }

    private void showConnectionError() {
        Snackbar.make(root, getString(R.string.error_de_conexion), Snackbar.LENGTH_SHORT).show();
    }

    private void saveState(Bundle outState) {
        outState.putString(ARG_PARAM_PLACE_ID, placeId);

        if (client != null)
            outState.putParcelable(ARG_PARAM_CLIENT, client);

        if (job != null)
            outState.putString(ARG_PARAM_JOB_ID, job.getId());

        outState.putParcelableArrayList(ARG_PARAM_CLIENT, (ArrayList<ServiceInstance>) serviceInstancesWithCredits);
    }

    private void recoverState(Bundle bundle) {
        placeId = bundle.getString(ARG_PARAM_PLACE_ID);

        if (bundle.containsKey(ARG_PARAM_JOB_ID))
            jobId = bundle.getString(ARG_PARAM_JOB_ID);

        if (bundle.containsKey(ARG_PARAM_CLIENT))
            client = bundle.getParcelable(ARG_PARAM_CLIENT);

        if (bundle.containsKey(ARG_PARAM_EDIT_RIGHTS))
            hasEditRights = bundle.getBoolean(ARG_PARAM_EDIT_RIGHTS);

        serviceInstancesWithCredits = bundle.getParcelableArrayList(ARG_PARAM_SERVICE_INSTANCE_LIST);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        saveState(outState);
    }

    /**
     * Retrieve from database the credits of the user
     */
    private void fetchCredits() {
        List<ServiceInstance> selectedServices = serviceSelectionView.getSelectedServices();
        List<String> selectedServicesIds = new ArrayList<>();
        for (ServiceInstance si : selectedServices) {
            selectedServicesIds.add(si.getService().getId());
        }

        PlaceCreditsManager.getCreditsForJobClient(
                placeId,
                client.getId(),
                btnSelectStart.getDate(),
                btnSelectEnd.getDate(),
                selectedServicesIds,
                new PlaceCreditsManager.GetCreditsListener() {
                    @Override
                    public void onFetch(List<PlaceCreditsImpl> creditsList) {
                        onCreditsFetch(creditsList);
                    }

                    @Override
                    public void onFailure() {
                        Snackbar.make(root, getString(R.string.error_de_conexion), Snackbar.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void onCreditsFetch(List<PlaceCreditsImpl> creditsList) {
        btnSearchCredits.hideLoading();
        btnSearchCredits.setVisibility(View.GONE);

        if (creditsList.size() > 0) {
            labelNoCreditsFound.setVisibility(View.GONE);
            recyclerViewCredits.setVisibility(View.VISIBLE);

            List<ViewJobCredit> viewJobCreditList = new ArrayList<>();
            for (PlaceCredits placeCredits : creditsList) {
                viewJobCreditList.add(new ViewJobCredit(
                        placeCredits,
                        view -> onCreditsClick(placeCredits)
                ));
            }

            FlexibleAdapter<ViewJobCredit> adapter = new FlexibleAdapter<>(viewJobCreditList);
            recyclerViewCredits.setAdapter(adapter);

        } else {
            labelNoCreditsFound.setVisibility(View.VISIBLE);
            recyclerViewCredits.setVisibility(View.GONE);
        }
    }

    private void onCreditsClick(PlaceCredits placeCredits) {
        View view = getLayoutInflater().inflate(R.layout.dialog_add_place_credits, null, false);

        CustomAlertDialogBuilder builder = new CustomAlertDialogBuilder(getContext())
                .setView(view);

        LoadingButton btnConfirm = view.findViewById(R.id.btnConfirm);
        LoadingButton btnDelete = view.findViewById(R.id.btnDelete);
        view.findViewById(R.id.labelServices).setVisibility(View.GONE);
        AppCompatTextView labelPeriod = view.findViewById(R.id.labelPeriod);
        AppCompatEditText etCreditsCount = view.findViewById(R.id.editTextCreditsCount);
        TextInputLayout textInputEditText = view.findViewById(R.id.textInputCreditsCount);

        etCreditsCount.setText(String.valueOf(placeCredits.getCurrentCredits()));

        labelPeriod.setText(
                String.format(
                        "%s a %s",
                        CalendarUtils.formatDate(btnSelectStart.getDate()),
                        CalendarUtils.formatDate(btnSelectEnd.getDate())
                )
        );

        if (canEdit(placeCredits)) {
            btnConfirm.setOnClickListener(view1 -> {
                if (TextUtils.isEmpty(etCreditsCount.getText().toString())) {
                    btnConfirm.hideLoading();
                    textInputEditText.setError("Seleccione la cantidad de créditos a añadir");
                    return;
                }

                int inputNumber = (Integer.parseInt(etCreditsCount.getText().toString()));

                if (inputNumber > placeCredits.getCredits()) {
                    btnConfirm.hideLoading();
                    textInputEditText.setError(String.format("No puede ser mayor al máximo de créditos: %s", placeCredits.getCredits()));
                    return;
                }

                textInputEditText.setError(null);
                updateCredits(placeCredits, inputNumber, builder);
            });
            btnDelete.setOnClickListener(view1 -> deleteCredits(placeCredits, builder));
        } else {
            btnConfirm.setVisibility(View.GONE);
            btnDelete.setVisibility(View.GONE);
            etCreditsCount.setEnabled(false);
        }

        builder.show();
    }

    private boolean canEdit(PlaceCredits placeCredits) {
        return Calendar.getInstance().compareTo(placeCredits.getValidUntil()) < 0 && hasEditRights;
    }

    private void deleteCredits(PlaceCredits placeCredits, CustomAlertDialogBuilder builder) {
        PlaceCreditsManager.removeCreditFromUser(
                placeId,
                client.getId(),
                placeCredits.getId(),
                new PlaceCreditsManager.UpdateCreditsListener() {
                    @Override
                    public void onUpdate() {
                        builder.dismiss();
                        fetchCredits();
                    }

                    @Override
                    public void onFailure() {
                        showConnectionError();
                    }
                }
        );
    }

    private void updateCredits(PlaceCredits placeCredits, int inputNumber, CustomAlertDialogBuilder builder) {
        PlaceCreditsManager.updateCreditsToUser(
                placeId,
                client.getId(),
                placeCredits.getId(),
                null,
                null,
                inputNumber,
                new PlaceCreditsManager.UpdateCreditsListener() {
                    @Override
                    public void onUpdate() {
                        fetchCredits();
                        builder.dismiss();
                    }

                    @Override
                    public void onFailure() {
                        showConnectionError();
                    }
                }
        );
    }
}