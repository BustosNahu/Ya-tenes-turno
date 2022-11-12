package com.yatenesturno.activities.main_screen;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.WriterException;
import com.yatenesturno.R;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class ActivityViewQR extends AppCompatActivity {

    private String placeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_qr);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        placeId = getIntent().getExtras().getString("placeId");
    }

    @Override
    protected void onResume() {
        super.onResume();
        initUI();
    }

    private void initUI() {
        String placeUrl = "https://yatenesturno.com.ar/place/" + placeId;
        QRGEncoder qrgEncoder = new QRGEncoder(placeUrl, null, QRGContents.Type.TEXT, 800);

        try {
            Bitmap bitmap = qrgEncoder.encodeAsBitmap();
            ImageView ivQr = findViewById(R.id.imageViewQR);
            ivQr.setImageBitmap(bitmap);

            ivQr.getLayoutParams().height = findViewById(R.id.containerQR).getLayoutParams().width;
            ivQr.getLayoutParams().width = findViewById(R.id.containerQR).getLayoutParams().width;

            ivQr.requestLayout();

        } catch (WriterException ignored) {

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