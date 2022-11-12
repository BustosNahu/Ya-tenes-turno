package com.yatenesturno.activities.tutorial_screen;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.viewpager.widget.PagerAdapter;

import com.yatenesturno.R;
import com.yatenesturno.custom_views.NonSwipeableViewPager;

import java.util.List;

public class TutorialScreenImpl implements TutorialScreen {

    private NonSwipeableViewPager viewPager;
    private CardView btnNext, btnPrev;
    private ViewGroup rootView;
    private List<Screen> screenList;
    private View view;
    private OnCloseListener listener;

    private SharedPreferences getSharedPref(AppCompatActivity activity) {
        String SHARED_PREF = "PREF_TUTORIAL";
        return activity.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE);
    }

    /**
     * Checks wheter the corresponding activity tutorial has been shown
     * @param activity activity to check for
     * @return true if tutorial hasnt been shown already, false otherwise
     */
    public boolean shouldShowTutorial(AppCompatActivity activity) {
        SharedPreferences sharedPreferences = getSharedPref(activity);

        return !sharedPreferences.getBoolean(getNameForActivity(activity), false);
    }

    private void setDontShowAgain(AppCompatActivity activity) {
        SharedPreferences sharedPreferences = getSharedPref(activity);
        sharedPreferences.edit().putBoolean(getNameForActivity(activity), true).apply();
    }

    private String getNameForActivity(AppCompatActivity activity) {
        return activity.getLocalClassName();
    }

    @Override
    public void showTutorial(AppCompatActivity activity, List<Screen> screenList, boolean ignore) {
        if (!shouldShowTutorial(activity) && !ignore) {
            return;
        }

        new Handler(Looper.myLooper()).postDelayed(() -> {
            hideActionBar(activity);
            this.screenList = screenList;
            setUpViewPager(activity, screenList);

            setDontShowAgain(activity);
        }, 1500);
    }

    private void hideActionBar(AppCompatActivity activity) {
        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().hide();
        }
    }

    private void showActionBar(AppCompatActivity activity) {
        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().show();
        }
    }

    private void setUpViewPager(AppCompatActivity activity, List<Screen> screenList) {
        rootView = activity.findViewById(R.id.root);

        view = activity.getLayoutInflater().inflate(R.layout.tutorial_view_pager, rootView, false);

        viewPager = view.findViewById(R.id.viewPager);
        CustomPagerAdapter adapter = new CustomPagerAdapter(activity, screenList);
        viewPager.setAdapter(adapter);

        btnNext = view.findViewById(R.id.btnNext);
        btnPrev = view.findViewById(R.id.btnPrev);
        AppCompatImageButton btnClose = view.findViewById(R.id.btnClose);

        btnNext.setOnClickListener(v -> handleNextBtnClick(activity));
        btnPrev.setOnClickListener(v -> handlePrevBtnClick(activity));
        btnClose.setOnClickListener(view1 -> close(activity));

        rootView.addView(view);
    }

    private void close(AppCompatActivity activity) {
        rootView.removeView(view);
        showActionBar(activity);

        if (listener != null) {
            listener.onClose();
        }
    }

    public void setListener(OnCloseListener listener) {
        this.listener = listener;
    }

    private void handleNextBtnClick(AppCompatActivity activity) {
        if (viewPager.getCurrentItem() < screenList.size() - 1) {
            viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);

            if (viewPager.getCurrentItem() == screenList.size() - 1) {
                ((AppCompatTextView) btnNext.getChildAt(0)).setText("Finalizar");
            } else {
                ((AppCompatTextView) btnNext.getChildAt(0)).setText("Siguiente");
            }

            if (viewPager.getCurrentItem() == 1) {
                btnPrev.setVisibility(View.VISIBLE);
            }
        } else {
            close(activity);
        }
    }

    private void handlePrevBtnClick(AppCompatActivity activity) {
        if (viewPager.getCurrentItem() > 0) {
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);

            ((AppCompatTextView) btnNext.getChildAt(0)).setText("Siguiente");

            if (viewPager.getCurrentItem() == 0) {
                btnPrev.setVisibility(View.GONE);
            }
        }
    }

    public interface OnCloseListener {
        void onClose();
    }

    private static class CustomPagerAdapter extends PagerAdapter {

        private final Context context;
        private final LayoutInflater layoutInflater;
        private final List<Screen> screenList;

        public CustomPagerAdapter(Context context, List<Screen> screenList) {

            this.context = context;
            this.screenList = screenList;

            layoutInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return screenList.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            View itemView = layoutInflater.inflate(R.layout.tutorial_pager_item, container, false);

            ImageView imageView = itemView.findViewById(R.id.imageView);

            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),
                    screenList.get(position).getContentId());
            RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(context.getResources(), bitmap);
            roundedBitmapDrawable.setCornerRadius(16);
            imageView.setImageDrawable(roundedBitmapDrawable);

            container.addView(itemView);

            return itemView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, @NonNull Object object) {
            container.removeView((LinearLayoutCompat) object);
        }

    }
}
