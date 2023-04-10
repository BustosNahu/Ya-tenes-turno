package com.yatenesturno.activities.web_view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import android.widget.ImageView;

import com.facebook.shimmer.Shimmer;
import com.facebook.shimmer.ShimmerDrawable;
import com.yatenesturno.R;

public class OpenWebActivity extends AppCompatActivity {


    String uri = "https://yatenesturno.com.ar/login";


    ImageView iv_web_link;
    private Animation iv_web_link_anim;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.open_web_layout);

        getSupportActionBar().setBackgroundDrawable(getDrawable(R.color.white));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setElevation(0);
        getSupportActionBar().setTitle("Mi PC - IPhone");

        initViews();
        linkClicked();

    }

    /**
     * Method to know if the image view URL is clicked

     * and it opens login web
     */
    private void linkClicked() {
        iv_web_link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                startActivity(i);
            }
        });
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
        iv_web_link = findViewById(R.id.iv_web_link);
        initAnimation();


    }

    private void initAnimation() {
        Shimmer shimmer = new Shimmer.AlphaHighlightBuilder()
                .setDuration(1500)
                .setBaseAlpha(0.9f)
                .setHighlightAlpha(1f)
                .setDirection(Shimmer.Direction.LEFT_TO_RIGHT)
                .setAutoStart(true)
                .build();

        // Agrega el shimmer a la ImageView
        ShimmerDrawable shimmerDrawable = new ShimmerDrawable();
        shimmerDrawable.setShimmer(shimmer);
        iv_web_link.setImageDrawable(shimmerDrawable);
    }
}