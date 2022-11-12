package com.yatenesturno.activities.tutorial_screen;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public interface TutorialScreen {

    void showTutorial(AppCompatActivity act, List<Screen> screenList, boolean ignore);

}
