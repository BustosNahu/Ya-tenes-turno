package com.yatenesturno.activities.place_register;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.yatenesturno.Constants;
import com.yatenesturno.Flags;
import com.yatenesturno.R;
import com.yatenesturno.activities.ObjectConfiguratorCoordinator;
import com.yatenesturno.activities.main_screen.MainActivity;
import com.yatenesturno.activities.place_register.step3.NewPlaceIntroStep3;
import com.yatenesturno.activities.place_register.step3.NewPlaceStep3;
import com.yatenesturno.activities.place_register.step_1.NewPlaceIntroStep1;
import com.yatenesturno.activities.place_register.step_1.NewPlaceStep1;
import com.yatenesturno.activities.place_register.step_2.ConfirmScheduledHours;
import com.yatenesturno.activities.place_register.step_2.NewPlaceIntroStep2;
import com.yatenesturno.activities.place_register.step_2.NewPlaceStep2;
import com.yatenesturno.custom_views.LoadingOverlay;
import com.yatenesturno.database.djangoImpl.DatabaseDjangoWrite;
import com.yatenesturno.database.ImageLoaderWriteImpl;
import com.yatenesturno.listeners.DatabaseCallback;
import com.yatenesturno.object_interfaces.JobType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;


public class NewPlaceActivity extends AppCompatActivity {


    public Bundle step1Bundle;
    public Bundle bundle;
    public Bundle step3Bundle;
    public Bundle step2Bundle;
    private String placeId;
    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

    public Boolean savedBundle = false;

    LoadingOverlay loadingOverlay;
    private boolean isRunning;
     public boolean isLoading;

    private View view;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_new_place);

        initViews();

        if (savedInstanceState != null) {
            recoverState(savedInstanceState);

            startUI();
        } else {
            fragmentTransaction.replace(R.id.new_place_fragment_main, new NewPlaceIntroStep1()).commit();
        }
    }



    private void recoverState(Bundle bundle) {

        int fragmentList = bundle.getInt("currentItem", 0);

        //TODO save it state in cada fragment
        switch (fragmentList) {
            case 0: {
                fragmentTransaction.replace(R.id.new_place_fragment_main, new NewPlaceIntroStep1());
            }
            case 1: {
                fragmentTransaction.replace(R.id.new_place_fragment_main, new NewPlaceStep1());
            }
            case 2: {
                fragmentTransaction.replace(R.id.new_place_fragment_main, new NewPlaceIntroStep2());
            }
            case 3: {
                fragmentTransaction.replace(R.id.new_place_fragment_main, new NewPlaceStep2());
            }
            case 4: {
                fragmentTransaction.replace(R.id.new_place_fragment_main, new ConfirmScheduledHours());
            }
            case 5: {
                fragmentTransaction.replace(R.id.new_place_fragment_main, new NewPlaceIntroStep3());
            }
            case 6: {
                fragmentTransaction.replace(R.id.new_place_fragment_main, new NewPlaceStep3());
            }
        }
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

    private void initViews() {
        loadingOverlay = new LoadingOverlay(findViewById(R.id.new_place_root));
        showLoadingOverlay();
    }

    @Override
    protected void onStart() {
        super.onStart();

        initUI();
    }

    private void initUI() {

        new Handler(Looper.myLooper()).postDelayed(this::startUI, 200);
    }

    public void updateFragments() {
        int fragmentList = bundle.getInt("currentItem", 0);

        switch (fragmentList) {
            case 0: {
                fragmentTransaction.replace(R.id.new_place_fragment_main, new NewPlaceIntroStep1()).commit();
            }
            case 1: {
                fragmentTransaction.replace(R.id.new_place_fragment_main, new NewPlaceStep1()).commit();
            }
            case 2: {
                fragmentTransaction.replace(R.id.new_place_fragment_main, new NewPlaceIntroStep2()).commit();
            }
            case 3: {
                fragmentTransaction.replace(R.id.new_place_fragment_main, new NewPlaceStep2()).commit();
            }
            //TODO
        }
    }


    public void startUI() {
        hideLoadingOverlay();
        if (isRunning) {

        }
    }


    private void showLoadingOverlay() {
        if (loadingOverlay != null) {
            loadingOverlay.show();
        }
    }

    public void hideLoadingOverlay() {
        if (loadingOverlay != null) {
            loadingOverlay.hide();
        }
    }


    public void saveBundleFromStep1(Bundle fragmentBundle) {
        step1Bundle = fragmentBundle;
        savedBundle = true;
    }

    public void saveBundleFromStep2(Bundle fragmentBundle) {
        step2Bundle = fragmentBundle;
        savedBundle = true;
    }
    public void saveBundleFromStep3(Bundle fragmentBundle) {
        step3Bundle = fragmentBundle;
        savedBundle = true;
    }

    public Bundle returnBundleFromActToFrag() {
        return bundle;
    }

    public Bundle returnBundleFromActToFragStep1() {
        return step1Bundle;
    }
    public Bundle returnBundleFromActToFragStep2() {
        return step2Bundle;
    }


    private void returnCancel() {
        setResult(Activity.RESULT_CANCELED);
        finish();
    }
    Button back;

    public void onButtonShowPopupWindowClick() {

        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") View popupView = inflater.inflate(R.layout.finished_create_place_popup, null);
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.MATCH_PARENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        popupWindow.showAtLocation(this.findViewById(android.R.id.content).getRootView(), Gravity.CENTER, 0, 0);
        back = popupView.findViewById(R.id.existent_shop);

        popupView.setElevation(10f);
        // dismiss the popup window when touched
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(Activity.RESULT_OK);
                Intent i = new Intent(NewPlaceActivity.this, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                //i.putExtra("placeId",placeId);
                startActivityForResult(i,Constants.RC_NEW_PLACE);
                overridePendingTransition(R.anim.slide_up_in,R.anim.slide_down_out);
                finish();
            }
        });

    }

    private void returnOK() {
        onButtonShowPopupWindowClick();
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Fragment newPlaceLocation = new NewPlaceLocationFragment();
        newPlaceLocation.onRequestPermissionsResult(requestCode, permissions, grantResults);
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
        createNewPlace();
    }

    public  void createNewPlace() {
        isLoading = true;
        showLoadingOverlay();
        Map<String, String> body = new HashMap<>();

        body.put("address", step1Bundle.getString("address"));
        body.put("slogan", step1Bundle.getString("etSlogan"));

        if (!TextUtils.isEmpty(step1Bundle.getString("info"))) {
            body.put("info", step1Bundle.getString("info"));
        }

        body.put("businessname", step1Bundle.getString("etShopName"));
        body.put("phonenumber", step1Bundle.getString("etPhone"));
        body.put("works_here", String.valueOf(step3Bundle.getBoolean("worksHere")));

        Log.d("bodyToWriteDb", body.toString());

        DatabaseDjangoWrite.getInstance().POST(
                Constants.DJANGO_URL_NEW_PLACE,
                body,
                new CallbackNewPlace()
        );
    }

    private void createPlaceDoes(String placeId) {
        JSONObject json = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        List<JobType> jobtypes = (List<JobType>) step1Bundle.getSerializable("jobTypeList");


        for (JobType type : jobtypes) {
            jsonArray.put(type.getId());
        }

        try {
            json.put("jobtypes", jsonArray);
            json.put("place", placeId);

            DatabaseDjangoWrite.getInstance().POSTJSON(
                    Constants.DJANGO_URL_PLACE_DOES,
                    json,
                    new CallbackPlaceDoes()
            );

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void hideKeyboard() {
        View view = findViewById(R.id.root).getRootView();
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void saveImage() {
        if (Flags.SAVE_IMAGES) {
            Uri uri = Uri.parse(step1Bundle.getString("uri"));
            new ImageLoaderWriteImpl()
                    .writeImageToRemote(this,
                            placeId,
                            uri,
                            this::returnOK);
        } else {
            returnOK();
            isLoading = false;
        }
    }

    public void showConnectionError() {
        Toast.makeText(getApplicationContext(), getString(R.string.error_de_conexion), Toast.LENGTH_SHORT).show();
    }




    private class CallbackNewPlace extends DatabaseCallback {

        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
            try {
                placeId = response.getString("place_id");
                createPlaceDoes(placeId);
            } catch (JSONException e) {
                Log.d("coonectErrorJson", e.toString());

            }
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
            showConnectionError();
            Log.d("coonectError", "status" + statusCode+ "headers" + headers+ "responseSTR" + responseString);
        }
    }






    private class CallbackPlaceDoes extends DatabaseCallback {

        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
            Log.d("ONSUCCESCALLBACKPLACEDOES", "status" + statusCode+ "headers" + headers+ "responseSTR" + response);
            hideLoadingOverlay();
            saveImage();

        }

        @Override
        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
            Log.d("ON FAILURE CALLBACK PLACE DOES", "status" + statusCode+ "headers" + headers+ "responseSTR" + responseString);
            showConnectionError();
        }
    }


}