package com.yatenesturno.activities.first_shop;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Outline;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.yatenesturno.Constants;
import com.yatenesturno.R;
import com.yatenesturno.activities.job_request.JobRequestActivity;
import com.yatenesturno.activities.main_screen.MainActivity;
import com.yatenesturno.activities.place_register.NewPlaceActivity;
import com.yatenesturno.database.DatabaseDjangoRead;
import com.yatenesturno.listeners.DatabaseCallback;
import com.yatenesturno.object_interfaces.JobRequest;
import com.yatenesturno.object_views.ViewJobRequest;
import com.yatenesturno.serializers.BuilderListJobRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class FirstShop extends AppCompatActivity {


    Button btnCreateShop;
    Button btnJoinShop;
    Button btnCreateShopPopUp;
    Boolean isRunning;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_shop);
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getColor(R.color.black));


        btnCreateShop = findViewById(R.id.existent_shop_btn);
        btnJoinShop = findViewById(R.id.register_my_shop);

        setShadows(btnCreateShop);

        btnCreateShop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onButtonShowPopupWindowClick(v);
            }
        });
        btnJoinShop.setOnClickListener(view -> startJobRequestsActivity());
        SharedPreferences sharedPreferences =  getSharedPreferences("firstShop", MODE_PRIVATE);
        sharedPreferences.edit().putBoolean("firstShop",false).apply();
    }
    private List<ViewJobRequest> viewJobRequestList;

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








    protected void startJobRequestsActivity() {
        Intent intent = new Intent(this, JobRequestActivity.class);
        intent.putExtra("fromFirstShop", true);
        startActivity(intent);
    }

    protected void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void setShadows(Button button) {
        button.setClipToOutline(true);
        button.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), 200);
            }
        });
    }


    public void onButtonShowPopupWindowClick(View view) {

        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") View popupView = inflater.inflate(R.layout.create_popup, null);

        // create the popup window
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.MATCH_PARENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window tolken
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
        btnCreateShopPopUp = popupView.findViewById(R.id.existent_shop);

        popupView.setElevation(10f);
        // dismiss the popup window when touched
        btnCreateShopPopUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(FirstShop.this, NewPlaceActivity.class);
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_right);
                startActivity(i);
            }
        });
        setShadows(btnCreateShopPopUp);

    }





}