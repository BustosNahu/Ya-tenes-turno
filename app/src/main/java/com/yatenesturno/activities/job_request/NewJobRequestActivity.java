package com.yatenesturno.activities.job_request;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.yatenesturno.Constants;
import com.yatenesturno.R;
import com.yatenesturno.activities.get_premium.GetPremiumActivity;
import com.yatenesturno.activities.main_screen.MainActivity;
import com.yatenesturno.custom_views.LoadingOverlay;
import com.yatenesturno.database.djangoImpl.DatabaseDjangoRead;
import com.yatenesturno.database.djangoImpl.DatabaseDjangoWrite;
import com.yatenesturno.listeners.DatabaseCallback;
import com.yatenesturno.object_interfaces.Place;
import com.yatenesturno.object_views.ViewPlace;
import com.yatenesturno.serializers.BuilderListPlace;
import com.yatenesturno.user_auth.UserManagement;
import com.yatenesturno.utils.CustomAlertDialogBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;
import eu.davidea.flexibleadapter.FlexibleAdapter;

public class NewJobRequestActivity extends AppCompatActivity {

    public static final String PLACE_ID = "placeId";

    private FlexibleAdapter<ViewPlace> adapter;
    private LoadingOverlay loadingOverlay;
    private boolean isRunning;
    private boolean fromFirstShop;
    Intent intent;

    private String placeId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_job_request);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(getDrawable(R.color.white));
        getSupportActionBar().setElevation(0);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        loadingOverlay = new LoadingOverlay(findViewById(R.id.searchCoordinator));
        fromFirstShop = getIntent().getBooleanExtra("fromFirstShop", false);
        intent = new Intent(this, MainActivity.class);


        initUI();

        //SACAR TOAST
        if (savedInstanceState == null) {
            initUI();
            Toast.makeText(this, "Estoy NULL", Toast.LENGTH_SHORT).show();
        } else {
            recoverState(savedInstanceState);
            Toast.makeText(this, "Estoy RECOVERSTATE", Toast.LENGTH_SHORT).show();
        }
    }

    private void initUI() {
        setTitle("");
        SearchView searchView = findViewById(R.id.searchViewNewJobRequest);
        RecyclerView recyclerView = findViewById(R.id.recyclerViewNewJobRequest);

        searchView.setOnQueryTextListener(new QueryTextListenerNewJobRequest());

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        this.adapter = new FlexibleAdapter<>(null);
        adapter.mItemClickListener = new ItemClickListenerSearchPlace();
        recyclerView.setAdapter(adapter);

        placeId = getIntent().getStringExtra("placeId");
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putAll(saveState());
    }

    private Bundle saveState() {
        Bundle bundle = new Bundle();
        bundle.putString(PLACE_ID, placeId);
        return bundle;
    }

    private void recoverState(Bundle bundle){
        placeId = bundle.getString(PLACE_ID);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void searchPlace(String newText) {

        HashMap<String, String> body = new HashMap<>();
        body.put("searchquery", newText);

        showLoadingOverlay();
        DatabaseDjangoRead.getInstance().GET(
                Constants.DJANGO_URL_SEARCH_PLACE,
                body,
                new CallbackGetSearchPlaces()
        );
    }

    /**
     * this method is the final action which users makes to send a job request, when you click on ENVIAR, it's validates if you are premium or not, if not,
     * it show GetPremiumActivity screen
     * @param position
     */
    private void showConfirmDialog(int position) {
        final ViewPlace vp = adapter.getItem(position);

        new CustomAlertDialogBuilder(this)
                .setTitle(getString(R.string.confirm_job_request))
                .setMessage(vp.getPlace().getBusinessName())

                .setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(R.string.send, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(GetPremiumActivity.hasPremiumInPlaceOrShowScreen(NewJobRequestActivity.this, placeId, UserManagement.getInstance().getUser().getId())) {
                            sendJobRequest(vp);
                            dialog.dismiss();
                            showLoadingOverlay();
                        }
                    }
                })
                .show();
    }

    public void showLoadingOverlay() {
        if (loadingOverlay != null) {
            loadingOverlay.show();
        }
    }

    private void sendJobRequest(ViewPlace vp) {
        Map<String, String> body = buildRequestBody(vp);

        DatabaseDjangoWrite.getInstance().POST(
                Constants.DJANGO_URL_NEW_JOB_REQUEST,
                body,
                new CallbackNewJobRequest()
        );
    }

    private Map<String, String> buildRequestBody(ViewPlace vp) {
        Map<String, String> body = new HashMap<>();
        body.put("place_id", vp.getPlace().getId());
        return body;
    }

    private void checkResults() {
        if (adapter.getCurrentItems().size() == 0) {
            showMessage("No se encontraron resultados");
        }
    }

    public void populatePlaces(JSONObject response) throws JSONException {
        List<Place> aux = new BuilderListPlace().build(response);

        adapter.clear();
        for (Place p : aux) {
            adapter.addItem(new ViewPlace(p));
        }
        adapter.notifyDataSetChanged();
        checkResults();
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

    public void showConnectionError() {
        Toast.makeText(getApplicationContext(), getString(R.string.error_de_conexion), Toast.LENGTH_SHORT).show();
    }

    public void onPlacesFetch(JSONObject response) throws JSONException {
        hideLoadingOverlay();

        if (isRunning) {
            populatePlaces(response);
        }
    }

    public void hideLoadingOverlay() {
        if (loadingOverlay != null) {
            loadingOverlay.hide();
        }
    }

    public void showMessage(String s) {
        Snackbar.make(findViewById(R.id.root), s, Snackbar.LENGTH_SHORT).show();
    }

    private class CallbackNewJobRequest extends DatabaseCallback {
        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
            hideLoadingOverlay();
            showMessage("Solicitud enviada");
            Log.d("FromFirstShop", String.valueOf(fromFirstShop));
            if (fromFirstShop){
                startActivity(intent);
            }
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
            hideLoadingOverlay();
            showConnectionError();
        }
    }

    private class QueryTextListenerNewJobRequest implements SearchView.OnQueryTextListener {

        @Override
        public boolean onQueryTextSubmit(String newText) {
            searchPlace(newText);
            return true;
        }

        @Override
        public boolean onQueryTextChange(final String newText) {
            if (newText.equals("")) {
                adapter.clear();
                adapter.notifyDataSetChanged();
            }

            return true;
        }
    }

    private class CallbackGetSearchPlaces extends DatabaseCallback {

        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
            try {
                onPlacesFetch(response);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
            hideLoadingOverlay();
            showConnectionError();
        }
    }

    private class ItemClickListenerSearchPlace implements FlexibleAdapter.OnItemClickListener {
        @Override
        public boolean onItemClick(View view, int position) {
            showConfirmDialog(position);
            return true;
        }
    }
}