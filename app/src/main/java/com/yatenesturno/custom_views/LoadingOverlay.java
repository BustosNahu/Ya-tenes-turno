package com.yatenesturno.custom_views;

import android.view.LayoutInflater;
import android.view.View;

import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.yatenesturno.R;

public class LoadingOverlay {

    private final CoordinatorLayout rootView;
    private View progressView;

    public LoadingOverlay(CoordinatorLayout rootView) {
        this.rootView = rootView;
        inflateProgressView();
    }

    private void inflateProgressView() {
        LayoutInflater inflater = LayoutInflater.from(rootView.getContext());
        progressView = inflater.inflate(R.layout.progress_view, rootView, false);
        progressView.findViewById(R.id.progressBar).setScaleY(5f);
        rootView.addView(progressView);
    }

    public void show() {
        progressView.setVisibility(View.VISIBLE);
    }

    public void hide() {
        progressView.setVisibility(View.GONE);
    }
}
