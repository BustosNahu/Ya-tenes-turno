package com.yatenesturno.activities.purchases;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yatenesturno.Constants;
import com.yatenesturno.R;
import com.yatenesturno.activities.purchases.single_purchase.PurchaseViewActivity;
import com.yatenesturno.custom_views.LoadingOverlay;
import com.yatenesturno.database.djangoImpl.DatabaseDjangoRead;
import com.yatenesturno.database.interfaces.ImageLoaderRead;
import com.yatenesturno.database.ImageLoaderReadImpl;
import com.yatenesturno.functionality.ManagerPlace;
import com.yatenesturno.listeners.DatabaseCallback;
import com.yatenesturno.object_interfaces.Job;
import com.yatenesturno.object_interfaces.Place;
import com.yatenesturno.object_interfaces.PlacePremium;
import com.yatenesturno.objects.PlacePremiumImpl;
import com.yatenesturno.serializers.BuilderListPlacePremium;
import com.yatenesturno.user_auth.UserManagement;
import com.yatenesturno.utils.CalendarUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import de.hdodenhof.circleimageview.CircleImageView;

public class PurchasesActivity extends AppCompatActivity {

    private final boolean populated = false;
    private List<PlacePremium> placePremiumList;
    private RecyclerView recyclerViewSubscriptions;
    private LoadingOverlay loadingOverlay;
    private boolean hasTrial;
    private Calendar trialValidUntil;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchases);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(getDrawable(R.color.white));
        getSupportActionBar().setElevation(0);
        getSupportActionBar().setTitle("Mis Compras");
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        initViews();
        new Handler(Looper.myLooper()).postDelayed(() -> {
            loadingOverlay.show();
            if (placePremiumList != null && !populated) {
                populateRecyclerView();
            }
        }, 100);
        fetchSubscriptions();
    }

    private void initViews() {
        this.loadingOverlay = new LoadingOverlay(findViewById(R.id.root));
        this.recyclerViewSubscriptions = findViewById(R.id.recyclerViewSubscriptions);
    }

    private void fetchSubscriptions() {
        DatabaseDjangoRead.getInstance().GET(
                Constants.DJANGO_URL_GET_SUBSCRIPTIONS,
                null,
                new DatabaseCallback() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        handleResponse(response);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

                    }
                }
        );
    }

    private void handleResponse(JSONObject response) {
        try {
            hasTrial = response.has("trial") && !response.isNull("trial");

            if (hasTrial) {
                trialValidUntil = CalendarUtils.parseDate(response.getJSONObject("trial").getString("valid_until"));

                /*
                    Add mock place premium for trial, so the user can buy message credits
                 */
                ManagerPlace.getInstance().getPlaces(new ManagerPlace.OnPlaceListFetchListener() {
                    @Override
                    public void onFetch(List<Place> placeList) {
                        placePremiumList = new ArrayList<>();
                        for (Place place : placeList) {
                            PlacePremiumImpl placePremium = new PlacePremiumImpl();

                            placePremium.setPlace(place);
                            placePremium.setUser(UserManagement.getInstance().getUser());

                            placePremiumList.add(placePremium);
                        }
                        populateRecyclerView();
                    }

                    @Override
                    public void onFailure() {

                    }
                });

            } else {
                placePremiumList = new BuilderListPlacePremium().build(response.getJSONArray("subscriptions"));
                populateRecyclerView();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private long getRemainingDays() {
        long millisDiff = trialValidUntil.getTimeInMillis() - Calendar.getInstance().getTimeInMillis();

        return (millisDiff / 1000) / 60 / 60 / 24;
    }

    private void populateRecyclerView() {
        loadingOverlay.hide();

        if (placePremiumList.size() > 0) {
            showRecyclerView();
            hideNoSubscriptions();
            recyclerViewSubscriptions.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));

            recyclerViewSubscriptions.setAdapter(new SubscriptionAdapter(placePremiumList));
        } else {
            hideRecyclerView();
            showNoSubscriptions();
        }
    }

    private void showNoSubscriptions() {
        findViewById(R.id.labelNoSubscriptions).setVisibility(View.VISIBLE);
    }

    private void hideNoSubscriptions() {
        findViewById(R.id.labelNoSubscriptions).setVisibility(View.GONE);
    }

    private void showRecyclerView() {
        recyclerViewSubscriptions.setVisibility(View.VISIBLE);
    }

    private void hideRecyclerView() {
        recyclerViewSubscriptions.setVisibility(View.GONE);
    }

    private class OnSubscriptionClickListener implements View.OnClickListener {

        private final PlacePremium placePremium;

        public OnSubscriptionClickListener(PlacePremium placePremium) {
            this.placePremium = placePremium;
        }

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(PurchasesActivity.this, PurchaseViewActivity.class);

            Bundle bundle = new Bundle();
            bundle.putParcelable("placePremium", placePremium);
            bundle.putParcelable("place", placePremium.getPlace());
            intent.putExtras(bundle);

            startActivity(intent);
        }
    }

    private class SubscriptionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int VIEW_TYPE_COMBO = 0, VIEW_TYPE_INDIVIDUAL = 1, VIEW_TYPE_TRIAL = 2;

        private final List<PlacePremium> subscriptionsList;
        private final ImageLoaderReadImpl imageLoaderRead;

        public SubscriptionAdapter(List<PlacePremium> subscriptionTokens) {
            this.subscriptionsList = subscriptionTokens;
            this.imageLoaderRead = new ImageLoaderReadImpl();
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(PurchasesActivity.this);
            RecyclerView.ViewHolder viewHolder;
            switch (viewType) {
                case VIEW_TYPE_COMBO:
                    viewHolder = new ViewHolderCombo(layoutInflater.inflate(R.layout.sub_template_combo, parent, false));
                    break;
                case VIEW_TYPE_INDIVIDUAL:
                case VIEW_TYPE_TRIAL:
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
                    bindComboViewHolder((ViewHolderCombo) holder, subscriptionsList.get(position));
                    break;
                case VIEW_TYPE_INDIVIDUAL:
                    bindIndividualViewHolder((ViewHolderIndividual) holder, subscriptionsList.get(position));
                    break;
                case VIEW_TYPE_TRIAL:
                    bindTrialViewHolder((ViewHolderIndividual) holder, subscriptionsList.get(position));
                    break;
            }
        }

        private void bindTrialViewHolder(ViewHolderIndividual holder, PlacePremium placePremium) {
            holder.labelDescription.setText(String.format(getString(R.string.trial_remaining_days), getRemainingDays()));
            holder.labelDescription.setTextColor(getColor(R.color.green));
            holder.labelPlace.setText(placePremium.getPlace().getBusinessName());
            imageLoaderRead.getImage(getApplicationContext(), placePremium.getPlace(), new ImageLoaderRead.OnGetImageListener() {
                @Override
                public void onSuccess(Bitmap bitmap) {
                    holder.imageView.setImageBitmap(bitmap);
                }

                @Override
                public void onFailure() {

                }
            });

            holder.cardView.setOnClickListener(new OnSubscriptionClickListener(placePremium));
            holder.cardViewValid.setVisibility(View.GONE);
        }

        private void bindIndividualViewHolder(ViewHolderIndividual holder, PlacePremium placePremium) {
            holder.labelDescription.setText(placePremium.getTemplate().getDescription());
            holder.labelDescription.setTextColor(getColor(R.color.black));
            holder.labelPlace.setText(placePremium.getPlace().getBusinessName());
            imageLoaderRead.getImage(getApplicationContext(), placePremium.getPlace(), new ImageLoaderRead.OnGetImageListener() {
                @Override
                public void onSuccess(Bitmap bitmap) {
                    holder.imageView.setImageBitmap(bitmap);
                }

                @Override
                public void onFailure() {

                }
            });

            holder.cardView.setOnClickListener(new OnSubscriptionClickListener(placePremium));
            holder.cardViewValid.setVisibility(View.GONE);
        }

        private void bindComboViewHolder(ViewHolderCombo holder, PlacePremium placePremium) {
            holder.labelDescription.setText(placePremium.getTemplate().getDescription());
            holder.labelPlace.setText(placePremium.getPlace().getBusinessName());
            holder.labelDescription.setTextColor(getColor(R.color.black));
            imageLoaderRead.getImage(getApplicationContext(), placePremium.getPlace(), new ImageLoaderRead.OnGetImageListener() {
                @Override
                public void onSuccess(Bitmap bitmap) {
                    holder.imageView.setImageBitmap(bitmap);
                }

                @Override
                public void onFailure() {

                }
            });


            StringBuilder employeeNames = new StringBuilder();
            for (Job job : placePremium.getPlace().getJobList()) {
                employeeNames.append(job.getEmployee().getName()).append(", ");
            }

            holder.labelEmployees.setText(employeeNames.substring(0, employeeNames.length() - 2));

            holder.cardView.setOnClickListener(new OnSubscriptionClickListener(placePremium));
            holder.cardViewValid.setVisibility(View.GONE);
        }

        @Override
        public int getItemViewType(int position) {
            if (hasTrial) {
                return VIEW_TYPE_TRIAL;
            }
            return subscriptionsList.get(position).getTemplate().isCombo() ? VIEW_TYPE_COMBO : VIEW_TYPE_INDIVIDUAL;
        }

        @Override
        public int getItemCount() {
            return subscriptionsList.size();
        }

        private class ViewHolderCombo extends RecyclerView.ViewHolder {

            TextView labelPlace, labelDescription, labelEmployees;
            CircleImageView imageView;
            CardView cardView, cardViewValid;

            public ViewHolderCombo(@NonNull View itemView) {
                super(itemView);
                this.cardViewValid = itemView.findViewById(R.id.labelValid);
                this.cardView = itemView.findViewById(R.id.cardViewHolder);
                this.imageView = itemView.findViewById(R.id.imageView);
                this.labelDescription = itemView.findViewById(R.id.labelComboDescription);
                this.labelPlace = itemView.findViewById(R.id.labelPlace);
                this.labelEmployees = itemView.findViewById(R.id.labelEmployees);
            }
        }

        private class ViewHolderIndividual extends RecyclerView.ViewHolder {

            TextView labelPlace, labelDescription;
            CircleImageView imageView;
            CardView cardView, cardViewValid;

            public ViewHolderIndividual(@NonNull View itemView) {
                super(itemView);
                this.cardViewValid = itemView.findViewById(R.id.labelValid);
                this.cardView = itemView.findViewById(R.id.cardViewHolder);
                this.imageView = itemView.findViewById(R.id.imageView);
                this.labelDescription = itemView.findViewById(R.id.labelComboDescription);
                this.labelPlace = itemView.findViewById(R.id.labelPlace);
            }
        }
    }
}