package com.yatenesturno.activities.main_screen;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static android.content.Intent.getIntent;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputLayout;
import com.yatenesturno.Constants;
import com.yatenesturno.R;
import com.yatenesturno.activities.get_premium.GetPremiumActivity;
import com.yatenesturno.activities.job_appointment_book.AnonymousAppActivity;
import com.yatenesturno.activities.services.CreateServiceActivity;
import com.yatenesturno.custom_views.LoadingButton;
import com.yatenesturno.custom_views.LoadingOverlay;
import com.yatenesturno.database.djangoImpl.DatabaseDjangoRead;
import com.yatenesturno.functionality.DayScheduleManager;
import com.yatenesturno.listeners.DatabaseCallback;
import com.yatenesturno.notification.EmailNotification;
import com.yatenesturno.object_interfaces.CustomUser;
import com.yatenesturno.object_interfaces.Job;
import com.yatenesturno.object_interfaces.Place;
import com.yatenesturno.object_interfaces.ServiceInstance;
import com.yatenesturno.serializers.BuilderListCustomUser;
import com.yatenesturno.serializers.BuilderListServiceInstance;
import com.yatenesturno.user_auth.UserManagement;
import com.yatenesturno.utils.CustomAlertDialogBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import cz.msebera.android.httpclient.Header;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flexibleadapter.items.IFilterable;
import eu.davidea.flexibleadapter.items.IFlexible;
import eu.davidea.viewholders.FlexibleViewHolder;

public class FragmentAdminClients extends Fragment {

    private Job job;
    private Place place;
    private List<CustomUser> clientList;
    private boolean hasRefreshed;
    private boolean populated;
    public static final String TAG = "fragmentclients";

    /**
     * UI References
     */
    private Button btnNewPromo, btnNewAnonymousApp;
    private LoadingOverlay loadingOverlay;
    private RecyclerView recyclerViewClients;
    private SearchView searchView;
    private CardView holderSearchViewClients;
    private FlexibleAdapter<FlexibleClientItem> adapter;

    public FragmentAdminClients() {

    }

    private void onClientClick(CustomUser user) {
        checkPremiumForChat(user);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (hasRefreshed) {
            hasRefreshed = false;
            populateClients();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    public void setState(Bundle bundle) {
        job = bundle.getParcelable("selectedJob");
        place = bundle.getParcelable("selectedPlace");
        clientList = bundle.getParcelableArrayList("clientList");
    }

    public void recoverState() {

        if (job != null && place != null && clientList != null) {
            populateClients();
            hasServices();
//            setupNewAppointmentBtn();
        }

        initViews();
    }

    private void initViews() {
        loadingOverlay = new LoadingOverlay(getView().findViewById(R.id.root));
        recyclerViewClients = getView().findViewById(R.id.recyclerViewClients);
        btnNewPromo = getView().findViewById(R.id.btnNewPromo);
        btnNewAnonymousApp = getView().findViewById(R.id.btnNewAnonymousAppointment);
        searchView = getView().findViewById(R.id.searchViewClients);
        holderSearchViewClients = getView().findViewById(R.id.holderSearchViewClients);
    }

    public List<CustomUser> getClientList() {
        return clientList;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews();

        if (savedInstanceState != null) {
            recoverState();
        }

        if (place != null && !populated) {
            populated = true;
            showLoadingOverlay();
            getClients();
            hasServices();
        }
    }

    public void showLoadingOverlay() {
        if (loadingOverlay != null) {
            loadingOverlay.show();
        }
    }

    private void populateClients() {
        if (clientList.size() > 0) {
            displayHasClients();
        } else {
            displayNoClients();

        }
    }

    private void displayHasClients() {
        getView().findViewById(R.id.labelNoClients).setVisibility(View.GONE);
        recyclerViewClients.setVisibility(View.VISIBLE);
        recyclerViewClients.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));

        List<FlexibleClientItem> clientItemList = new ArrayList<>();

        if (canSendMessages()) {
            for (CustomUser customUser : clientList) {
                clientItemList.add(new FlexibleClientItem(customUser, view -> {
                    onClientClick(customUser);
                }));
            }

        } else {
            for (CustomUser customUser : clientList) {
                clientItemList.add(new FlexibleClientItem(customUser, null));
            }
        }

        adapter = new FlexibleAdapter<>(clientItemList);
        recyclerViewClients.setAdapter(adapter);
        btnNewPromo.setVisibility(View.VISIBLE);

        setNewPromoListener();
        setSearchView();
    }

    private void setSearchView() {
        if (clientList.size() > 5) {
            holderSearchViewClients.setVisibility(View.VISIBLE);

        } else {
            holderSearchViewClients.setVisibility(View.GONE);
        }

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return onQueryTextChange(query);
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                if (adapter.hasNewFilter(newText)) {
                    adapter.setFilter(newText);
                    adapter.filterItems(100);
                }
                return true;
            }
        });
    }

    private void setNewPromoListener() {
        btnNewPromo.setOnClickListener(v -> {
            displayNewPromoDialog();
        });
    }

    List<ServiceInstance> providedServices;

    void hasServices() {
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
                            hideLoadingOverlay();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            providedServices = new ArrayList<>();
                            hideLoadingOverlay();
                        }

                        if (providedServices.isEmpty()) {
                            //hasn't services
                            setNoServicesBtn();
                        } else {
                            setServicesFloatingBtn();
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

    private boolean canEditJob() {
        boolean isPlaceOwner = place.getOwner().getId().equals(UserManagement.getInstance().getUser().getId());
        return job.canEdit() || isPlaceOwner;
    }

    private void setNoServicesBtn() {
        btnNewAnonymousApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (canEditJob()) createServiceBtn(view);
                else
                    Toast.makeText(requireContext(), "No tienes acceso a esta sección, pídele al dueño de la tienda que te de acceso o que configure los servicios", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void setServicesFloatingBtn() {
        Log.d("fragmentEvents", "setServicesFloatingBtn");
        btnNewAnonymousApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (canEditJob()) navigateToAppointments();
                else
                    Toast.makeText(requireContext(), "No tienes acceso a esta sección, pídele al dueño de la tienda que te de acceso o que configure los servicios", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void navigateToAppointments() {
        Intent intent = new Intent(getContext(), AnonymousAppActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable("job", job);
        bundle.putParcelableArrayList("clientList", (ArrayList<CustomUser>) clientList);
        bundle.putString("placeId", place.getId());
        intent.putExtras(bundle);
        requireActivity().startActivityForResult(intent, Constants.RC_NEW_ANONYMOUS_APP);
    }


    private void createServiceBtn(View view) {
        CardView popUpBtnCreateService;
        CardView popUpBtnDecline;

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
                if (canEditJob()) navigateToNewService();
                else
                    Toast.makeText(requireContext(), "No tienes acceso a esta sección, pídele al dueño de la tienda que te de acceso o que configure los servicios", Toast.LENGTH_LONG).show();
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


    private void navigateToNewService() {
        fetchDaySchedules();

    }

    private void fetchDaySchedules() {
        showLoadingOverlay();
        new Handler(Objects.requireNonNull(Looper.myLooper())).postDelayed(() -> DayScheduleManager.getInstance().getDaySchedules(job.getId(), dayScheduleList -> {
            hideLoadingOverlay();
            job.setDaySchedules(dayScheduleList);
            loadingOverlay.hide();
            Intent i = new Intent(requireActivity(), CreateServiceActivity.class);
            i.putExtra("job", (Serializable) job);
            i.putExtra("place", (Serializable) place);
            requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
            startActivity(i);
        }), 300);
    }

    private void sendNewPromo(String description) {
        StringBuilder toEmails = new StringBuilder();
        for (CustomUser c : clientList) {
            toEmails.append(c.getEmail()).append(",");
        }

        Bundle bundle = new Bundle();
        bundle.putString("contactInfo", toEmails.toString());
        bundle.putString("message", description);
        bundle.putString("subject", getPromoTitle());

        EmailNotification emailNotification = new EmailNotification();
        emailNotification.sendMessage(getContext(), bundle);
    }

    private String getPromoTitle() {
        return "Nueva PROMO en " + place.getBusinessName() + "!";
    }

    public void checkPremiumForChat(CustomUser user) {
        if (GetPremiumActivity.hasPremiumInPlaceOrShowScreen(getActivity(), place.getId(), UserManagement.getInstance().getUser().getId())) {
            startChatActivity(user);
        }
    }

    private boolean canSendMessages() {
        boolean isPlaceOwner = place.getOwner().getId().equals(UserManagement.getInstance().getUser().getId());
        return isPlaceOwner || job.canChat();
    }

    private void displayNoClients() {
        getView().findViewById(R.id.labelNoClients).setVisibility(View.VISIBLE);
        recyclerViewClients.setVisibility(View.GONE);
        btnNewPromo.setVisibility(View.GONE);
    }

    private void startChatActivity(CustomUser user) {
        Intent intent = new Intent(getActivity(), SendMessageActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("client", user);
        bundle.putString("jobId", job.getId());
        bundle.putString("placeId", place.getId());
        intent.putExtras(bundle);
        startActivity(intent);
    }

    public void setJob(Place place, Job selectedJob) {
        job = selectedJob;
        this.place = place;

        if (loadingOverlay == null && getView() != null) {
            initViews();
        }

        populated = false;
        if (getView() != null) {
            populated = true;
            showLoadingOverlay();
            getClients();
            hasServices();
        }
        Log.d(TAG, selectedJob.getEmployee().getName());
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

    public void hideLoadingOverlay() {
        if (loadingOverlay != null) {
            loadingOverlay.hide();
        }
    }

    public void showConnectionError() {
        Toast.makeText(getActivity(), getString(R.string.error_de_conexion), Toast.LENGTH_SHORT).show();
    }

    public void onClientsFetch(JSONObject response) {
        hideLoadingOverlay();
        handleResponse(response);
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

        if (getView() != null) {
            populateClients();
        } else {
            hasRefreshed = true;
        }
    }

    public static class FlexibleClientItem extends AbstractFlexibleItem<FlexibleClientItem.ViewHolderClientItem> implements IFilterable<String> {

        private final CustomUser customUser;
        private final View.OnClickListener listener;

        public FlexibleClientItem(CustomUser customUser, View.OnClickListener listener) {
            this.customUser = customUser;
            this.listener = listener;
        }

        @Override
        public boolean equals(Object o) {
            return false;
        }

        @Override
        public int getLayoutRes() {
            return R.layout.client_layout;
        }

        @Override
        public ViewHolderClientItem createViewHolder(View view, FlexibleAdapter<IFlexible> adapter) {
            return new ViewHolderClientItem(view, adapter);
        }

        @Override
        public void bindViewHolder(FlexibleAdapter<IFlexible> adapter, ViewHolderClientItem holder, int position, List<Object> payloads) {
            holder.labelClient.setText(customUser.getName());

            if (listener != null) {
                holder.root.setOnClickListener(listener);
            }

        }

        @Override
        public boolean filter(String constraint) {
            return customUser.getName().toLowerCase().contains(constraint.toLowerCase());
        }

        public class ViewHolderClientItem extends FlexibleViewHolder {

            TextView labelClient;
            ViewGroup root;

            public ViewHolderClientItem(View view, FlexibleAdapter adapter) {
                super(view, adapter);
                this.root = view.findViewById(R.id.root);
                this.labelClient = view.findViewById(R.id.labelClient);
            }
        }
    }


    /**
     * Metod who inflates a dialog, and before to confirm promotion, it validates premium or not premium user.
     * if the user is not premium, it shows you getPremimum screen
     */
    public void displayNewPromoDialog() {
        final View view = getLayoutInflater().inflate(R.layout.new_promo_dialog, null);
        LoadingButton btnConfirm = view.findViewById(R.id.btnConfirm);
        TextInputLayout textInputLayout = view.findViewById(R.id.textInputExtraInfo);


        CustomAlertDialogBuilder builder =
                new CustomAlertDialogBuilder(getContext())
                        .setTitle(getString(R.string.write_promo_description))
                        .setView(view);

        btnConfirm.setOnClickListener(view1 -> {
            //Condition to validate if the user has any place yet, if the user has already make it,
            // validate if it a premium user or not, if the user don't has it, it shows the getPremium screen to the user.
            if (GetPremiumActivity.hasPremiumInPlaceOrShowScreen(getActivity(), place.getId(), UserManagement.getInstance().getUser().getId())) {

                String description = ((EditText) view.findViewById(R.id.etNewPromo)).getText().toString();
                if (!TextUtils.isEmpty(description)) {
                    textInputLayout.setError(null);
                    sendNewPromo(description);
                    builder.dismiss();
                } else {
                    textInputLayout.setError("Por favor ingrese un mensaje");
                }
                btnConfirm.hideLoading();
            }
        });

        builder.show();

    }


}