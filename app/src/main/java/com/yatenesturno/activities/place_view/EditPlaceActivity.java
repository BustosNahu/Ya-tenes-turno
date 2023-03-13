package com.yatenesturno.activities.place_view;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.yatenesturno.Constants;
import com.yatenesturno.Flags;
import com.yatenesturno.R;
import com.yatenesturno.activities.ObjectConfigurator;
import com.yatenesturno.activities.ObjectConfiguratorCoordinator;
import com.yatenesturno.custom_views.LoadingButton;
import com.yatenesturno.custom_views.LoadingOverlay;
import com.yatenesturno.database.djangoImpl.DatabaseDjangoWrite;
import com.yatenesturno.database.interfaces.ImageLoaderWrite;
import com.yatenesturno.database.ImageLoaderWriteImpl;
import com.yatenesturno.functionality.ManagerPlace;
import com.yatenesturno.listeners.DatabaseCallback;
import com.yatenesturno.object_interfaces.JobType;
import com.yatenesturno.object_interfaces.Place;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;


public class EditPlaceActivity extends AppCompatActivity {

    private ObjectConfiguratorCoordinator coordinator;
    private Bundle bundle;
    private List<ObjectConfigurator> fragments;
    private LoadingButton btnConfirm;

    private Place place;

    private ViewPager viewPager;

    private LoadingOverlay loadingOverlay;
    private boolean changed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_place);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(getDrawable(R.color.white));
        getSupportActionBar().setElevation(0);
        getSupportActionBar().setTitle("Editar Lugar");
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        //viewPager = findViewById(R.id.view_pager);

        loadingOverlay = new LoadingOverlay(findViewById(R.id.root));
        loadingOverlay.show();

        final String placeId = getIntent().getExtras().getString("placeId");
        ManagerPlace.getInstance().getOwnedPlaces(new ManagerPlace.OnPlaceListFetchListener() {
            @Override
            public void onFetch(List<Place> placeList) {
                place = findPlace(placeList, placeId);
                initUI();
            }

            @Override
            public void onFailure() {
                Toast.makeText(getApplicationContext(), getString(R.string.error_de_conexion), Toast.LENGTH_SHORT).show();
                loadingOverlay.hide();
            }
        });
    }

    private void initUI() {
        btnConfirm.setOnClickListener(new BtnConfirmClickListener());
        btnConfirm.setMatchParent();
        setTitle("");
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            fragments = new ArrayList<>();
            //fragments.add(new NewPlaceStep1(place));
//            fragments.add(new NewPlaceContactFragment(place));
//            fragments.add(new NewPlaceJobTypeFragment(place));

            coordinator = new ObjectConfiguratorCoordinator(
                    getSupportFragmentManager(),
                    findViewById(android.R.id.content).getRootView(),
                    fragments,
                    new NewPlaceCoordinatorListener()
            );
            loadingOverlay.hide();
            updateButton();
        }, 100);

    }

    public Place findPlace(List<Place> list, String placeId) {
        for (Place p : list) {
            if (p.getId().equals(placeId)) {
                return p;
            }
        }
        return null;
    }

    private void updateButton() {
        if (coordinator.hasNext()) {
            btnConfirm.setBackgroundColor(getColor(R.color.colorPrimary));
            btnConfirm.setText(getString(R.string.next));
        } else {
            btnConfirm.setText(getString(R.string.confirm));
            btnConfirm.setBackgroundColor(getColor(R.color.green));
        }
    }

    private void returnCancel() {
        setResult(Activity.RESULT_CANCELED);
        finish();
    }

    private void returnOK() {
        setResult(Activity.RESULT_OK);
        loadingOverlay.hide();
        finish();
    }

    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() == 3) {
            hideKeyboard();
        }

        coordinator.prev();
        updateButton();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveToDatabase(Bundle bundle) {
        this.bundle = bundle;
        loadingOverlay.show();
        updatePlace();
    }

    private void updatePlace() {
        changed = false;
        JSONObject body = new JSONObject();

        try {
            if (!bundle.get("slogan").equals(place.getSlogan())) {
                body.put("slogan", bundle.getString("slogan"));
            }
            if (!bundle.get("businessname").equals(place.getBusinessName())) {
                body.put("businessname", bundle.getString("businessname"));
            }
            if (!bundle.getString("phonenumber").equals(place.getPhoneNumber())) {
                body.put("phonenumber", bundle.getString("phonenumber"));
            }
            if (!bundle.getString("info").equals(place.getCategory())) {
                body.put("info", bundle.getString("info"));
            }

            boolean changedJobTypes = false;
            for (Parcelable jobType : bundle.getParcelableArrayList("jobtypes")) {
                boolean found = false;
                for (JobType jobType1 : place.getJobTypes()) {
                    if (jobType.equals(jobType1)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    changedJobTypes = true;
                    break;
                }
            }

            if (changedJobTypes) {
                List<JobType> jobtypes = (List<JobType>) bundle.getSerializable("jobtypes");
                JSONArray typesArray = new JSONArray();
                for (int i = 0; i < jobtypes.size(); i++) {
                    typesArray.put(jobtypes.get(i).getId());
                }
                body.put("job_types", typesArray);
            }


            if (body.length() > 0) {
                changed = true;
                body.put("place_id", place.getId());
                DatabaseDjangoWrite.getInstance().POSTJSON(
                        Constants.DJANGO_URL_UPDATE_PLACE,
                        body,
                        new CallbackUpdatePlace()
                );
            } else {
                saveImage();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void saveImage() {
        if (bundle.containsKey("uri") && Flags.SAVE_IMAGES) {
            final Uri uri = Uri.parse(bundle.getString("uri"));
            new ImageLoaderWriteImpl().invalidateImage(this, place, new ImageLoaderWrite.OnInvalidateImageListener() {
                @Override
                public void onSuccess() {
                    new ImageLoaderWriteImpl()
                            .writeImageToRemote(
                                    EditPlaceActivity.this,
                                    place.getId(),
                                    uri,
                                    () -> returnOK());
                }

                @Override
                public void onFailure() {

                }
            });

        } else {
            if (changed) {
                returnOK();
            } else {
                returnCancel();
            }
        }
    }

    private void hideKeyboard() {
        View view = findViewById(R.id.root).getRootView();
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private class NewPlaceCoordinatorListener implements ObjectConfiguratorCoordinator.ListenerCoordinator {

        @Override
        public void onFinish(Bundle bundle) {
            btnConfirm.showLoading();
            btnConfirm.setEnabled(false);
            saveToDatabase(bundle);
        }

        @Override
        public void onCanceled() {
            returnCancel();
        }
    }

    private class CallbackUpdatePlace extends DatabaseCallback {

        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
            saveImage();
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
            loadingOverlay.hide();
            Toast.makeText(getApplicationContext(), getString(R.string.error_de_conexion), Toast.LENGTH_SHORT).show();
        }
    }

    private class BtnConfirmClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            btnConfirm.hideLoading();

            hideKeyboard();
            coordinator.next();
            updateButton();
        }
    }
}