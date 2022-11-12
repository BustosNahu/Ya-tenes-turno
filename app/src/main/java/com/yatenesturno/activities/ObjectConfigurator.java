package com.yatenesturno.activities;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

public abstract class ObjectConfigurator extends Fragment {

    public abstract Bundle getData();

    public abstract boolean validateData();

}
