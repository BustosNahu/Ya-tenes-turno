package com.yatenesturno.custom_views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.cardview.widget.CardView;

import com.yatenesturno.R;

public class LoadingButton extends LinearLayoutCompat {

    private final ProgressBar progressBar;
     final CardView cardView;
    private final AppCompatTextView textViewButton;

    public LoadingButton(@NonNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.LoadingButton, 0, 0);

        String btnText = a.getString(R.styleable.LoadingButton_buttonText);
        int btnBackground = a.getColor(R.styleable.LoadingButton_buttonBackground, context.getColor(R.color.colorPrimary));
        boolean allCaps = a.getBoolean(R.styleable.LoadingButton_allCaps, false);

        a.recycle();

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.loading_button, this, true);

        textViewButton = findViewById(R.id.buttonText);
        textViewButton.setText(btnText);
        textViewButton.setAllCaps(allCaps);

        progressBar = findViewById(R.id.progressBar);
        cardView = findViewById(R.id.root);

        cardView.setCardBackgroundColor(btnBackground);
    }

    public LoadingButton(@NonNull Context context) {
        this(context, null);
    }

    public void setText(String text) {
        textViewButton.setText(text);
    }

    public void setBackgroundColor(int color) {
        cardView.setCardBackgroundColor(color);
    }

    public void setMatchParent() {
        cardView.getLayoutParams().width = LayoutParams.MATCH_PARENT;
    }

    public void showLoading() {
        progressBar.setVisibility(VISIBLE);
    }

    public void hideLoading() {
        progressBar.setVisibility(GONE);
    }

    public void textSize(int size) {
        textViewButton.setTextSize(size);
    }

    @Override
    public void setOnClickListener(@Nullable @org.jetbrains.annotations.Nullable OnClickListener l) {
        cardView.setOnClickListener(view -> {
            showLoading();
            l.onClick(view);
        });
    }
}
