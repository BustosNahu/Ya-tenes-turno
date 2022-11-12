package com.yatenesturno.activities.job_edit;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.yatenesturno.R;

public class MoreInfoHelper {

    private final ViewGroup root;
    private final FragmentManager childFragmentManager;
    private final int[] btnIds = new int[]{
            R.id.infoConcurrency,
            R.id.infoDuration,
            R.id.infoConcurrentServices,
            R.id.infoFixedTime,
            R.id.infoIsClass,
            R.id.infoInterval,
            R.id.infoSchedule,
            R.id.infoClassTimes,
            R.id.infoEmergency,
            R.id.infoCredits,
            R.id.infoBooking,
            R.id.infoBookingSimultaneously,
    };
    private final int[] stringResourceId = new int[]{
            R.string.infoConcurrency,
            R.string.infoDaysDuration,
            R.string.infoConcurrentServices,
            R.string.infoFixedTime,
            R.string.infoIsClass,
            R.string.infoInterval,
            R.string.infoSchedule,
            R.string.infoClassTimes,
            R.string.infoEmergency,
            R.string.infoCredits,
            R.string.infoBooking,
            R.string.infoBookingSimultaneously,
    };
    private final FragmentMoreInfo fragmentMoreInfo;

    public MoreInfoHelper(ViewGroup root, FragmentManager childFragmentManager) {
        this.root = root;
        this.childFragmentManager = childFragmentManager;
        this.fragmentMoreInfo = new FragmentMoreInfo();

        for (int i = 0; i < btnIds.length; i++) {
            addListener(
                    root.findViewById(btnIds[i]),
                    root.getContext().getString(stringResourceId[i])
            );
        }

    }

    public void addListener(AppCompatImageButton btnInfo, String infoText) {
        btnInfo.setOnClickListener(view -> fragmentMoreInfo.show(infoText, childFragmentManager));
    }

    public static class FragmentMoreInfo extends BottomSheetDialogFragment {

        private String infoText;

        public void show(String infoText, FragmentManager fragmentManager) {
            this.infoText = infoText;
            super.show(fragmentManager, "MORE_INFO");
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.dialog_more_info, container);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            view.findViewById(R.id.btnGotIt).setOnClickListener(view1 -> dismiss());
            ((AppCompatTextView) view.findViewById(R.id.labelMoreInfo)).setText(infoText);
        }
    }

}
