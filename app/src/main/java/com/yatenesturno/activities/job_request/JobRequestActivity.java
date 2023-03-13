package com.yatenesturno.activities.job_request;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yatenesturno.Constants;
import com.yatenesturno.R;
import com.yatenesturno.activities.tutorial_screen.Screen;
import com.yatenesturno.activities.tutorial_screen.ScreenImpl;
import com.yatenesturno.activities.tutorial_screen.TutorialScreenImpl;
import com.yatenesturno.custom_views.LoadingOverlay;
import com.yatenesturno.database.djangoImpl.DatabaseDjangoRead;
import com.yatenesturno.database.djangoImpl.DatabaseDjangoWrite;
import com.yatenesturno.listeners.DatabaseCallback;
import com.yatenesturno.object_interfaces.JobRequest;
import com.yatenesturno.object_views.ViewJobRequest;
import com.yatenesturno.serializers.BuilderListJobRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;
import eu.davidea.flexibleadapter.FlexibleAdapter;

public class JobRequestActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private List<ViewJobRequest> viewJobRequestList;
    private FlexibleAdapter<ViewJobRequest> adapter;

    private LoadingOverlay loadingOverlay;
    private boolean isRunning;
    private boolean fromFirstShop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_request);

        this.recyclerView = findViewById(R.id.recyclerViewJobRequests);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(getDrawable(R.color.white));
        getSupportActionBar().setElevation(0);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        setTitle("");

        loadingOverlay = new LoadingOverlay(findViewById(R.id.containerRequests));

        fromFirstShop = getIntent().getBooleanExtra("fromFirstShop", false);
        initUI();
    }

    private void initUI() {
        findViewById(R.id.noPlaces).setOnClickListener(new ClickListenerNewJobRequest());

        loadingOverlay.show();
        getJobRequests();
        initTutorial();
    }

    private void initTutorial() {

            List<Screen> screenList = Arrays.asList(
                    new ScreenImpl(R.drawable.empl1),
                    new ScreenImpl(R.drawable.empl2)
            );
            TutorialScreenImpl tutorialScreen = new TutorialScreenImpl();
            tutorialScreen.showTutorial(this, screenList, false);

    }

    private void getJobRequests() {
        this.viewJobRequestList = new ArrayList<>();
        DatabaseDjangoRead.getInstance().GET(
                Constants.DJANGO_URL_JOB_REQUEST,
                null,
                new CallbackGetJobRequests()
        );

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void acceptJobRequest(ViewJobRequest jobRequest, boolean canEdit, boolean canChat) {
        loadingOverlay.show();
        Map<String, String> body = new HashMap<>();
        body.put("place_id", jobRequest.getJobRequest().getPlace().getId());
        body.put("service_provider", jobRequest.getJobRequest().getCustomUser().getId());

        String canEditString = canEdit ? "True" : "False";
        String canChatString = canChat ? "True" : "False";

        body.put("can_edit", canEditString);
        body.put("can_chat", canChatString);

        DatabaseDjangoWrite.getInstance().POST(
                Constants.DJANGO_URL_ACCEPT_JOB_REQUEST,
                body,
                new CallbackDecidedJobRequest(jobRequest)
        );
    }

    public void removeJobRequest(ViewJobRequest jobRequest) {
        loadingOverlay.show();
        Map<String, String> body = new HashMap<>();
        body.put("place_id", jobRequest.getJobRequest().getPlace().getId());
        body.put("service_provider", jobRequest.getJobRequest().getCustomUser().getId());

        DatabaseDjangoWrite.getInstance().POST(
                Constants.DJANGO_URL_CANCEL_JOB_REQUEST,
                body,
                new CallbackDecidedJobRequest(jobRequest)
        );
    }

    private void populateJobRequests(JSONObject response) {
        try {
            List<JobRequest> aux = new BuilderListJobRequest().build(response);

            if (aux.size() > 0) {
                displayHasJobRequests();
                for (JobRequest jobRequest : aux) {
                    viewJobRequestList.add(new ViewJobRequest(this, jobRequest));
                }
                populateRecyclerView();
            } else {
                displayNoJobRequests();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void populateRecyclerView() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
                recyclerView.setLayoutManager(layoutManager);
                adapter = new FlexibleAdapter<>(viewJobRequestList);
                recyclerView.setAdapter(adapter);
            }
        });
    }

    private void displayHasJobRequests() {
        findViewById(R.id.labelNoJobRequests).setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    private void displayNoJobRequests() {
        findViewById(R.id.labelNoJobRequests).setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
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

    private class CallbackDecidedJobRequest extends DatabaseCallback {

        private final ViewJobRequest viewJobRequest;

        public CallbackDecidedJobRequest(ViewJobRequest viewJobRequest) {
            this.viewJobRequest = viewJobRequest;
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
            loadingOverlay.hide();
            viewJobRequestList.remove(viewJobRequest);
            adapter.updateDataSet(viewJobRequestList, true);

            if (viewJobRequestList.size() == 0) {
                displayNoJobRequests();
            }

            setResult(RESULT_OK);
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

            loadingOverlay.hide();
            Toast.makeText(getApplicationContext(), getString(R.string.error_de_conexion), Toast.LENGTH_SHORT).show();
            setResult(RESULT_CANCELED);
        }
    }

    private class CallbackGetJobRequests extends DatabaseCallback {
        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
            if (isRunning) {
                loadingOverlay.hide();
                populateJobRequests(response);
            }
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
            loadingOverlay.hide();
            Toast.makeText(getApplicationContext(), getString(R.string.error_de_conexion), Toast.LENGTH_SHORT).show();
        }
    }

    private class ClickListenerNewJobRequest implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getApplicationContext(), NewJobRequestActivity.class);
            intent.putExtra("fromFirstShop", fromFirstShop);
            startActivity(intent);
        }
    }
}