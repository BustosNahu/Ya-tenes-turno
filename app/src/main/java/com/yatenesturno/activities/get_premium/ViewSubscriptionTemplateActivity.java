package com.yatenesturno.activities.get_premium;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.cardview.widget.CardView;

import com.yatenesturno.Constants;
import com.yatenesturno.R;
import com.yatenesturno.custom_views.LoadingOverlay;
import com.yatenesturno.database.djangoImpl.DatabaseDjangoWrite;
import com.yatenesturno.functionality.PlacePremiumManager;
import com.yatenesturno.listeners.DatabaseCallback;
import com.yatenesturno.object_interfaces.Job;
import com.yatenesturno.object_interfaces.SubscriptionToken;
import com.yatenesturno.utils.CustomAlertDialogBuilder;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import cz.msebera.android.httpclient.Header;

public class ViewSubscriptionTemplateActivity extends AppCompatActivity {

    private final ActivityResultLauncher<Intent> subscribeLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    PlacePremiumManager.getInstance().refresh(null);
                    setResult(RESULT_OK);
                    finish();
                }
            });

    private SubscriptionToken subscriptionToken;
    private AppCompatTextView template, userList, place;
    private AppCompatButton btnAction;
    private CardView labelValid;
    private LoadingOverlay loadingOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_subscription_template);

        getSupportActionBar().setBackgroundDrawable(getDrawable(R.color.black));
        getSupportActionBar().setElevation(0);
        getSupportActionBar().setTitle("Suscripciones");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            subscriptionToken = getIntent().getExtras().getParcelable("subscriptionToken");
        } else {
            recoverState(savedInstanceState);
        }

        initUI();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initUI() {
        template = findViewById(R.id.template);
        place = findViewById(R.id.place);
        userList = findViewById(R.id.userList);
        btnAction = findViewById(R.id.actionBtn);
        labelValid = findViewById(R.id.labelValid);
        loadingOverlay = new LoadingOverlay(findViewById(R.id.root));

        loadingOverlay.show();
        fillTemplate();
        if (subscriptionToken.isValid()) {
            initValidSubscription();
        } else {
            initNotValidSubscription();
        }
    }

    private void fillTemplate() {
        template.setText(subscriptionToken.getTemplate().getDescription());
        place.setText(subscriptionToken.getPlace().getBusinessName());
        StringBuilder employeeNames = new StringBuilder();

        if (subscriptionToken.getTemplate().isCombo()) {
            for (Job job : subscriptionToken.getPlace().getJobList()) {
                employeeNames.append(job.getEmployee().getName()).append("\n");
            }
        } else {
            employeeNames.append(subscriptionToken.getUser().getName());
        }

        userList.setText(employeeNames);
        loadingOverlay.hide();
    }

    private void initValidSubscription() {
        btnAction.setOnClickListener(view -> showConfirmUnsubscribe());
        labelValid.setVisibility(View.VISIBLE);
    }

    private void showConfirmUnsubscribe() {
        new CustomAlertDialogBuilder(this)
                .setTitle("¿Confirma que desea cancelar su suscripción?")
                .setPositiveButton(R.string.confirm, (dialogInterface, i) -> unsubscribe())
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void unsubscribe() {
        loadingOverlay.show();
        Map<String, String> body = new HashMap<>();
        body.put("place_id", subscriptionToken.getPlace().getId());
        body.put("user_id", subscriptionToken.getUser().getId());

        DatabaseDjangoWrite.getInstance().POST(
                Constants.DJANGO_URL_UNSUBSCRIBE,
                body,
                new DatabaseCallback() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        PlacePremiumManager.getInstance().refresh(null);
                        loadingOverlay.hide();
                        setResult(RESULT_OK);
                        finish();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

                    }
                }
        );
    }

    private void initNotValidSubscription() {
        btnAction.setBackgroundColor(getColor(R.color.colorPrimary));
        btnAction.setTextColor(getColor(R.color.white));
        btnAction.setText("Obtener Premium");
        btnAction.setOnClickListener(view -> checkWarning());
        labelValid.setVisibility(View.GONE);
    }

    private void checkWarning() {
        if (subscriptionToken.hasWarning()) {
            showWarningDialog();
        } else {
            startPurchaseFlow();
        }
    }

    private void showWarningDialog() {
        new CustomAlertDialogBuilder(this)
                .setTitle("Tienes un empleado con una suscripción activa. Sugerimos la cancelación de la misma antes de continuar")
                .setPositiveButton(R.string.continue_anyway, (dialogInterface, i) -> startPurchaseFlow())
                .show();
    }

    private void startPurchaseFlow() {
        Intent intent = new Intent(this, SubscriptionWebViewActivity.class);

        Bundle bundle = new Bundle();
        bundle.putParcelable("token", subscriptionToken);
        intent.putExtras(bundle);

        subscribeLauncher.launch(intent);
    }

    private void recoverState(Bundle savedInstanceState) {
        subscriptionToken = savedInstanceState.getParcelable("subscriptionToken");
    }


}