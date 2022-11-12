package com.yatenesturno.activities.settings;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.yatenesturno.R;
import com.yatenesturno.custom_views.NonSwipeableViewPager;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {
    private static final List<Integer> settingList = new ArrayList<Integer>() {{
//        add(R.string.my_subscriptions);
    }};

    private NonSwipeableViewPager viewPager;

    private SettingsListFragment settingsListFragment;
    private SettingViewFragment settingViewFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_a_ctivity);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(getDrawable(R.color.white));
        getSupportActionBar().setElevation(0);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        new Handler(Looper.myLooper()).postDelayed(this::initUI, 100);
    }

    private void initUI() {
        initViews();

        initViewPager();
    }

    private void initViewPager() {
        List<Fragment> fragments = new ArrayList<>();

        settingsListFragment = new SettingsListFragment();
        settingViewFragment = new SettingViewFragment();

        settingsListFragment.setSettingList(settingList);
        settingViewFragment.setSettingList(settingList);

        settingsListFragment.setListener(this::selectSetting);

        fragments.add(settingsListFragment);
        fragments.add(settingViewFragment);

        viewPager.setAdapter(new SettingFragmentAdapter(getSupportFragmentManager(), fragments));
    }

    private void selectSetting(int position) {
        settingViewFragment.setSetting(position);
        viewPager.setCurrentItem(1);
    }

    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() == 1) {
            viewPager.setCurrentItem(0);
        } else {
            super.onBackPressed();
        }
    }

    private void initViews() {
        viewPager = findViewById(R.id.fragmentContainer);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private static class SettingFragmentAdapter extends FragmentPagerAdapter {

        private final List<Fragment> fragmentList;

        public SettingFragmentAdapter(FragmentManager fm, List<Fragment> frags) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
            fragmentList = frags;
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }
    }
}