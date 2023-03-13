package com.yatenesturno.view_builder;

import static android.app.Activity.RESULT_OK;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yatenesturno.Constants;
import com.yatenesturno.R;
import com.yatenesturno.activities.get_premium.GetPremiumActivity;
import com.yatenesturno.activities.main_screen.SendMessageActivity;
import com.yatenesturno.custom_views.LabelHandlerImpl;
import com.yatenesturno.custom_views.LoadingOverlay;
import com.yatenesturno.database.djangoImpl.DatabaseDjangoWrite;
import com.yatenesturno.functionality.LabelHandler;
import com.yatenesturno.functionality.LabelSelectorView;
import com.yatenesturno.listeners.DatabaseCallback;
import com.yatenesturno.object_interfaces.Appointment;
import com.yatenesturno.object_interfaces.CustomUser;
import com.yatenesturno.object_interfaces.Label;
import com.yatenesturno.objects.AppointmentClassImpl;
import com.yatenesturno.objects.AppointmentImpl;
import com.yatenesturno.user_auth.UserManagement;
import com.yatenesturno.utils.CustomAlertDialogBuilder;
import com.yatenesturno.utils.TimeZoneManager;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;

public class InnerViewAppointmentClass extends Fragment {

    public static final String APPOINTMENT = "appointment";
    public static final String PLACE_ID = "placeId";

    /**
     * Instance variables
     */
    private final String jobId;
    private String placeId;
    private List<AppointmentImpl> innerAppList;

    /**
     * UI References
     */
    private AdapterClassClients adapter;
    private LabelSelectorView labelSelectorView;
    private AppointmentClassImpl appointmentClass;
    private LoadingOverlay loadingOverlay;

    public InnerViewAppointmentClass(AppointmentClassImpl appointmentClass, String placeId, String jobId) {
        this.appointmentClass = appointmentClass;
        this.placeId = placeId;
        this.jobId = jobId;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putAll(saveState());
    }

    private Bundle saveState() {
        Bundle bundle = new Bundle();

        bundle.putParcelable(APPOINTMENT, appointmentClass);
        bundle.putString(PLACE_ID, placeId);

        return bundle;
    }

    private void recoverState(Bundle bundle) {
        appointmentClass = bundle.getParcelable(APPOINTMENT);
        placeId = bundle.getString(PLACE_ID);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.inner_appointment_class_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState != null) {
            recoverState(savedInstanceState);
        }

        initView();
    }

    private void initView() {
        RecyclerView recyclerViewClients = getView().findViewById(R.id.recyclerViewClients);
        loadingOverlay = new LoadingOverlay(getView().findViewById(R.id.root));

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerViewClients.setLayoutManager(layoutManager);

        this.innerAppList = appointmentClass.getCompositionApps();
        this.adapter = new AdapterClassClients(innerAppList);
        recyclerViewClients.setAdapter(adapter);

        this.labelSelectorView = new LabelSelectorView(placeId, jobId, appointmentClass);

        setUpBtnCancel();
    }

    private void setUpBtnCancel() {
        Button btnCancel = getView().findViewById(R.id.btnCancel);
        if (!isPastAppointment()) {
            btnCancel.setOnClickListener(new OnBtnCancelClickListener());
        } else {
            btnCancel.setVisibility(View.GONE);
        }
    }

    private boolean isPastAppointment() {
        Calendar todayCalendar = Calendar.getInstance();
        return todayCalendar.compareTo(appointmentClass.getTimeStampStart()) >= 0;
    }

    private void displayConfirmationCancelClass(Context context) {
        String title = getString(R.string.confirm_cancel_app);

        new CustomAlertDialogBuilder(context)
                .setTitle(title)
                .setPositiveButton(R.string.accept, (dialog, which) -> {
                    onConfirmCancelAppointment();
                    dialog.dismiss();
                })
                .show();
    }

    private void displayConfirmationCancelClient(Context context, final CustomUser client) {
        String title = "¿Confirma la cancelación del turno de " + client.getGivenName() + "?";

        new CustomAlertDialogBuilder(context)
                .setTitle(title)
                .setPositiveButton(R.string.accept, (dialog, which) -> {
                    onConfirmCancelClient(client);
                    dialog.dismiss();
                })
                .show();
    }

    private void onConfirmCancelClient(final CustomUser client) {
        showLoadingOverlay();
        Map<String, String> body = getCancelClientFromClassBody(client.getId());

        DatabaseDjangoWrite.getInstance().POST(
                Constants.DJANGO_URL_DROP_CLIENT_CLASS_APPOINTMENT,
                body,
                new DatabaseCallback() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        onDropClientAppointment(client);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        hideLoadingOverlay();
                    }
                }
        );
    }

    private Map<String, String> getCancelClientFromClassBody(String client_id) {
        Map<String, String> body = new HashMap<>();
        body.put("service_instance", appointmentClass.getServiceInstance().getId());
        body.put("timestamp", formatCalendarAsTimeStamp(
                TimeZoneManager.toUTC(
                        appointmentClass.getTimeStampStart())));
        body.put("client_id", client_id);
        return body;
    }

    private void onConfirmCancelAppointment() {
        if (isPastAppointment()) {
            return;
        }
        showLoadingOverlay();
        Map<String, String> body = getCancelClassBody();

        DatabaseDjangoWrite.getInstance().POST(
                Constants.DJANGO_URL_DROP_CLASS_APPOINTMENT,
                body,
                new DatabaseCallback() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        onDropAppointment();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        hideLoadingOverlay();
                    }
                }
        );
    }

    public void onDropAppointment() {
        hideLoadingOverlay();
        getActivity().setResult(RESULT_OK);
        getActivity().finish();
    }

    public void onDropClientAppointment(CustomUser client) {
        hideLoadingOverlay();
        getActivity().setResult(RESULT_OK);

        AppointmentImpl toRemove = null;
        for (AppointmentImpl appimpl : innerAppList) {
            if (appimpl.getClient().equals(client)) {
                toRemove = appimpl;
                break;
            }
        }

        if (toRemove != null) {
            innerAppList.remove(toRemove);
        }
        adapter.notifyDataSetChanged();
    }

    private Map<String, String> getCancelClassBody() {
        Map<String, String> body = new HashMap<>();
        body.put("service_instance", appointmentClass.getServiceInstance().getId());
        body.put("timestamp", formatCalendarAsTimeStamp(
                TimeZoneManager.toUTC(
                        appointmentClass.getTimeStampStart())));
        return body;
    }

    private String formatCalendarAsTimeStamp(Calendar calendar) {
        return calendar.get(Calendar.YEAR) + "-" +
                (calendar.get(Calendar.MONTH) + 1) + "-" +
                calendar.get(Calendar.DAY_OF_MONTH) + " " +
                calendar.get(Calendar.HOUR_OF_DAY) + ":" +
                calendar.get(Calendar.MINUTE) + ":" +
                calendar.get(Calendar.SECOND);

    }

    private void showLoadingOverlay() {
        if (loadingOverlay != null) {
            loadingOverlay.show();
        }
    }

    public void hideLoadingOverlay() {
        if (loadingOverlay != null) {
            loadingOverlay.hide();
        }
    }

    public boolean canSendMessages() {
        return GetPremiumActivity.hasPremiumInPlaceOrShowScreen(getActivity(), placeId, UserManagement.getInstance().getUser().getId());
    }

    private void deleteAppLabel(AppointmentImpl app) {
        LabelHandlerImpl.getInstance().clearLabel(placeId, jobId, app.getId(), new LabelHandler.LabelHandlerListener() {
            @Override
            public void onSuccess() {
                getActivity().setResult(RESULT_OK);
            }

            @Override
            public void onFailure() {

            }
        });
    }

    private void postAppLabelToRemote(Appointment appointment, Label label) {
        if (label.isNotAttendedLabel()) {
            postAttended(appointment, false);
        } else {
            postLabelToApp(appointment, label);
        }
    }

    private void postAttended(Appointment appointment, boolean attended) {
        getActivity().setResult(RESULT_OK);
        LabelHandlerImpl.getInstance().setAttended(
                placeId,
                jobId,
                attended,
                appointment.getId(),
                new LabelHandler.LabelHandlerListener() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onFailure() {
                        Toast.makeText(getContext(), getString(R.string.error_attended_change), Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void postLabelToApp(Appointment appointment, Label label) {
        getActivity().setResult(RESULT_OK);
        LabelHandlerImpl.getInstance().setLabel(
                placeId,
                jobId,
                appointment.getId(),
                label.getId(),
                new LabelHandler.LabelHandlerListener() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onFailure() {
                        Toast.makeText(getContext(), getString(R.string.error_attended_change), Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private class AdapterClassClients extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private final int TYPE_FREE_SLOT = 0, TYPE_CLIENT_SLOT = 1;
        List<AppointmentImpl> appsList;

        public AdapterClassClients(List<AppointmentImpl> clientList) {
            this.appsList = clientList;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v;
            switch (viewType) {

                case TYPE_CLIENT_SLOT:
                    v = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.appointment_class_client_slot, parent, false);

                    return new ViewHolderClient(v);

                default:
                case TYPE_FREE_SLOT:
                    v = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.appointment_class_free_slot, parent, false);

                    return new ViewHolderFreeSlot(v);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            switch (holder.getItemViewType()) {
                case TYPE_CLIENT_SLOT:
                    bindClientSlot((ViewHolderClient) holder, position);
                    break;

                default:
                case TYPE_FREE_SLOT:
                    bindFreeSlot((ViewHolderFreeSlot) holder);
                    break;
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position >= appsList.size()) {
                return TYPE_FREE_SLOT;
            }
            return TYPE_CLIENT_SLOT;
        }

        private void bindFreeSlot(ViewHolderFreeSlot holder) {
            if (isPastAppointment()) {
                holder.tvFreeSlot.setText(R.string.slot_not_filled);
            } else {
                holder.tvFreeSlot.setText(R.string.free_slot);
            }
        }

        private void bindClientSlot(ViewHolderClient holder, final int position) {
            AppointmentImpl currApp = appsList.get(position);
            holder.labelClientName.setText(currApp.getClient().getName());

            if (currApp.usesCredits() && currApp.bookedWithoutCredits()) {
                holder.labelWithoutCredits.setVisibility(View.VISIBLE);
                holder.ivCredits.setVisibility(View.VISIBLE);
            } else {
                holder.labelWithoutCredits.setVisibility(View.GONE);
                holder.ivCredits.setVisibility(View.GONE);
            }

            holder.btnSendMsg.setOnClickListener(v -> {
                if (canSendMessages()) {
                    Bundle bundle = new Bundle();

                    bundle.putSerializable("client", currApp.getClient());
                    bundle.putString("placeId", placeId);

                    Intent intent = new Intent(getContext(), SendMessageActivity.class);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            });

            updateLabel(holder, currApp);
            holder.cardViewLabel.setOnClickListener(view -> {
                labelSelectorView.setOnLabelSelectedListener(new LabelSelectorView.OnLabelClickListener() {
                    @Override
                    public void onLabelSelected(Label label) {
                        AppointmentImpl app = currApp;
                        if (label != null) {
                            postAppLabelToRemote(app, label);
                        } else if (app.getLabel() != null && !app.getLabel().isNotAttendedLabel()) {
                            deleteAppLabel(app);
                        } else {
                            app.setLabel(null);
                            app.setDidAttend(true);
                            postAttended(app, true);
                        }
                        app.setLabel(label);
                        updateLabel(holder, app);
                    }

                    @Override
                    public void onDelete(Label label) {
                        Appointment app = appsList.get(position);
                        if (app.getLabel() != null) {
                            if (label.equals(app.getLabel())) {
                                app.setLabel(null);
                                app.setDidAttend(true);
                                updateLabel(holder, app);
                            }
                        }
                        getActivity().setResult(RESULT_OK);
                    }
                });
                labelSelectorView.showChangeLabelDialog(getContext());
            });

            if (!isPastAppointment()) {
                holder.root.setOnLongClickListener(v -> {
                    displayConfirmationCancelClient(getContext(), innerAppList.get(position).getClient());
                    return false;
                });
            }
        }

        private void updateLabel(ViewHolderClient holder, Appointment app) {
            LabelSelectorView.fillView(holder.cardViewLabel, app);
        }

        @Override
        public int getItemCount() {
            return Math.max(appointmentClass.getServiceInstance().getConcurrency(), appsList.size());
        }

        public class ViewHolderClient extends RecyclerView.ViewHolder {
            public TextView labelClientName;
            public CardView root;
            public AppCompatImageButton btnSendMsg;
            public CardView cardViewLabel;
            public AppCompatImageView ivCredits;
            public TextView labelWithoutCredits;

            public ViewHolderClient(View view) {
                super(view);

                labelClientName = view.findViewById(R.id.labelClientName);
                root = view.findViewById(R.id.root);
                btnSendMsg = view.findViewById(R.id.btnSendMsg);
                cardViewLabel = view.findViewById(R.id.cardViewLabel);
                labelWithoutCredits = view.findViewById(R.id.labelWithoutCredits);
                ivCredits = view.findViewById(R.id.ivCredits);
            }
        }

        public class ViewHolderFreeSlot extends RecyclerView.ViewHolder {

            public TextView tvFreeSlot;

            public ViewHolderFreeSlot(View view) {
                super(view);

                tvFreeSlot = view.findViewById(R.id.tvFreeSlot);
            }
        }
    }

    private class OnBtnCancelClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            displayConfirmationCancelClass(v.getContext());
        }
    }
}
