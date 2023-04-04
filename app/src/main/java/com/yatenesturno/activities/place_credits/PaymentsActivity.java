package com.yatenesturno.activities.place_credits;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.yatenesturno.R;
import com.yatenesturno.activities.employee.EmployeeActivity;
import com.yatenesturno.object_interfaces.Job;
import com.yatenesturno.object_interfaces.Place;

public class PaymentsActivity extends AppCompatActivity {

    /**
     * Argument variables keys
     */
    public static final String ARG_PLACE = "place";

    /**
     * Activity employee launcher
     */
    private final ActivityResultLauncher<Intent> employeeActivityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {

            });

    /**
     * UI References
     */
    private CardView btnViewPlaceCredits;

    /**
     * Instance variables
     */
    private Place place;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payments);

        if (savedInstanceState != null) {
            recoverState(savedInstanceState);
        } else {
            recoverState(getIntent().getExtras());
        }

        initViews();
    }

    private void initViews() {
        setUpNavBar();

        btnViewPlaceCredits = findViewById(R.id.btnViewPlaceCredits);

        btnViewPlaceCredits.setOnClickListener(view -> startPlaceCreditsActivity());
    }

    private void setUpNavBar() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(getDrawable(R.color.white));
        getSupportActionBar().setTitle("Mis Cobros");
        getSupportActionBar().setElevation(0);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startPlaceCreditsActivity() {
        Intent intent = new Intent(this, PlaceClientCreditsActivity.class);
        Bundle bundle = new Bundle();

        bundle.putParcelable(ARG_PLACE, place);

        intent.putExtras(bundle);
        employeeActivityLauncher.launch(intent);
    }

    private void recoverState(Bundle extras) {
        place = extras.getParcelable(ARG_PLACE);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(ARG_PLACE, place);
    }
}