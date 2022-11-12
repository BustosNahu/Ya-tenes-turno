package com.yatenesturno.activities.place_register.step_1;

import static com.yatenesturno.R.id.new_place_intro_step3_viewpager;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.yatenesturno.R;
import com.yatenesturno.activities.ObjectConfigurator;
import com.yatenesturno.activities.first_shop.FirstShop;
import com.yatenesturno.activities.main_screen.MainActivity;
import com.yatenesturno.activities.place_register.step_1.itemsViewPager.NewPlaceVPAdapter;
import com.yatenesturno.activities.place_register.step_2.NewPlaceIntroStep2;
import com.yatenesturno.activities.place_register.step_2.NewPlaceStep2;
import com.yatenesturno.functionality.ManagerPlace;
import com.yatenesturno.object_interfaces.Place;
import com.yatenesturno.objects.NewPlaceIntroItem;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class NewPlaceIntroStep1 extends ObjectConfigurator {


    private NewPlaceVPAdapter adapter;
    LinearLayout layoutNewPlaceIndicator;
    ImageButton forwardArrow;
    ImageButton backwardArrow;
    Boolean finished = false;
    ViewPager2 viewPager;

    CardView btnStart;
    ImageButton btnBack;
    Boolean hasPlaces;
    public NewPlaceIntroStep1() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getActivity().getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getContext().getColor(R.color.black));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setupNewPlaceItems();

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_new_place_intro_steps, container, false);
        viewPager = view.findViewById(R.id.new_place_intro_step_viewpager);
        viewPager.setAdapter(adapter);
        layoutNewPlaceIndicator = view.findViewById(R.id.linearLayout_indicator_new_place_container);
        forwardArrow = view.findViewById(R.id.forward_btn_new_place_container);
        backwardArrow = view.findViewById(R.id.backward_btn_new_place_container);
        btnStart = view.findViewById(R.id.btnStart_intro);
        btnBack = view.findViewById(R.id.back_btn_new_place_intro);

        setupOnboardingIndicators();
        setCurrentOnboardingIndicator(0);
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                setCurrentOnboardingIndicator(position);
            }
        });

        forwardArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
            }
        });

        backwardArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
            }
        });

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO CAMBIAR STEP1
                Fragment fragment = new NewPlaceStep1();
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.new_place_fragment_main, fragment)
                        .commit();
            }
        });



        if (!finished){
            btnStart.setCardBackgroundColor(requireContext().getColor(R.color.darker_grey));
        }
        //btnState();
        getPlaces();
        return view;
    }


    private void getPlaces() {
        ManagerPlace.getInstance().getPlaces(new ManagerPlace.OnPlaceListFetchListener() {
            @Override
            public void onFetch(List<Place> places) {
                if (!places.isEmpty()){
                    hasPlaces = true;
                }else{
                    hasPlaces = false;

                }

                btnBack.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (hasPlaces){
                            Intent intent = new Intent (requireActivity(), MainActivity.class);
                            startActivity(intent);
                        }else{
                            Intent intent = new Intent (requireActivity(), FirstShop.class);
                            startActivity(intent);
                        }
                    }
                });
            }

            @Override
            public void onFailure() {
                Toast.makeText(requireContext(), getString(R.string.error_de_conexion), Toast.LENGTH_SHORT).show();
                hasPlaces = false;

            }
        });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("currentItem", 0);
    }



    private void setupNewPlaceItems() {

        List<NewPlaceIntroItem> introItems = new ArrayList<>();

        Spannable description1 = new SpannableString("En esta sección te pediremos que ingreses los datos de tu tienda");
        description1.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.colorPrimary)), 33, description1.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        description1.setSpan(new StyleSpan(Typeface.BOLD), 33, description1.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);


        NewPlaceIntroItem introItem1 = new NewPlaceIntroItem();
        introItem1.setDescription(description1);
        introItem1.setImage(R.drawable.place_shop_vp_1);

        Spannable description2 = new SpannableString("Si brindas tus servicios en un lugar específico, te pediremos que coloque la direción del lugar en donde atiendes Podría ser la dirección de tu peluqueria.");
        description2.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.colorPrimary)), 49, 113, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        description2.setSpan(new StyleSpan(Typeface.BOLD), 49, 113, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        description2.setSpan(new StyleSpan(Typeface.BOLD), 128, description2.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        NewPlaceIntroItem introItem2 = new NewPlaceIntroItem();
        introItem2.setDescription(description2);
        introItem2.setImage(R.drawable.place_shop_vp_2);


        Spannable description3 = new SpannableString("Pero si vas a ofrecer tu servicio como profesional sin espacio tienda o a domicilio, completala con tus datos personales Podría ser, Dr Díaz, especialista en rodilla.");
        description3.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.colorPrimary)), 85, 121, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        description3.setSpan(new StyleSpan(Typeface.BOLD), 85, 121, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        description3.setSpan(new StyleSpan(Typeface.BOLD), 133, description3.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        NewPlaceIntroItem introItem3 = new NewPlaceIntroItem();
        introItem3.setDescription(description3);
        introItem3.setImage(R.drawable.place_shop_vp_3);

        introItems.add(introItem1);
        introItems.add(introItem2);
        introItems.add(introItem3);
        adapter = new NewPlaceVPAdapter(introItems);
    }

    private void setupOnboardingIndicators() {

        ImageView[] indicators = new ImageView[adapter.getItemCount()];
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.gravity = Gravity.CENTER;
        for (int i = 0; i < indicators.length; i++) {
            indicators[i] = new ImageView(requireContext());
            indicators[i].setPadding(3,0,10,0);
            indicators[i].setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.new_place_intro_indicador_inactive));
//            indicators[i].setLayoutParams(layoutParams);
            layoutNewPlaceIndicator.addView(indicators[i]);
        }
    }

    private void setCurrentOnboardingIndicator(int index) {
        validateData();
        int childCount = layoutNewPlaceIndicator.getChildCount();
        for (int i = 0; i < childCount; i++) {
            ImageView imageView = (ImageView) layoutNewPlaceIndicator.getChildAt(i);
            if (i == index) {
                imageView.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.new_place_intro_indicador_active));
            } else {
                imageView.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.new_place_intro_indicador_inactive));
            }
        }
        if (index == adapter.getItemCount() - 1) {
            finished = true;
            forwardArrow.setVisibility(View.GONE);
            backwardArrow.setVisibility(View.VISIBLE);
            btnStart.setEnabled(true);
            btnStart.setCardBackgroundColor(requireContext().getColor(R.color.colorPrimary));
        } else if (index >= 1) {
            forwardArrow.setVisibility(View.VISIBLE);
            backwardArrow.setVisibility(View.VISIBLE);
        } else if (index == 0) {
            forwardArrow.setVisibility(View.VISIBLE);
            backwardArrow.setVisibility(View.GONE);
        }
    }



    @Override
    public Bundle getData() {
        return null;
    }

    @Override
    public boolean validateData() {
        return finished;
    }
}