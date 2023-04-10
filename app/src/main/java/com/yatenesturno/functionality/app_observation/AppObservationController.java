package com.yatenesturno.functionality.app_observation;

import android.app.Activity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.crashlytics.internal.model.CrashlyticsReport;
import com.yatenesturno.R;
import com.yatenesturno.activities.get_premium.GetPremiumActivity;
import com.yatenesturno.activities.job_request.NewJobRequestActivity;
import com.yatenesturno.custom_views.LoadingButton;
import com.yatenesturno.functionality.ManagerPlace;
import com.yatenesturno.functionality.PlacePremiumManager;
import com.yatenesturno.object_interfaces.Appointment;
import com.yatenesturno.object_interfaces.Place;
import com.yatenesturno.user_auth.UserManagement;
import com.yatenesturno.utils.CustomAlertDialogBuilder;
import com.yatenesturno.view_builder.InnerViewAppointment;
import com.yatenesturno.view_builder.InnerViewAppointmentClass;

import java.util.List;

public class AppObservationController {

    private final Appointment appointment;
    private final String jobId;
    private final OnChangeListener listener;
    private final String placeId;
    private final Activity activity;
    private ViewGroup root;
    private AppCompatTextView labelObservation;

    public AppObservationController(ViewGroup holderView, String jobId, String placeId, Appointment appointment, Activity activity,  OnChangeListener listener) {
        this.jobId = jobId;
        this.appointment = appointment;
        this.listener = listener;
        this.placeId = placeId;
        this.activity = activity;

        inflateView(holderView);
        updateView();
    }

    private void inflateView(ViewGroup holderView) {
        LayoutInflater inflater = LayoutInflater.from(holderView.getContext());
        root = (ViewGroup) inflater.inflate(R.layout.appointment_observation_layout, holderView, false);
        holderView.addView(root);

        labelObservation = root.findViewById(R.id.labelObservation);

        root.findViewById(R.id.btnAddObservation).setOnClickListener(view -> showDialog());
    }

    private void updateView() {
        if (appointment.getObservation() != null) {
            labelObservation.setText(appointment.getObservation());
        } else {
            labelObservation.setText(root.getContext().getString(R.string.add_observation));
        }
    }

    /**
     * Method to show dialog edit observation, then you could choose any option,
     * but it validates if you are premium too
     */
    private void showDialog() {
        CustomAlertDialogBuilder builder = new CustomAlertDialogBuilder(root.getContext());
        ViewGroup view = (ViewGroup) LayoutInflater.from(root.getContext()).inflate(R.layout.view_observation_dialog, null, false);

        TextInputEditText editText = view.findViewById(R.id.labelObservation);
        if (appointment.getObservation() != null) {
            editText.setText(appointment.getObservation());
        }

        LoadingButton btnConfirm = view.findViewById(R.id.btnConfirm);
        LoadingButton btnDelete = view.findViewById(R.id.btnDelete);

        btnConfirm.setOnClickListener(view1 -> {
                if (TextUtils.isEmpty(editText.getText())) {
                    //if(GetPremiumActivity.hasPremiumInPlaceOrShowScreen(activity, placeId , UserManagement.getInstance().getUser().getId())){
                        removeObservation(btnConfirm, builder);
                    //}
                    //btnConfirm.hideLoading();
                } else {
                    if(GetPremiumActivity.hasPremiumInPlaceOrShowScreen(activity, placeId , UserManagement.getInstance().getUser().getId())){
                        addObservation(editText.getText().toString(), btnConfirm, builder);
                    }
                    btnConfirm.hideLoading();
                }

            notifyListener();
        });
        btnDelete.setOnClickListener(view1 -> {
                removeObservation(btnDelete, builder);
                notifyListener();
        });

        ListView listViewSuggestions = view.findViewById(R.id.listViewSuggestions);
        SuggestionsAdapter adapter = new SuggestionsAdapter();
        listViewSuggestions.setOnItemClickListener((adapterView, view1, i, l) -> {
            editText.setText(((AppCompatTextView) view1).getText());
        });
        listViewSuggestions.setAdapter(adapter);

        builder.setView(view);
        builder.show();

    }

    private void notifyListener() {
        if (listener != null) {
            listener.onChange();
        }
    }

    private void addObservation(String observation, LoadingButton btnConfirm, CustomAlertDialogBuilder builder) {
        AppObservationManager.addObservationToAppointment(jobId, appointment.getId(), observation, new AppObservationManager.OnUpdateListener() {
            @Override
            public void onUpdate() {
                btnConfirm.hideLoading();
                appointment.setObservation(observation);
                updateView();
                builder.dismiss();
            }

            @Override
            public void onFailure() {
                btnConfirm.hideLoading();
            }
        });
    }

    private void removeObservation(LoadingButton btnConfirm, CustomAlertDialogBuilder builder) {
        AppObservationManager.deleteObservationFromAppointment(jobId, appointment.getId(), new AppObservationManager.OnUpdateListener() {
            @Override
            public void onUpdate() {
                btnConfirm.hideLoading();
                appointment.setObservation(null);
                updateView();
                builder.dismiss();
            }

            @Override
            public void onFailure() {
                btnConfirm.hideLoading();
            }
        });
    }

    public interface OnChangeListener {
        void onChange();
    }

    private class SuggestionsAdapter extends BaseAdapter {

        private final String[] suggestionsList = new String[]{
                root.getContext().getString(R.string.suggestion_1),
                root.getContext().getString(R.string.suggestion_2),
                root.getContext().getString(R.string.suggestion_3),
                root.getContext().getString(R.string.suggestion_4),
                root.getContext().getString(R.string.suggestion_5)
        };

        @Override
        public int getCount() {
            return suggestionsList.length;
        }

        @Override
        public Object getItem(int i) {
            return suggestionsList[i];
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup container) {
            if (convertView == null) {
                convertView = new AppCompatTextView(root.getContext());

            }

            ((AppCompatTextView) convertView).setText(suggestionsList[position]);
            ((AppCompatTextView) convertView).setTextColor(root.getContext().getColor(R.color.darker_grey));
            return convertView;
        }
    }
}
