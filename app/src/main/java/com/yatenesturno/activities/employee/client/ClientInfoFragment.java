package com.yatenesturno.activities.employee.client;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.yatenesturno.Constants;
import com.yatenesturno.R;
import com.yatenesturno.database.djangoImpl.DatabaseDjangoRead;
import com.yatenesturno.database.interfaces.ImageLoaderRead;
import com.yatenesturno.database.ImageLoaderReadImpl;
import com.yatenesturno.listeners.DatabaseCallback;
import com.yatenesturno.object_interfaces.CustomUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import cz.msebera.android.httpclient.Header;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ClientInfoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ClientInfoFragment extends Fragment {

    /**
     * Parameter arguments
     */
    private static final String ARG_PARAM_JOB_ID = "job_id";
    private static final String ARG_PARAM_CLIENT = "client";

    /**
     * Parameters
     */
    private String jobId;
    private CustomUser client;

    /**
     * View references
     */
    private AppCompatTextView labelClientName, labelClientEmail, labelClientWhatsapp;
    private AppCompatImageButton btnCopyEmail, btnCopyWhatsapp;
    private CircleImageView imageClient;
    private CardView btnAppointments, btnJobCredits;

    private boolean isRunning;

    public ClientInfoFragment() {
        // Required empty public constructor
    }

    public static ClientInfoFragment newInstance(String jobId, CustomUser client) {
        ClientInfoFragment fragment = new ClientInfoFragment();

        Bundle args = new Bundle();
        args.putString(ARG_PARAM_JOB_ID, jobId);
        args.putParcelable(ARG_PARAM_CLIENT, client);
        fragment.setArguments(args);

        return fragment;
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
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        saveState(outState);
    }

    private void saveState(Bundle outState) {
        outState.putString(ARG_PARAM_JOB_ID, jobId);
        outState.putParcelable(ARG_PARAM_CLIENT, client);
    }

    private void recoverState(Bundle bundle) {
        jobId = bundle.getString(ARG_PARAM_JOB_ID);
        client = bundle.getParcelable(ARG_PARAM_CLIENT);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_client_info, container, false);

        labelClientName = view.findViewById(R.id.labelClientName);
        labelClientEmail = view.findViewById(R.id.labelClientEmail);
        labelClientWhatsapp = view.findViewById(R.id.labelClientWhatsapp);
        btnCopyEmail = view.findViewById(R.id.btnCopyEmail);
        btnCopyWhatsapp = view.findViewById(R.id.btnCopyWhatsapp);
        imageClient = view.findViewById(R.id.imageClient);
        btnAppointments = view.findViewById(R.id.btnAppointments);
        btnJobCredits = view.findViewById(R.id.btnJobCredits);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fetchClientInfo();

        btnAppointments.setOnClickListener(view1 -> {
//           ((ClientInfoActivity) getActivity()).showAppointmentsFragment();
            Snackbar.make(view, "Próximamente", Snackbar.LENGTH_SHORT).show();
        });
    }

    public void fetchClientInfo() {
        Map<String, String> body = getBody();

        DatabaseDjangoRead.getInstance().GET(
                Constants.DJANGO_URL_NOTIFICATION_METHOD,
                body,
                new DatabaseCallback() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        if (isRunning) {
                            onInfoFetch(response);
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        Toast.makeText(getContext(), getString(R.string.error_de_conexion), Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    public Map<String, String> getBody() {
        Map<String, String> body = new HashMap<>();
        body.put("client_id", client.getId());
        return body;
    }

    private void onInfoFetch(JSONObject response) {
        try {
            JSONObject notificationJson = response.getJSONObject("notification");
            String notificationType = notificationJson.getString("notification_type");
            String contactInfo = notificationJson.getString("contact_info");

            labelClientName.setText(client.getName());
            getClientImage();

            btnCopyWhatsapp.setOnClickListener(view -> copyToClipBoard(contactInfo));
            btnCopyEmail.setOnClickListener(view -> copyToClipBoard(client.getEmail()));

            if (notificationType.equals("email")) {
                getView().findViewById(R.id.wrapperWhatsapp).setVisibility(View.INVISIBLE);
            } else {
                labelClientWhatsapp.setText(contactInfo);
            }

            labelClientEmail.setText(client.getEmail());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void copyToClipBoard(String text) {
        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("contact info", text);
        clipboard.setPrimaryClip(clip);

        showSnackBarCopiedToClipBoard();
    }

    private void showSnackBarCopiedToClipBoard() {
        Snackbar.make(getView().findViewById(R.id.root), getString(R.string.copied_to_clipboard), Snackbar.LENGTH_SHORT).show();
    }

    private void getClientImage() {
        new ImageLoaderReadImpl().getImage(getContext(), client, new ImageLoaderRead.OnGetImageListener() {
            @Override
            public void onSuccess(Bitmap bitmap) {
                imageClient.setImageBitmap(bitmap);
            }

            @Override
            public void onFailure() {

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        isRunning = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        isRunning = false;
    }

    public void showBtnCredits() {
        btnJobCredits.setVisibility(View.VISIBLE);
        btnJobCredits.setOnClickListener(view -> ((ClientInfoActivity) getActivity()).showJobCreditsFragment());
    }
}