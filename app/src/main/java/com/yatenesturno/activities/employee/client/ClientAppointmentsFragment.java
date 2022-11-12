package com.yatenesturno.activities.employee.client;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.yatenesturno.R;
import com.yatenesturno.object_interfaces.CustomUser;

/**
 * A simple {@link Fragment} subclass.
 *
 * */
public class ClientAppointmentsFragment extends Fragment {

    private static final String ARG_PARAM_JOB_ID = "job_id";
    private static final String ARG_PARAM_CLIENT = "client";

    private String jobId;
    private CustomUser client;

    public ClientAppointmentsFragment() {

    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param job job of the client
     * @param client client
     * @return A new instance of fragment ClientJobCredits.
     */
    public static ClientAppointmentsFragment newInstance(String job, CustomUser client) {
        ClientAppointmentsFragment fragment = new ClientAppointmentsFragment();

        Bundle args = new Bundle();
        args.putString(ARG_PARAM_JOB_ID, job);
        args.putParcelable(ARG_PARAM_CLIENT, client);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            jobId = getArguments().getString(ARG_PARAM_JOB_ID);
            client = getArguments().getParcelable(ARG_PARAM_CLIENT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_client_appointments, container, false);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(ARG_PARAM_JOB_ID, jobId);
        outState.putParcelable(ARG_PARAM_CLIENT, client);
    }
}