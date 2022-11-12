package com.yatenesturno.activities.place_register;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.yatenesturno.R;
import com.yatenesturno.activities.ObjectConfigurator;

public class NewPlaceWorksThereFragment extends ObjectConfigurator {

    private MaterialCheckBox checkBox;

    public NewPlaceWorksThereFragment() {

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putAll(saveState());
    }

    private Bundle saveState() {
        Bundle bundle = new Bundle();

        bundle.putBoolean("worksHere", worksHere());

        return bundle;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_new_place_e, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.checkBox = view.findViewById(R.id.checkboxNewPlace);

        if (savedInstanceState != null) {
            restoreState(savedInstanceState);
        }
    }

    private void restoreState(Bundle bundle) {
        checkBox.setChecked(bundle.getBoolean("worksHere"));
    }

    @Override
    public Bundle getData() {
        Bundle bundle = new Bundle();

        bundle.putBoolean("works_here", worksHere());

        return bundle;
    }

    public boolean worksHere() {
        return checkBox.isChecked();
    }

    @Override
    public boolean validateData() {
        return true;
    }

}