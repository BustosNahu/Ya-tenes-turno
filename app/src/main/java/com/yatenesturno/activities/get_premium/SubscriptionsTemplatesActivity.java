package com.yatenesturno.activities.get_premium;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yatenesturno.Constants;
import com.yatenesturno.R;
import com.yatenesturno.custom_views.LoadingOverlay;
import com.yatenesturno.database.DatabaseDjangoRead;
import com.yatenesturno.listeners.DatabaseCallback;
import com.yatenesturno.object_interfaces.Job;
import com.yatenesturno.object_interfaces.SubscriptionToken;
import com.yatenesturno.serializers.BuilderListSubscriptionToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import cz.msebera.android.httpclient.Header;

public class SubscriptionsTemplatesActivity extends AppCompatActivity {


    private List<SubscriptionToken> placeSubscriptionList, jobSubscriptionList;
    private RecyclerView recyclerViewPlaceTemplates, recyclerViewJobTemplates;
    private TextView labelPlaceTemplates, labelJobTemplates;
    private LoadingOverlay loadingOverlay;
    private final ActivityResultLauncher<Intent> subscriptionLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                loadingOverlay.hide();
                if (result.getResultCode() == Activity.RESULT_OK) {
                    querySubscriptionTemplates();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_subscriptions_templates);
        getSupportActionBar().setBackgroundDrawable(getDrawable(R.color.black));
        getSupportActionBar().setElevation(0);
        getSupportActionBar().setTitle("Suscripciones");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        new Handler(Looper.myLooper()).postDelayed(this::init, 100);
    }

    private void init() {

        initViews();
        querySubscriptionTemplates();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initViews() {
        loadingOverlay = new LoadingOverlay(findViewById(R.id.root));
        labelPlaceTemplates = findViewById(R.id.labelPlaceTemplates);
        labelJobTemplates = findViewById(R.id.labelJobTemplates);

        recyclerViewPlaceTemplates = findViewById(R.id.recyclerViewPlaceTemplates);
        recyclerViewJobTemplates = findViewById(R.id.recyclerViewJobTemplates);

        loadingOverlay.show();
    }

    private void querySubscriptionTemplates() {
        loadingOverlay.show();
        DatabaseDjangoRead.getInstance().GET(
                Constants.DJANGO_URL_ACCEPT_FETCH_SUBSCRIPTIONS,
                null,
                new DatabaseCallback() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        handleResponse(response);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        Toast.makeText(getApplicationContext(),responseString,Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    public void handleResponse(JSONObject response) {
        try {
            JSONArray placeSubTemplates = response.getJSONArray("place_sub_templates");
            JSONArray jobSubTemplates = response.getJSONArray("job_sub_templates");

            placeSubscriptionList = new BuilderListSubscriptionToken().build(placeSubTemplates);
            jobSubscriptionList = new BuilderListSubscriptionToken().build(jobSubTemplates);

            loadingOverlay.hide();
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),"ERROR: " + e,Toast.LENGTH_LONG).show();

        }

        updateVisibility();
    }

    private void updateVisibility() {
        if (placeSubscriptionList.size() == 0 && jobSubscriptionList.size() == 0) {
            findViewById(R.id.labelNoSubscriptions).setVisibility(View.VISIBLE);
            return;
        } else {
            findViewById(R.id.labelNoSubscriptions).setVisibility(View.GONE);
        }

        if (placeSubscriptionList.size() > 0) {
            showPlaceTemplates();
        } else {
            hidePlacesTemplates();
        }

        if (jobSubscriptionList.size() > 0) {
            showJobTemplates();
        } else {
            hideJobsTemplates();
        }
    }

    private void showPlaceTemplates() {
        recyclerViewPlaceTemplates.setVisibility(View.VISIBLE);
        labelPlaceTemplates.setVisibility(View.VISIBLE);

        LinearLayoutManager lp = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        recyclerViewPlaceTemplates.setLayoutManager(lp);

        recyclerViewPlaceTemplates.setAdapter(new SubTemplateAdapter(placeSubscriptionList));
    }

    private void hidePlacesTemplates() {
        recyclerViewPlaceTemplates.setVisibility(View.GONE);
        labelPlaceTemplates.setVisibility(View.GONE);

    }

    private void showJobTemplates() {
        recyclerViewJobTemplates.setVisibility(View.VISIBLE);
        labelJobTemplates.setVisibility(View.VISIBLE);

        LinearLayoutManager lp = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        recyclerViewJobTemplates.setLayoutManager(lp);

        recyclerViewJobTemplates.setAdapter(new SubTemplateAdapter(jobSubscriptionList));
    }

    private void hideJobsTemplates() {
        recyclerViewJobTemplates.setVisibility(View.GONE);
        labelJobTemplates.setVisibility(View.GONE);
    }

    private void openSubscriptionActivity(SubscriptionToken subscriptionToken) {
        Intent intent = new Intent(this, ViewSubscriptionTemplateActivity.class);

        Bundle extras = new Bundle();
        extras.putParcelable("subscriptionToken", subscriptionToken);
        intent.putExtras(extras);

        loadingOverlay.show();
        subscriptionLauncher.launch(intent);
    }

    private class SubTemplateAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int VIEW_TYPE_COMBO = 0, VIEW_TYPE_INDIVIDUAL = 1;

        private final List<SubscriptionToken> subscriptionTokens;

        public SubTemplateAdapter(List<SubscriptionToken> subscriptionTokens) {
            this.subscriptionTokens = subscriptionTokens;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getApplicationContext());
            RecyclerView.ViewHolder viewHolder;
            switch (viewType) {
                case VIEW_TYPE_COMBO:
                    viewHolder = new ViewHolderCombo(layoutInflater.inflate(R.layout.sub_template_combo, parent, false));
                    break;
                case VIEW_TYPE_INDIVIDUAL:
                    viewHolder = new ViewHolderIndividual(layoutInflater.inflate(R.layout.sub_template_individual, parent, false));
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + viewType);
            }

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            switch (holder.getItemViewType()) {
                case VIEW_TYPE_COMBO:
                    bindComboViewHolder((ViewHolderCombo) holder, subscriptionTokens.get(position));
                    break;
                case VIEW_TYPE_INDIVIDUAL:
                    bindIndividualViewHolder((ViewHolderIndividual) holder, subscriptionTokens.get(position));
                    break;
            }
        }

        private void bindIndividualViewHolder(ViewHolderIndividual holder, SubscriptionToken subscriptionToken) {
            holder.labelDescription.setText(subscriptionToken.getTemplate().getDescription());
            holder.labelPlace.setText(subscriptionToken.getPlace().getBusinessName());

            holder.cardView.setOnClickListener(new OnTokenClickListener(subscriptionToken));
            if (subscriptionToken.isValid()) {
                holder.validCardView.setVisibility(View.VISIBLE);
            } else {
                holder.validCardView.setVisibility(View.GONE);
            }
            holder.ivVerified.setVisibility(View.GONE);
            holder.ivCombo.setImageDrawable(AppCompatResources.getDrawable(SubscriptionsTemplatesActivity.this, R.drawable.ic_outline_verified_24_big_primary));
        }

        private void bindComboViewHolder(ViewHolderCombo holder, SubscriptionToken subscriptionToken) {
            holder.labelDescription.setText(subscriptionToken.getTemplate().getDescription());
            holder.labelPlace.setText(subscriptionToken.getPlace().getBusinessName());

            StringBuilder employeeNames = new StringBuilder();
            for (Job job : subscriptionToken.getPlace().getJobList()) {
                employeeNames.append(job.getEmployee().getName()).append(", ");
            }
            Log.d("SUBSCRIPTIONDEBUG", employeeNames.toString());
            holder.labelEmployees.setText(employeeNames.substring(0, employeeNames.length()));
            holder.cardView.setOnClickListener(new OnTokenClickListener(subscriptionToken));

            if (subscriptionToken.isValid()) {
                holder.validCardView.setVisibility(View.VISIBLE);
            } else {
                holder.validCardView.setVisibility(View.GONE);
            }
            holder.ivVerified.setVisibility(View.GONE);
            holder.ivCombo.setImageDrawable(AppCompatResources.getDrawable(SubscriptionsTemplatesActivity.this, R.drawable.ic_outline_verified_24_big_primary));
        }

        @Override
        public int getItemViewType(int position) {
            return subscriptionTokens.get(position).getTemplate().isCombo() ? VIEW_TYPE_COMBO : VIEW_TYPE_INDIVIDUAL;
        }

        @Override
        public int getItemCount() {
            return subscriptionTokens.size();
        }

        private class ViewHolderCombo extends RecyclerView.ViewHolder {

            TextView labelPlace, labelDescription, labelEmployees;
            CardView cardView, validCardView;
            ImageView ivVerified, ivCombo;

            public ViewHolderCombo(@NonNull View itemView) {
                super(itemView);
                this.ivVerified = itemView.findViewById(R.id.ivVerified);
                this.ivCombo = itemView.findViewById(R.id.imageView);
                this.validCardView = itemView.findViewById(R.id.labelValid);
                this.cardView = itemView.findViewById(R.id.cardViewHolder);
                this.labelDescription = itemView.findViewById(R.id.labelComboDescription);
                this.labelPlace = itemView.findViewById(R.id.labelPlace);
                this.labelEmployees = itemView.findViewById(R.id.labelEmployees);
            }
        }

        private class ViewHolderIndividual extends RecyclerView.ViewHolder {

            TextView labelPlace, labelDescription;
            CardView cardView, validCardView;
            ImageView ivVerified, ivCombo;

            public ViewHolderIndividual(@NonNull View itemView) {
                super(itemView);
                this.ivVerified = itemView.findViewById(R.id.ivVerified);
                this.ivCombo = itemView.findViewById(R.id.imageView);
                this.validCardView = itemView.findViewById(R.id.labelValid);
                this.cardView = itemView.findViewById(R.id.cardViewHolder);
                this.labelDescription = itemView.findViewById(R.id.labelComboDescription);
                this.labelPlace = itemView.findViewById(R.id.labelPlace);
            }
        }
    }

    private class OnTokenClickListener implements View.OnClickListener {

        private final SubscriptionToken token;

        public OnTokenClickListener(SubscriptionToken token) {
            this.token = token;
        }

        @Override
        public void onClick(View v) {
            openSubscriptionActivity(token);
        }
    }
}