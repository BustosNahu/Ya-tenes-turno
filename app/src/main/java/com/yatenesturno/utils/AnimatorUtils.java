package com.yatenesturno.utils;

import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

public class AnimatorUtils {

    public static void exitToLeftEnterFromRight(View view, int durationInMillis, final WhenOnMidTransitionListener listener) {

    }

    public static void exitToRightEnterFromLeft(final View view, int durationInMillis, final WhenOnMidTransitionListener listener) {
        TranslateAnimation animObj = new TranslateAnimation(0, view.getWidth(), 0, 0);
        animObj.setDuration(100);
        view.startAnimation(animObj);

        animObj.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                listener.whenOnMidTransition(view);

                TranslateAnimation animObj = new TranslateAnimation(-view.getWidth(), 0, 0, 0);
                animObj.setDuration(100);
                view.startAnimation(animObj);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    public static void smoothOutAndSmoothIn(final View view, final int durationInMillis, final WhenOnMidTransitionListener listener) {
        Animation fadeOutAnimation = new AlphaAnimation(1, 0);
        fadeOutAnimation.setInterpolator(new AccelerateInterpolator());
        fadeOutAnimation.setDuration(durationInMillis / 2);
        view.startAnimation(fadeOutAnimation);

        fadeOutAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                listener.whenOnMidTransition(view);

                Animation fadeInAnimation = new AlphaAnimation(0, 1);
                fadeInAnimation.setInterpolator(new AccelerateInterpolator());
                fadeInAnimation.setDuration(durationInMillis / 2);
                view.startAnimation(fadeInAnimation);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    public static void smoothOut(final View view, final int durationInMillis, final OnAnimationFinish listener) {
        Animation fadeOutAnimation = new AlphaAnimation(1, 0);
        fadeOutAnimation.setInterpolator(new AccelerateInterpolator());
        fadeOutAnimation.setDuration(durationInMillis / 2);
        view.startAnimation(fadeOutAnimation);

        fadeOutAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                listener.onFinish(view);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    public interface WhenOnMidTransitionListener {
        void whenOnMidTransition(View view);
    }

    public interface OnAnimationFinish {
        void onFinish(View view);
    }
}
