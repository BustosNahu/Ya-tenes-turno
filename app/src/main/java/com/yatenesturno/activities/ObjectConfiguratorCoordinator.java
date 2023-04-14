package com.yatenesturno.activities;

import android.os.Bundle;
import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.yatenesturno.R;

import java.util.List;

public class ObjectConfiguratorCoordinator {

    private final PagerAdapter adapter;
//    private final ViewPager viewPager;
    private final ListenerCoordinator listener;
    private final Bundle bundle;

    private final List<ObjectConfigurator> fragments;

    public ObjectConfiguratorCoordinator(FragmentManager fragmentManager,
                                         View rootView,
                                         List<ObjectConfigurator> fragments,
                                         ListenerCoordinator listener) {

//        this.viewPager = rootView.findViewById(R.id.view_pager);
        this.fragments = fragments;
        this.listener = listener;
        this.adapter = new ObjectConfigPagerAdapter(fragmentManager, fragments);
        this.bundle = new Bundle();

//        viewPager.setOffscreenPageLimit(fragments.size());
//        viewPager.setAdapter(adapter);
    }

//    public boolean hasNext() {
//        return viewPager.getCurrentItem() < adapter.getCount() - 1;
//    }

//    public boolean hasPrev() {
//        return viewPager.getCurrentItem() > 0;
//    }

//    private void updateBundle() {
//        Bundle currentFragBundle = fragments.get(viewPager.getCurrentItem()).getData();
//
//        if (currentFragBundle != null) {
//            for (String key : currentFragBundle.keySet()) {
//                bundle.putSerializable(key, currentFragBundle.getSerializable(key));
//            }
//        }
//    }

//    public void next() {
//        if (fragments.get(viewPager.getCurrentItem()).validateData()) {
//            updateBundle();
//            if (hasNext()) {
//                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
//            } else {
//                listener.onFinish(bundle);
//            }
//        }
//    }

//    public void prev() {
//        if (hasPrev()) {
//            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
//        } else {
//            listener.onCanceled();
//        }
//    }

//    public int getCurrentItem() {
//        return viewPager.getCurrentItem();
//    }

//    public void setCurrentItem(int position) {
//        viewPager.setCurrentItem(position);
//    }

    public interface ListenerCoordinator {
        void onFinish(Bundle bundle);

        void onCanceled();
    }

    private static class ObjectConfigPagerAdapter extends FragmentStatePagerAdapter {

        private final List<ObjectConfigurator> fragmentList;

        public ObjectConfigPagerAdapter(FragmentManager fm, List<ObjectConfigurator> frags) {
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
