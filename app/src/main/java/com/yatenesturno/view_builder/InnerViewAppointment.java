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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yatenesturno.Constants;
import com.yatenesturno.R;
import com.yatenesturno.activities.get_premium.GetPremiumActivity;
import com.yatenesturno.activities.main_screen.SendMessageActivity;
import com.yatenesturno.custom_views.LabelHandlerImpl;
import com.yatenesturno.custom_views.LoadingOverlay;
import com.yatenesturno.database.DatabaseDjangoWrite;
import com.yatenesturno.functionality.LabelHandler;
import com.yatenesturno.functionality.LabelSelectorView;
import com.yatenesturno.functionality.app_observation.AppObservationController;
import com.yatenesturno.listeners.DatabaseCallback;
import com.yatenesturno.object_interfaces.AppointmentService;
import com.yatenesturno.object_interfaces.Label;
import com.yatenesturno.object_interfaces.ServiceInstance;
import com.yatenesturno.objects.AppointmentImpl;
import com.yatenesturno.user_auth.UserManagement;
import com.yatenesturno.utils.CustomAlertDialogBuilder;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;

public class InnerViewAppointment extends Fragment {

    public static final String APPOINTMENT = "appointment";
    public static final String PLACE_ID = "placeId";
    private AppointmentImpl appointment;
    private String placeId;
    private LoadingOverlay loadingOverlay;
    private String jobId;

    public InnerViewAppointment(AppointmentImpl appointment, String placeId, String jobId) {
        this.appointment = appointment;
        this.placeId = placeId;
        this.jobId = jobId;
    }

    public InnerViewAppointment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(getLayoutResId(), container, false);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putAll(saveState());
    }

    private Bundle saveState() {
        Bundle bundle = new Bundle();

        bundle.putParcelable(APPOINTMENT, appointment);
        bundle.putString(PLACE_ID, placeId);

        return bundle;
    }

    private void recoverState(Bundle bundle) {
        appointment = bundle.getParcelable(APPOINTMENT);
        placeId = bundle.getString(PLACE_ID);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState != null) {
            recoverState(savedInstanceState);
        }

        initView();
    }

    public void initView() {
        RecyclerView recyclerViewServiceInstance = getView().findViewById(R.id.recyclerViewServiceInstance);
        loadingOverlay = new LoadingOverlay(getView().findViewById(R.id.root));

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerViewServiceInstance.setLayoutManager(layoutManager);

        recyclerViewServiceInstance.setAdapter(new AdapterServiceInstance(appointment.getAppointmentServices()));

        new AppObservationController(
                getView().findViewById(R.id.appObservation),
                jobId,
                appointment,
                () -> getActivity().setResult(RESULT_OK));

        setUpBtnCancel();
        setUpLabel();
        setUpBtnSendMessage();
    }

    private int getLayoutResId() {
        return R.layout.inner_appointment_impl_view;
    }

    private void setUpBtnCancel() {
        Button btnCancel = getView().findViewById(R.id.btnCancel);
        if (!isPastAppointment()) {
            btnCancel.setOnClickListener(new OnBtnCancelClickListener());
        } else {
            btnCancel.setVisibility(View.GONE);
        }
    }

    private void setUpLabel() {
        View labelView = LabelSelectorView.buildView(getContext(), appointment);
        ViewGroup holderLabel = getView().findViewById(R.id.containerLabel);
        holderLabel.removeAllViews();
        holderLabel.addView(labelView);

        labelView.setOnClickListener(v -> showChangeLabelDialog());
    }

    private void showChangeLabelDialog() {
        LabelSelectorView labelSelectorView = new LabelSelectorView(placeId, jobId, appointment);
        labelSelectorView.setOnLabelSelectedListener(new LabelSelectorView.OnLabelClickListener() {
            @Override
            public void onLabelSelected(Label label) {
                if (label != null) {
                    postAppLabelToRemote(label);
                } else if (appointment.getLabel() != null && !appointment.getLabel().isNotAttendedLabel()) {
                    deleteAppLabel();
                } else {
                    appointment.setLabel(null);
                    appointment.setDidAttend(true);
                    postAttended(true);
                }

                appointment.setLabel(label);
                setUpLabel();
            }

            @Override
            public void onDelete(Label label) {
                if (appointment.getLabel() != null) {
                    if (label.equals(appointment.getLabel())) {
                        appointment.setLabel(null);
                        appointment.setDidAttend(true);
                        setUpLabel();
                    }
                }
                getActivity().setResult(RESULT_OK);
            }
        });
        labelSelectorView.showChangeLabelDialog(getContext());
    }

    private void deleteAppLabel() {
        getActivity().setResult(RESULT_OK);
        LabelHandlerImpl.getInstance().clearLabel(placeId, jobId, appointment.getId(), new LabelHandler.LabelHandlerListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure() {

            }
        });
    }

    private void postAppLabelToRemote(Label label) {
        if (label.isNotAttendedLabel()) {
            postAttended(false);
        } else {
            postLabelToApp(label);
        }
    }

    private void postAttended(boolean attended) {
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

    private void postLabelToApp(Label label) {
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

    public boolean isPastAppointment() {
        Calendar todayCalendar = Calendar.getInstance();
        return todayCalendar.compareTo(appointment.getTimeStampStart()) >= 0;
    }

    public boolean canSendMessages() {
        return GetPremiumActivity.hasPremiumInPlaceOrShowScreen(getActivity(), placeId, UserManagement.getInstance().getUser().getId());
    }

    private void setUpBtnSendMessage() {
        Button btnSendMessage = getView().findViewById(R.id.btnSendMessage);

        btnSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (canSendMessages()) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("client", appointment.getClient());
                    bundle.putString("placeId", placeId);

                    Intent intent = new Intent(getContext(), SendMessageActivity.class);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            }
        });
    }

    private void addTimes(Calendar c1, Calendar c2) {
        c1.add(Calendar.HOUR_OF_DAY, c2.get(Calendar.HOUR_OF_DAY));
        c1.add(Calendar.MINUTE, c2.get(Calendar.MINUTE));
    }

    private Calendar getCalendarStartForService(AppointmentService appointmentService) {
        Calendar serviceInstanceStart = (Calendar) appointment.getTimeStampStart().clone();
        List<AppointmentService> aServiceList = appointment.getAppointmentServices();

        int it = 0;
        AppointmentService current = aServiceList.get(it);
        while (current != appointmentService) {
            it++;
            addTimes(serviceInstanceStart, current.getServiceInstance().getDuration());
            current = aServiceList.get(it);
        }

        return serviceInstanceStart;
    }

    private void displayCancelConfirmDialog(Context context) {
        String title = getString(R.string.confirm_cancel_app);

        new CustomAlertDialogBuilder(context)
                .setTitle(title)
                .setPositiveButton(R.string.accept, (dialog, which) -> {
                    onCancelAppointment();
                    dialog.dismiss();
                })
                .show();
    }

    private void onCancelAppointment() {
        showLoadingOverlay();
        Map<String, String> body = getBody();

        DatabaseDjangoWrite.getInstance().POST(
                Constants.DJANGO_URL_DROP_APPOINTMENT,
                body,
                new DatabaseCallback() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        onDropAppointment();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        hideLoadingOverlay();
                        showConnectionError();
                    }
                }
        );
    }

    public void showConnectionError() {
        Toast.makeText(getContext(), getString(R.string.error_de_conexion), Toast.LENGTH_SHORT).show();
    }

    public void onDropAppointment() {
        hideLoadingOverlay();
        getActivity().setResult(RESULT_OK);
        getActivity().finish();
    }

    private Map<String, String> getBody() {
        Map<String, String> body = new HashMap<>();
        body.put("appointment_id", appointment.getId());
        return body;
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

    private String formatCalendar(Calendar calendar) {
        return String.format("%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
    }

    private class AdapterServiceInstance extends RecyclerView.Adapter<AdapterServiceInstance.ViewHolderAppointmentService> {
        List<AppointmentService> serviceInstanceList;

        public AdapterServiceInstance(List<AppointmentService> serviceInstanceList) {
            this.serviceInstanceList = serviceInstanceList;
        }

        @NonNull
        @Override
        public ViewHolderAppointmentService onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.appointment_view_service_layout, parent, false);

            return new ViewHolderAppointmentService(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolderAppointmentService holder, int position) {
            AppointmentService aService = serviceInstanceList.get(position);

            ServiceInstance si = aService.getServiceInstance();

            String strStart = formatCalendar(getCalendarStartForService(aService));
            String strDuration = "";
            if (si.getDuration().get(Calendar.HOUR_OF_DAY) > 0) {
                strDuration += String.format("%dh %02dm", si.getDuration().get(Calendar.HOUR_OF_DAY), si.getDuration().get(Calendar.MINUTE));
            } else {
                strDuration += String.format("%02dm", si.getDuration().get(Calendar.MINUTE));
            }

            holder.labelServiceInstanceStart.setText(strStart);
            holder.labelServiceInstanceName.setText(si.getService().getName());
            holder.labelServiceInstanceCost.setText("$" + si.getPrice());
            holder.labelServiceInstanceDuration.setText(strDuration);
        }

        @Override
        public int getItemCount() {
            return serviceInstanceList.size();
        }

        public class ViewHolderAppointmentService extends RecyclerView.ViewHolder {
            public TextView labelServiceInstanceStart;
            public TextView labelServiceInstanceName;
            public TextView labelServiceInstanceCost;
            public TextView labelServiceInstanceDuration;

            public ViewHolderAppointmentService(View view) {
                super(view);

                labelServiceInstanceStart = view.findViewById(R.id.labelServiceInstanceStart);
                labelServiceInstanceName = view.findViewById(R.id.labelServiceInstanceName);
                labelServiceInstanceCost = view.findViewById(R.id.labelServiceInstanceCost);
                labelServiceInstanceDuration = view.findViewById(R.id.labelServiceInstanceDuration);
            }
        }
    }

    private class OnBtnCancelClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            displayCancelConfirmDialog(v.getContext());
        }
    }
}
