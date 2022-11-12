package com.yatenesturno.activities.help;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.yatenesturno.R;

import java.util.List;

public class SingleHelpViewFragment extends Fragment {

    private List<Integer> helpFragmentTitleList;
    private Fragment fragment;

    public SingleHelpViewFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_setting_view, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();

        initFragment(fragment);
    }

    public void setHelpFragmentTitleList(List<Integer> helpFragmentTitleList) {
        this.helpFragmentTitleList = helpFragmentTitleList;
    }

    public void setHelpFragment(int position) {
        fragment = null;

        switch (position) {
            case 0:
                fragment = new ViewTutorialsFragment();
                break;
        }
    }

    public void initFragment(Fragment fragment) {
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
}