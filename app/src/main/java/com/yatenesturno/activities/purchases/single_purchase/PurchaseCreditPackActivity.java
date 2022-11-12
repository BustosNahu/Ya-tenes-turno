package com.yatenesturno.activities.purchases.single_purchase;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.yatenesturno.Constants;
import com.yatenesturno.R;
import com.yatenesturno.custom_views.LoadingOverlay;
import com.yatenesturno.database.DatabaseDjangoWrite;
import com.yatenesturno.listeners.DatabaseCallback;
import com.yatenesturno.object_interfaces.Place;
import com.yatenesturno.object_interfaces.PlacePremium;

import org.json.JSONObject;

import java.util.HashMap;

import cz.msebera.android.httpclient.Header;

public class PurchaseCreditPackActivity extends AppCompatActivity {

    private CreditPack creditPack;
    private WebView webView;
    private LoadingOverlay loadingOverlay;
    private Place place;
    private String jobId;
    private boolean approved;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_purchase_credit_pack);

        getSupportActionBar().setBackgroundDrawable(getDrawable(R.color.black));
        getSupportActionBar().setElevation(0);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        approved = false;

        if (savedInstanceState == null) {
            recoverState(getIntent().getExtras());
        } else {
            recoverState(savedInstanceState);
        }

        new Handler(Looper.myLooper()).postDelayed(this::init, 100);
    }

    private void recoverState(Bundle savedInstanceState) {
        creditPack = savedInstanceState.getParcelable("creditPack");
        place = savedInstanceState.getParcelable("place");
        jobId = savedInstanceState.getString("jobId");
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("creditPack", creditPack);
        outState.putParcelable("place", place);
        outState.putString("jobId", jobId);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void init() {
        webView = findViewById(R.id.webViewSubscription);
        loadingOverlay = new LoadingOverlay(findViewById(R.id.root));

        displayWebView();
    }

    public void displayWebView() {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.loadUrl(creditPack.getMercadoPagoUrl());
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                loadingOverlay.show();
            }

            @Nullable
            @org.jetbrains.annotations.Nullable
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                if (isApprovedRedirectUrl(request.getUrl().toString()) && !approved) {
                    approved = true;
                    runOnUiThread(() -> {
                        hideBtnGoBack();
                        new Handler(Looper.myLooper()).postDelayed(PurchaseCreditPackActivity.this::onPaymentSuccessful, 300);
                    });
                }
                return super.shouldInterceptRequest(view, request);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                loadingOverlay.hide();
            }

        });
    }

    private void hideBtnGoBack() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }

    @Override
    public void onBackPressed() {
        if (approved) {
            return;
        }
        super.onBackPressed();
    }

    private void onPaymentSuccessful() {
        HashMap<String, String> body = new HashMap<>();

        body.put("whatsapp_credits", String.valueOf(creditPack.getWhatsappCredits()));
        body.put("email_credits", String.valueOf(creditPack.getEmailCredits()));

        DatabaseDjangoWrite.getInstance().POST(
                getCreditsUrl(),
                body,
                new DatabaseCallback() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        showSuccessScreen();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

                    }
                }
        );
    }

    private String getCreditsUrl() {
        return String.format(Constants.DJANGO_URL_CREDITS, place.getId(), jobId);
    }

    private void showSuccessScreen() {
        webView.setVisibility(View.GONE);
        findViewById(R.id.containerSuccess).setVisibility(View.VISIBLE);
        findViewById(R.id.btnContinue).setOnClickListener(view -> returnOK());
    }

    public void returnOK() {
        Intent intent = new Intent();
        intent.putExtra("creditPack", creditPack);
        setResult(RESULT_OK, intent);
        finish();
    }

    public boolean isApprovedRedirectUrl(String url) {
        return url.contains("https://www.mercadopago.com.ar/checkout/v1/payment/redirect/")
                && url.contains("/congrats/approved/");
    }
}