package com.yatenesturno.activities.onboarding;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yatenesturno.R;
import com.yatenesturno.activities.sign_in.ActivitySignIn;
import com.yatenesturno.objects.OnboardingItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import eu.davidea.flexibleadapter.items.IFilterable;

public class OnboardingFragment extends Fragment {

    private OnBoardingAdapter onboardingAdapter;
    LinearLayout layoutOnboardingIndicators;
    TextView finished;
    TextView omitText;
    ImageButton nextBtn;
    ImageButton backBtn;

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        setUpOnBoardingItems();

        View rootView = inflater.inflate(R.layout.onboarding_fragment, container, false);
        layoutOnboardingIndicators = rootView.findViewById(R.id.layoutOnboardingIndicators);
        nextBtn = rootView.findViewById(R.id.imageButtonOnboardingSkip);
        omitText = rootView.findViewById(R.id.textView7);
        finished = rootView.findViewById(R.id.txt_finished_onboarding);
        backBtn = rootView.findViewById(R.id.imageReplaceTxt);

        ViewPager2 onBoardingViewPager = rootView.findViewById(R.id.view_pager_onboarding);
        onBoardingViewPager.setAdapter(onboardingAdapter);
        setupOnboardingIndicators();

        onBoardingViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                setCurrentOnboardingIndicator(position);
            }
        });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onBoardingViewPager.getCurrentItem() + 1 < onboardingAdapter.getItemCount()) {
                    onBoardingViewPager.setCurrentItem(onBoardingViewPager.getCurrentItem() + 1);
                } else {
                    startActivity(new Intent(getContext(), ActivitySignIn.class));
                    saveUserState();
                    requireActivity().finish();
                }

            }
        });

        omitText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), ActivitySignIn.class));
                saveUserState();
                requireActivity().finish();
            }
        });

        finished.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), ActivitySignIn.class));
                saveUserState();
                requireActivity().finish();
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBoardingViewPager.setCurrentItem(onBoardingViewPager.getCurrentItem() - 1);

            }
        });


        return rootView;
    }


    public void saveUserState() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("isFirstTime", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean("isFirstTime", false);
        editor.apply();
    }


    private void setUpOnBoardingItems() {
        List<OnboardingItem> onboardingItems = new ArrayList<>();

        OnboardingItem itemAssistant = new OnboardingItem();

        itemAssistant.setTitle("Somos el asistente virtual para tu negocio");
        itemAssistant.setDescription("Tené organizado todos los turnos de tus clientes de manera automática las 24hs del día");
        itemAssistant.setImage(R.drawable.onboarding1);


        OnboardingItem itemClients = new OnboardingItem();
        itemClients.setTitle("Fútbol , Barberías, Padel, Lavaderos, Estéticas, Tatto y muchos más");
        itemClients.setDescription("Obtené clientes nuevos a través de nuestra comunidad");
        itemClients.setImage(R.drawable.onboarding2);

        OnboardingItem itemIdeals = new OnboardingItem();
        itemIdeals.setTitle("También es una herramienta ideal para...");
        itemIdeals.setDescription("Centros Médicos, Consultorios y Particulares");
        itemIdeals.setImage(R.drawable.onboarding3);

        onboardingItems.add(itemAssistant);
        onboardingItems.add(itemClients);
        onboardingItems.add(itemIdeals);
        onboardingAdapter = new OnBoardingAdapter(onboardingItems);
    }

    private void setupOnboardingIndicators() {

        ImageView[] indicators = new ImageView[onboardingAdapter.getItemCount()];
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                100,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.gravity = Gravity.CENTER;
        for (int i = 0; i < indicators.length; i++) {
            indicators[i] = new ImageView(requireContext());
            indicators[i].setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.onboarding_indicator_inactive));
            indicators[i].setLayoutParams(layoutParams);
            layoutOnboardingIndicators.addView(indicators[i]);
        }

    }

    private void setCurrentOnboardingIndicator(int index) {
        int childCount = layoutOnboardingIndicators.getChildCount();
        for (int i = 0; i < childCount; i++) {
            ImageView imageView = (ImageView) layoutOnboardingIndicators.getChildAt(i);
            if (i == index) {
                imageView.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.onboarding_indicator_active));
            } else {
                imageView.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.onboarding_indicator_inactive));
            }
        }
        if (index == onboardingAdapter.getItemCount() - 1) {
            nextBtn.setVisibility(View.GONE);
            finished.setVisibility(View.VISIBLE);
            omitText.setVisibility(View.GONE);
            backBtn.setVisibility(View.VISIBLE);
        } else {
            nextBtn.setVisibility(View.VISIBLE);
            finished.setVisibility(View.GONE);
            omitText.setVisibility(View.VISIBLE);
            backBtn.setVisibility(View.GONE);
        }
    }
}