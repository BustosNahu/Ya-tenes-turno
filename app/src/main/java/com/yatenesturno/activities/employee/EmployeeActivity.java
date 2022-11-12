package com.yatenesturno.activities.employee;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yatenesturno.Constants;
import com.yatenesturno.R;
import com.yatenesturno.activities.employee.client.ClientInfoActivity;
import com.yatenesturno.activities.get_premium.GetPremiumActivity;
import com.yatenesturno.activities.main_screen.FragmentAdminClients;
import com.yatenesturno.custom_views.LoadingOverlay;
import com.yatenesturno.database.DatabaseDjangoRead;
import com.yatenesturno.database.ImageLoaderRead;
import com.yatenesturno.database.ImageLoaderReadImpl;
import com.yatenesturno.listeners.DatabaseCallback;
import com.yatenesturno.object_interfaces.CustomUser;
import com.yatenesturno.object_interfaces.Job;
import com.yatenesturno.object_interfaces.Place;
import com.yatenesturno.serializers.BuilderListCustomUser;
import com.yatenesturno.user_auth.UserManagement;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;
import de.hdodenhof.circleimageview.CircleImageView;
import eu.davidea.flexibleadapter.FlexibleAdapter;

public class EmployeeActivity extends AppCompatActivity {

    /**
     * Instance variables
     */
    private boolean isRunning;
    private Place place;
    private String jobId;
    private List<CustomUser> clientList;
    private FlexibleAdapter<FragmentAdminClients.FlexibleClientItem> adapter;

    /**
     * UI References
     */
    private LoadingOverlay loadingOverlay;
    private RecyclerView recyclerViewClients;
    /**
     * Client activity launcher
     */
    private final ActivityResultLauncher<Intent> clientActivityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    setResult(RESULT_OK);
                    getClients();
                }
            });
    private SearchView searchView;
    private CardView holderSearchViewClients;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(getDrawable(R.color.white));
        getSupportActionBar().setElevation(0);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        Bundle bundle = getIntent().getExtras();
        CustomUser employee = (CustomUser) bundle.getSerializable("employee");
        setTitle(employee.getName());

        jobId = bundle.getString("jobId");
        place = bundle.getParcelable("place");
        Log.d("employessPlaceName", place.getBusinessName());

        setTitle("");

        initView(employee);

        getClients();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isRunning = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isRunning = false;
    }

    private void initView(CustomUser employee) {
        TextView labelEmployeeName = findViewById(R.id.labelEmployeeName);
        CircleImageView imageViewEmployee = findViewById(R.id.imageViewEmployee);

        labelEmployeeName.setText(employee.getName());
        new ImageLoaderReadImpl().getImage(this, employee, new ImageLoaderRead.OnGetImageListener() {
            @Override
            public void onSuccess(Bitmap bitmap) {
                imageViewEmployee.setImageBitmap(bitmap);
            }

            @Override
            public void onFailure() {

            }
        });

        recyclerViewClients = findViewById(R.id.recyclerViewClients);
        searchView = findViewById(R.id.searchViewClients);
        holderSearchViewClients = findViewById(R.id.holderSearchViewClients);
        loadingOverlay = new LoadingOverlay(findViewById(R.id.root));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getClients() {
        loadingOverlay.show();
        Map<String, String> body = getBody();

        DatabaseDjangoRead.getInstance().GET(
                Constants.DJANGO_URL_CLIENTS_OF_JOB,
                body,
                new DatabaseCallback() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        loadingOverlay.hide();
                        onClientsFetch(response);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        loadingOverlay.hide();

                        if (isRunning) {
                            showConnectionError();
                        }
                    }
                }
        );
    }

    private Map<String, String> getBody() {
        Map<String, String> body = new HashMap<>();
        body.put("job_id", jobId);
        return body;
    }

    public void showConnectionError() {
        Toast.makeText(getApplicationContext(), getString(R.string.error_de_conexion), Toast.LENGTH_SHORT).show();
    }

    private void onClientsFetch(JSONObject response) {
        BuilderListCustomUser builderListCustomUser = new BuilderListCustomUser();
        try {
            clientList = builderListCustomUser.build(response);
            Log.d("ClientsList", clientList.toString());

        } catch (JSONException e) {
            e.printStackTrace();
            clientList = new ArrayList<>();
        }
        populateClientsView();
    }

    private void populateClientsView() {
        if (hasClients()) {
            setUpHasClientsView();

            Collections.sort(clientList, (t0, t1) -> t0.getName().toLowerCase().compareTo(t1.getName().toLowerCase()));

            List<FragmentAdminClients.FlexibleClientItem> clientItemList = new ArrayList<>();
            for (CustomUser customUser : clientList) {
                clientItemList.add(new FragmentAdminClients.FlexibleClientItem(customUser, view -> {
                    checkCanViewClientInfo(customUser);
                }));
            }
            recyclerViewClients.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
            adapter = new FlexibleAdapter<>(clientItemList);
            recyclerViewClients.setAdapter(adapter);

            setUpSearchView();
        } else {
            setUpNoClientsView();
        }
    }

    private void setUpSearchView() {
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

    private void setUpNoClientsView() {
        findViewById(R.id.labelNoClients).setVisibility(View.VISIBLE);
        recyclerViewClients.setVisibility(View.GONE);
    }

    private void setUpHasClientsView() {
        findViewById(R.id.labelNoClients).setVisibility(View.GONE);
        recyclerViewClients.setVisibility(View.VISIBLE);
    }

    private boolean hasClients() {
        return clientList.size() > 0;
    }

    private void checkCanViewClientInfo(CustomUser user) {
        if (GetPremiumActivity.hasPremiumInPlaceOrShowScreen(this, place.getId(), UserManagement.getInstance().getUser().getId())) {
            startViewClientInfoActivity(user);
        }
    }

    private void startViewClientInfoActivity(CustomUser user) {
        Intent intent = new Intent(this, ClientInfoActivity.class);
        Bundle bundle = new Bundle();

        bundle.putSerializable("client", user);
        bundle.putBoolean("hasEditRights", hasEditRights());
        bundle.putString("jobId", jobId);
        bundle.putString("placeId", place.getId());

        intent.putExtras(bundle);
        clientActivityLauncher.launch(intent);
    }

    private boolean hasEditRights() {
        if (place.getOwner().getId().equals(UserManagement.getInstance().getUser().getId())) {
            return true;
        }

        Job job = place.getJobById(jobId);
        if (job == null) {
            return false;
        }

        return place.getJobById(jobId).getEmployee().getId().equals(UserManagement.getInstance().getUser().getId());
    }

    public class AdapterClients extends BaseAdapter {

        private final List<CustomUser> clientList;

        public AdapterClients(List<CustomUser> list) {
            this.clientList = list;
        }

        @Override
        public int getCount() {
            return clientList.size();
        }

        @Override
        public Object getItem(int position) {
            return clientList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.client_layout, parent, false);
            }
            ((TextView) convertView.findViewById(R.id.labelClient)).setText(clientList.get(position).getName());

            return convertView;
        }
    }
}