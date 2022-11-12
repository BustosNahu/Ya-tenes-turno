package com.yatenesturno.activities.main_screen;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.textfield.TextInputEditText;
import com.yatenesturno.R;
import com.yatenesturno.custom_views.LoadingOverlay;
import com.yatenesturno.functionality.ManagerPlace;
import com.yatenesturno.notification.MessageSender;
import com.yatenesturno.object_interfaces.CustomUser;
import com.yatenesturno.object_interfaces.Place;

import java.util.List;

public class SendMessageActivity extends AppCompatActivity {

    private CustomUser client;
    private TextInputEditText editText;
    private String placeId;

    private LoadingOverlay loadingOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(getDrawable(R.color.white));
        getSupportActionBar().setElevation(0);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        setTitle("");
        loadingOverlay = new LoadingOverlay(findViewById(R.id.root));

        client = (CustomUser) getIntent().getExtras().getSerializable("client");
        placeId = getIntent().getExtras().getString("placeId");

        new Handler(Looper.myLooper()).postDelayed(() -> ManagerPlace.getInstance().getPlaces(new ManagerPlace.OnPlaceListFetchListener() {
            @Override
            public void onFetch(List<Place> placeList) {
                initUI();
            }

            @Override
            public void onFailure() {
                Toast.makeText(getApplicationContext(), getString(R.string.error_de_conexion), Toast.LENGTH_SHORT).show();
                loadingOverlay.hide();
            }
        }), 100);

    }

    private void initUI() {
        editText = findViewById(R.id.etMessage);
        ((TextView) findViewById(R.id.labelSendMessage)).setText("Enviando mensaje a " + client.getName());
        findViewById(R.id.btnAttachLink).setOnClickListener(v -> attachLink());
        findViewById(R.id.btnSend).setOnClickListener(v -> sendMessage());
    }

    private void attachLink() {
        String currentText = editText.getText().toString();

        String url = "https://www.yatenesturno.com.ar/web/place/?id=" + placeId;
        editText.setText(currentText + " " + url);
    }

    private void sendMessage() {
        String msg = editText.getText().toString();

        loadingOverlay.show();
        MessageSender messageSender = new MessageSender(client);
        messageSender.sendNotification(msg, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (loadingOverlay != null) {
            loadingOverlay.hide();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}