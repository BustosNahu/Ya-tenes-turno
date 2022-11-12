package com.yatenesturno.activities.purchases.single_purchase;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yatenesturno.Constants;
import com.yatenesturno.R;
import com.yatenesturno.custom_views.LoadingOverlay;
import com.yatenesturno.database.DatabaseDjangoRead;
import com.yatenesturno.database.DatabaseDjangoWrite;
import com.yatenesturno.database.ImageLoaderRead;
import com.yatenesturno.database.ImageLoaderReadImpl;
import com.yatenesturno.listeners.DatabaseCallback;
import com.yatenesturno.object_interfaces.CustomUser;
import com.yatenesturno.object_interfaces.Job;
import com.yatenesturno.object_interfaces.Place;
import com.yatenesturno.object_interfaces.PlacePremium;
import com.yatenesturno.user_auth.UserManagement;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;
import de.hdodenhof.circleimageview.CircleImageView;

public class PurchaseViewActivity extends AppCompatActivity {

    /**
     * class variables
     */
    public static final String PLACE_PREMIUM = "placePremium";

    /**
     * instance variables
     */
    private PlacePremium placePremium;
    private CreditsPacksFragment slidingCreditPacksFragment;
    private CustomUser selectedEmployee;
    private Place place;

    /**
     * UI References
     */
    private RecyclerView recyclerViewEmployees;
    private LoadingOverlay loadingOverlay;

    private final ActivityResultLauncher<Intent> purchaseLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                loadingOverlay.hide();
                if (result.getResultCode() == Activity.RESULT_OK) {
                    onCreditsCommitted();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase_view);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(getDrawable(R.color.white));
        getSupportActionBar().setElevation(0);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        if (savedInstanceState != null) {
            recoverState(savedInstanceState);
        } else {
            this.placePremium = getIntent().getExtras().getParcelable(PLACE_PREMIUM);
            this.place =getIntent().getExtras().getParcelable("place");
        }

        initUI();
    }

    private void onCreditsCommitted() {
        slidingCreditPacksFragment.dismiss();
        fillRecyclerView();
    }

    private void initUI() {
        getSupportActionBar().setTitle(place.getBusinessName());

        this.loadingOverlay = new LoadingOverlay(findViewById(R.id.root));
        this.recyclerViewEmployees = findViewById(R.id.recyclerViewEmployees);
        this.recyclerViewEmployees.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));

        loadingOverlay.show();
        initSlidingFragment();
        fillRecyclerView();
    }

    private void initSlidingFragment() {
        this.slidingCreditPacksFragment = new CreditsPacksFragment();
        slidingCreditPacksFragment.setOnPurchaseListener(this::onPurchaseClick);
    }

    private void onPurchaseClick(CreditPack creditPack) {
        Intent intent = new Intent(this, PurchaseCreditPackActivity.class);

        Bundle extras = new Bundle();
        extras.putParcelable("creditPack", creditPack);
        extras.putParcelable("place", place);
        extras.putString("jobId", getJobForEmployee(place, selectedEmployee).getId());
        intent.putExtras(extras);

        loadingOverlay.show();
        purchaseLauncher.launch(intent);
    }

    private void fillRecyclerView() {

        List<CustomUser> employeeList = new ArrayList<>();
        if (isOwner() && placePremium.getTemplate() != null && placePremium.getTemplate().isCombo()) {
            for (Job job : place.getJobList()) {
                employeeList.add(job.getEmployee());
            }
        } else {
            employeeList.add(UserManagement.getInstance().getUser());
        }

        AdapterEmployeeCredits adapter = new AdapterEmployeeCredits(employeeList);
        recyclerViewEmployees.setAdapter(adapter);
        loadingOverlay.hide();
    }

    private boolean isOwner() {
        return place.getOwner().getId().equals(UserManagement.getInstance().getUser().getId());
    }

    private void recoverState(Bundle savedInstanceState) {
        placePremium = savedInstanceState.getParcelable(PLACE_PREMIUM);
        place= savedInstanceState.getParcelable("place");
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(PLACE_PREMIUM, placePremium);
        outState.putParcelable("place", place);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private Job getJobForEmployee(Place place, CustomUser user) {
        for (Job job : place.getJobList()) {
            if (job.getEmployee().getId().equals(user.getId())) {
                return job;
            }
        }
        return null;
    }

    private void getCredits(CustomUser employee, ListenerGetCredits listener) {
        Job job = getJobForEmployee(place, employee);
        DatabaseDjangoRead.getInstance().GET(
                String.format(Constants.DJANGO_URL_CREDITS, place.getId(), job.getId()),
                null,
                new DatabaseCallback() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        handleGetMessagesResponse(response, listener);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

                    }
                }
        );
    }

    private void handleGetMessagesResponse(JSONObject response, ListenerGetCredits listener) {
        try {
            JSONObject whatsappCreditsObject = response.getJSONObject("credits");
            listener.onSuccess(whatsappCreditsObject.getInt("credits"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private interface ListenerGetCredits {
        void onSuccess(int remaining_messages);
    }

    private class AdapterEmployeeCredits extends RecyclerView.Adapter<AdapterEmployeeCredits.ViewHolderEmployee> {

        private final List<CustomUser> employeeList;
        private final ImageLoaderRead imageLoaderRead;
        private final Map<String, Integer> creditsMap;

        public AdapterEmployeeCredits(List<CustomUser> employeeList) {
            this.employeeList = employeeList;
            this.imageLoaderRead = new ImageLoaderReadImpl();
            creditsMap = new HashMap<>();
        }

        @NonNull
        @Override
        public ViewHolderEmployee onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(PurchaseViewActivity.this);
            ViewHolderEmployee viewHolder;

            viewHolder = new ViewHolderEmployee(layoutInflater.inflate(R.layout.employee_messages_list_item, parent, false));

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolderEmployee holder, int position) {
            CustomUser employee = employeeList.get(position);
            holder.labelEmployeeName.setText(employee.getName());

            imageLoaderRead.getImage(getApplicationContext(), employee, new ImageLoaderRead.OnGetImageListener() {
                @Override
                public void onSuccess(Bitmap bitmap) {
                    holder.imageView.setImageBitmap(bitmap);
                }

                @Override
                public void onFailure() {

                }
            });

            if (creditsMap.containsKey(employee.getId())) {
                holder.remainingWhatsappCredits.setText(creditsMap.get(employee.getId()));
            } else {
                fetchCredits(holder, employee);
            }

            holder.btnBuyCredits.setOnClickListener(view -> onBuyMoreCreditsClick(employee));
        }

        private void onBuyMoreCreditsClick(CustomUser employee) {
            selectedEmployee = employee;
            slidingCreditPacksFragment.show(getSupportFragmentManager(), employee.getGivenName());
        }

        private void fetchCredits(ViewHolderEmployee holder, CustomUser employee) {
            getCredits(employee, credits -> {
                creditsMap.put(employee.getId(), credits);
                holder.remainingWhatsappCredits.setText(credits + "");
            });
        }

        @Override
        public int getItemCount() {
            return employeeList.size();
        }

        private class ViewHolderEmployee extends RecyclerView.ViewHolder {

            TextView remainingWhatsappCredits, labelEmployeeName;
            CircleImageView imageView;
            CardView btnBuyCredits;

            public ViewHolderEmployee(@NonNull View itemView) {
                super(itemView);

                this.imageView = itemView.findViewById(R.id.imageView);
                this.btnBuyCredits = itemView.findViewById(R.id.btnBuyMessages);
                this.labelEmployeeName = itemView.findViewById(R.id.labelEmployeeName);
                this.remainingWhatsappCredits = itemView.findViewById(R.id.remainingWhatsappCredits);
            }
        }
    }
}