package com.yatenesturno.activities.help;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.yatenesturno.R;

import java.util.ArrayList;
import java.util.List;

public class HelpActivity extends AppCompatActivity {

    private static final List<Integer> settingList = new ArrayList<Integer>() {{
        add(R.string.tutoriales);
    }};

    private ViewPager2 viewPager;

    private HelpsListFragment helpsListFragment;
    private SingleHelpViewFragment singleHelpViewFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(getDrawable(R.color.white));
        getSupportActionBar().setTitle("Ayuda");
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

        helpsListFragment = new HelpsListFragment();
        singleHelpViewFragment = new SingleHelpViewFragment();

        helpsListFragment.setHelpFragmentTitleList(settingList);
        singleHelpViewFragment.setHelpFragmentTitleList(settingList);

        helpsListFragment.setListener(this::selectSetting);

        fragments.add(helpsListFragment);
        fragments.add(singleHelpViewFragment);

        viewPager.setAdapter(new HelpAdapter(getSupportFragmentManager(), getLifecycle(), fragments));
    }

    private void selectSetting(int position) {
        viewPager.setCurrentItem(1);
        singleHelpViewFragment.setHelpFragment(position);
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
        viewPager = findViewById(R.id.viewPager);
        viewPager.setUserInputEnabled(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private static class HelpAdapter extends FragmentStateAdapter {

        private final List<Fragment> fragmentList;

        public HelpAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle, List<Fragment> fragmentList) {
            super(fragmentManager, lifecycle);

            this.fragmentList = fragmentList;
        }


        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getItemCount() {
            return fragmentList.size();
        }
    }
}