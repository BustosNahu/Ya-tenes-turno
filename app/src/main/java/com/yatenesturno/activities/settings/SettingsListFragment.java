package com.yatenesturno.activities.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.yatenesturno.R;

import java.util.List;


public class SettingsListFragment extends Fragment {

    private List<Integer> settingList;
    private OnSettingSelectedListener listener;
    private ListView listViewSettings;

    public SettingsListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_settings_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initUI();
    }

    private void initUI() {
        initViews();

        listViewSettings.setAdapter(new AdapterSettings(settingList));
        listViewSettings.setOnItemClickListener((parent, view, position, id) -> setFragmentViewToSetting(position));
    }

    public void setListener(OnSettingSelectedListener listener) {
        this.listener = listener;
    }

    public void setSettingList(List<Integer> settingList) {
        this.settingList = settingList;
    }

    private void setFragmentViewToSetting(int position) {
        listener.onSelected(position);
    }

    private void initViews() {
        listViewSettings = getView().findViewById(R.id.listViewSettings);
    }

    public interface OnSettingSelectedListener {
        void onSelected(int position);
    }

    public class AdapterSettings extends BaseAdapter {

        private final List<Integer> settingsNameList;

        public AdapterSettings(List<Integer> list) {
            this.settingsNameList = list;
        }

        @Override
        public int getCount() {
            return settingsNameList.size();
        }

        @Override
        public Object getItem(int position) {
            return settingsNameList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.settings_layout, parent, false);
            }
            ((TextView) convertView.findViewById(R.id.labelSetting)).setText(getString(settingsNameList.get(position)));

            return convertView;
        }
    }
}