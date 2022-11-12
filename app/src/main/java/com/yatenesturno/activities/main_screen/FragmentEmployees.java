package com.yatenesturno.activities.main_screen;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yatenesturno.Constants;
import com.yatenesturno.R;
import com.yatenesturno.activities.employee.EmployeeActivity;
import com.yatenesturno.custom_views.LoadingOverlay;
import com.yatenesturno.database.DatabaseDjangoRead;
import com.yatenesturno.database.ImageLoaderRead;
import com.yatenesturno.database.ImageLoaderReadImpl;
import com.yatenesturno.listeners.DatabaseCallback;
import com.yatenesturno.object_interfaces.CustomUser;
import com.yatenesturno.object_interfaces.Place;
import com.yatenesturno.serializers.BuilderListCoJobs;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;
import de.hdodenhof.circleimageview.CircleImageView;


public class FragmentEmployees extends Fragment {

    /**
     * Employee activiy launcher
     */
    private final ActivityResultLauncher<Intent> employeeActivityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    ((MainActivity) getActivity()).refreshContent();
                }
            });

    /**
     * Instance variables
     */
    private Place place;
    private Map<String, CustomUser> mapJobIdUser;
    private List<String> jobIds;
    private boolean hasRefreshed;
    private AdapterRecyclerViewEmployee adapterRecyclerViewEmployee;

    /**
     * UI references
     */
    private RecyclerView recyclerViewEmployees;
    private LoadingOverlay loadingOverlay;

    public FragmentEmployees() {

    }

    public Map<String, CustomUser> getMapJobIdUser() {
        return mapJobIdUser;
    }

    public void setState(Bundle bundle) {
        place = bundle.getParcelable("selectedPlace");
        mapJobIdUser = (Map<String, CustomUser>) bundle.getSerializable("mapJobIdUser");
        jobIds = (List<String>) bundle.getSerializable("jobIds");
    }

    public List<String> getJobIds() {
        return jobIds;
    }

    private void recoverState(Bundle bundle) {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        jobIds = new ArrayList<>();
        mapJobIdUser = new HashMap<>();
        Log.d("FRAGMENT EMPLOYEES", "ESTAS ACA");

        return inflater.inflate(R.layout.fragment_client, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState != null) {
            recoverState(savedInstanceState);
        }

        if (place != null) {
            initUI();
        }
    }

    private void initUI() {
        initViews();
        if (getContext() != null) {
            if (adapterRecyclerViewEmployee == null) {
                adapterRecyclerViewEmployee = new AdapterRecyclerViewEmployee();
                recyclerViewEmployees.setAdapter(adapterRecyclerViewEmployee);
            } else {
                adapterRecyclerViewEmployee.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (hasRefreshed) {
            initUI();
            hasRefreshed = false;
        }
    }

    public void initViews() {
        loadingOverlay = new LoadingOverlay(getView().findViewById(R.id.root));
        recyclerViewEmployees = getView().findViewById(R.id.recyclerViewEmployees);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerViewEmployees.setLayoutManager(layoutManager);
    }

    public void setPlace(Place place) {
        this.place = place;
        getCoWorkers();
    }

    private void getCoWorkers() {
        showLoadingOverlay();
        Map<String, String> body = getRequestBody();
        DatabaseDjangoRead.getInstance().GET(
                Constants.DJANGO_URL_JOBS_IN_PLACE,
                body,
                new DatabaseCallback() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        hideLoadingOverlay();
                        onCoWorkersFetch(response);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        hideLoadingOverlay();
                        showConnectionError();
                    }
                }
        );
    }

    private Map<String, String> getRequestBody() {
        Map<String, String> body = new HashMap<>();
        body.put("place_id", place.getId());
        return body;
    }

    private void onCoWorkersFetch(JSONObject response) {
        try {
            invalidate();
            if (mapJobIdUser == null) {
                mapJobIdUser = new BuilderListCoJobs().build(response);
                jobIds = new ArrayList<>(mapJobIdUser.keySet());
            } else {
                mapJobIdUser.clear();
                jobIds.clear();

                mapJobIdUser.putAll(new BuilderListCoJobs().build(response));
                jobIds.addAll(mapJobIdUser.keySet());
            }
        } catch (JSONException e) {
            mapJobIdUser = new HashMap<>();
            jobIds = new ArrayList<>();
            e.printStackTrace();
        }

        if (getView() != null) {
            initUI();
        } else {
            hasRefreshed = true;
        }
    }

    private void invalidate() {
        adapterRecyclerViewEmployee = null;
        mapJobIdUser = null;
        jobIds = null;
    }

    private void showLoadingOverlay() {
        if (loadingOverlay != null) {
            loadingOverlay.show();
        }
    }

    public void showConnectionError() {
        if (getContext() != null) {
            Toast.makeText(getActivity(), getString(R.string.error_de_conexion), Toast.LENGTH_SHORT).show();
        }
    }

    public void hideLoadingOverlay() {
        if (loadingOverlay != null) {
            loadingOverlay.hide();
        }
    }

    private void startEmployeeActivity(int position) {
        Intent intent = new Intent(getContext(), EmployeeActivity.class);
        Bundle bundle = new Bundle();

        String jobId = jobIds.get(position);
        bundle.putSerializable("employee", mapJobIdUser.get(jobId));
        bundle.putString("jobId", jobId);
        bundle.putParcelable("place", place);

        intent.putExtras(bundle);
        employeeActivityLauncher.launch(intent);
    }

    private class AdapterRecyclerViewEmployee extends RecyclerView.Adapter<AdapterRecyclerViewEmployee.ViewHolderEmployee> {

        private final Map<String, Bitmap> userBitmapMap;

        public AdapterRecyclerViewEmployee() {
            userBitmapMap = new HashMap<>();
        }

        @NonNull
        @Override
        public ViewHolderEmployee onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.employee_item_layout, parent, false);
            return new ViewHolderEmployee(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolderEmployee holder, final int position) {

            CustomUser user = mapJobIdUser.get(jobIds.get(position));
            holder.labelEmployee.setText(user.getName());
            holder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startEmployeeActivity(position);
                }
            });

            if (!userBitmapMap.containsKey(user.getId())) {
                getProfileImage(user, holder.ivPic);
            } else {
                Bitmap image = userBitmapMap.get(user.getId());
                if (image != null) {
                    holder.ivPic.setImageBitmap(image);
                }
            }
        }

        private void getProfileImage(final CustomUser user, final ImageView iv) {
            new ImageLoaderReadImpl().getImage(getContext(), user, new ImageLoaderRead.OnGetImageListener() {
                @Override
                public void onSuccess(Bitmap bitmap) {
                    userBitmapMap.put(user.getId(), bitmap);
                    iv.setImageBitmap(bitmap);
                }

                @Override
                public void onFailure() {

                }
            });
        }

        @Override
        public int getItemCount() {
            return jobIds.size();
        }

        public class ViewHolderEmployee extends RecyclerView.ViewHolder {

            public TextView labelEmployee;
            public RelativeLayout cardView;
            public CircleImageView ivPic;

            public ViewHolderEmployee(@NonNull View view) {
                super(view);

                labelEmployee = view.findViewById(R.id.labelEmployee);
                cardView = view.findViewById(R.id.root);
                ivPic = view.findViewById(R.id.ivPic);
            }
        }
    }
}