package com.yatenesturno.activities.get_premium;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.yatenesturno.Constants;
import com.yatenesturno.R;
import com.yatenesturno.custom_views.LoadingOverlay;
import com.yatenesturno.database.djangoImpl.DatabaseDjangoRead;
import com.yatenesturno.database.djangoImpl.DatabaseDjangoWrite;
import com.yatenesturno.listeners.DatabaseCallback;
import com.yatenesturno.object_interfaces.SubscriptionToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import cz.msebera.android.httpclient.Header;

public class SubscriptionWebViewActivity extends AppCompatActivity {

    private WebView webView;
    private LoadingOverlay loadingOverlay;
    private SubscriptionToken token;
    private boolean approved;
    private String mpId;

    private static Map<String, String> parseUrlParams(String url) {
        String[] params = url.split("&");
        Map<String, String> paramsMap = new HashMap<>();
        for (String param : params) {
            if (param.contains("=")) {
                String[] paramArray = param.split("=");
                paramsMap.put(paramArray[0], paramArray[1]);
            }
        }

        return paramsMap;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription_web_view);

        getSupportActionBar().setBackgroundDrawable(getDrawable(R.color.black));
        getSupportActionBar().setElevation(0);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        token = getIntent().getExtras().getParcelable("token");

        new Handler(Looper.myLooper()).postDelayed(this::init, 100);
    }

    private void init() {
        initViews();
        validateTemplate();
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
        webView = findViewById(R.id.webViewSubscription);
        loadingOverlay = new LoadingOverlay(findViewById(R.id.root));
    }

    private void validateTemplate() {
        loadingOverlay.show();

        Map<String, String> body = getBody();

        DatabaseDjangoRead.getInstance().GET(
                Constants.DJANGO_URL_ACCEPT_VALIDATE_SUB_TOKEN,
                body,
                new DatabaseCallback() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        loadingOverlay.hide();
                        try {
                            displayWebView(response.getString("url"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        loadingOverlay.hide();
                        onValidationFailure();
                    }
                }
        );
    }

    private Map<String, String> getBody() {
        Map<String, String> out = new HashMap<>();

        out.put("place_id", token.getPlace().getId());
        out.put("template_id", token.getTemplate().getId());
        out.put("token", token.getId());

        return out;
    }

    public void displayWebView(String url) {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                Log.e("WebView Log", consoleMessage.message());
                return true;
            }
        });
        WebSettings settings = webView.getSettings();
        settings.setDomStorageEnabled(true);
        webView.loadUrl(url);
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                loadingOverlay.show();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                loadingOverlay.hide();
            }

            @Nullable
            @org.jetbrains.annotations.Nullable
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                Log.e("REQUEST: ", request.getUrl().toString());
                return super.shouldInterceptRequest(view, request);
            }

            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                view.loadUrl(url);
                Log.e("REQ should ov", url);
                if (isApprovedRedirectUrl(url) && !approved) {
//                    mpId = getMpId(url);
                    loadingOverlay.show();
                    view.setWebViewClient(new WebViewClient() {
                        @Override
                        public void onPageFinished(WebView view, String url) {
                            hideBtnGoBack();
                            new Handler(Looper.myLooper()).postDelayed(SubscriptionWebViewActivity.this::onSubscriptionApproved, 300);
                        }
                    });
                }
                return false;
            }

        });
    }

    public String getMpId(String url) {
        return parseUrlParams(url).get("external_reference");
    }

    public boolean isApprovedRedirectUrl(String url) {
        return url.startsWith("https://www.mercadopago.com.ar/subscriptions/checkout/congrats")
                && url.contains("status=approved");
    }

    private void onSubscriptionApproved() {
        if (!approved) {
            approved = true;
            showSuccessScreen();
            postSubscriptionToBackend();
        }
    }

    private void showSuccessScreen() {
        webView.setVisibility(View.GONE);
        findViewById(R.id.containerSuccess).setVisibility(View.VISIBLE);
        findViewById(R.id.btnContinue).setOnClickListener(view -> restartApp());
    }

    private void restartApp() {
        Intent i = getBaseContext().getPackageManager()
                .getLaunchIntentForPackage(getBaseContext().getPackageName());
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        finish();
        startActivity(i);
    }

    @Override
    public void onBackPressed() {
        if (approved) {
            return;
        }
        super.onBackPressed();
    }

    private void postSubscriptionToBackend() {
        Map<String, String> body = new HashMap<>();

        body.put("token", token.getId());
        token.getTemplate().getId();

//        body.put("mp_id", mpId);

        DatabaseDjangoWrite.getInstance().POST(
                Constants.DJANGO_URL_ON_SUBSCRIPTION_APPROVED,
                body,
                new DatabaseCallback() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        loadingOverlay.hide();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        returnOK();
                    }
                }
        );
    }

    private void hideBtnGoBack() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

    }

    public void returnOK() {
        setResult(RESULT_OK);
        finish();
    }

    private void onValidationFailure() {
        Snackbar.make(webView.getRootView(), R.string.sub_token_validation_error, Snackbar.LENGTH_SHORT).show();
    }
}