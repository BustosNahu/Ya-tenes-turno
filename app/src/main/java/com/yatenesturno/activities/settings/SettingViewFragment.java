package com.yatenesturno.activities.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.yatenesturno.R;

import java.util.List;

public class SettingViewFragment extends Fragment {

    private List<Integer> settingList;

    public SettingViewFragment() {

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

    public void setSettingList(List<Integer> settingList) {
        this.settingList = settingList;
    }

    public void setSetting(int position) {
        Fragment fragment = null;

        initFragment(fragment);
    }

    public void initFragment(Fragment fragment) {
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
}