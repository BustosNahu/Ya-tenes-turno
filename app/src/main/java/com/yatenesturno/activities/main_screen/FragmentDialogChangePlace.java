package com.yatenesturno.activities.main_screen;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.yatenesturno.R;
import com.yatenesturno.custom_views.ListViewForBottomSheet;
import com.yatenesturno.database.ImageLoaderRead;
import com.yatenesturno.database.ImageLoaderReadImpl;
import com.yatenesturno.functionality.ManagerPlace;
import com.yatenesturno.object_interfaces.Place;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class FragmentDialogChangePlace extends BottomSheetDialogFragment {

    private final FragmentMainViewPager fragmentMain;
    private List<Place> _placeList;
    private final ManagerPlace managerPlace = ManagerPlace.getInstance();

    public FragmentDialogChangePlace(FragmentMainViewPager fragmentMain) {
        this.fragmentMain = fragmentMain;
        managerPlace.getPlaces(new ManagerPlace.OnPlaceListFetchListener() {
            @Override
            public void onFetch(List<Place> placeList) {
                FragmentDialogChangePlace.this._placeList = placeList;
                Log.d("DialogChangePlaceList", placeList.toString());
                managerPlace.replaceList(placeList ,ManagerPlace.getInstance());
            }

            @Override
            public void onFailure() {
                Toast.makeText(requireActivity(), getString(R.string.error_de_conexion), Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_change_place_layout, container);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initUI();
    }

    private void initUI() {
        ListViewForBottomSheet listViewForBottomSheet = requireView().findViewById(R.id.listViewPlaces);
        listViewForBottomSheet.setAdapter(new AdapterChangePlace());
        listViewForBottomSheet.setOnItemClickListener((parent, view, position, id) -> onPlaceSelected(position));
    }

    private void onPlaceSelected(int position) {
        fragmentMain.changePlace(_placeList.get(position));
        dismiss();
    }

    private class AdapterChangePlace extends BaseAdapter {

        private final Map<String, Bitmap> placeBitmapMap;

        public AdapterChangePlace() {
            this.placeBitmapMap = new HashMap<>();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.place_layout, parent, false);
            }
            ((TextView) convertView.findViewById(R.id.labelPlaceName)).setText(_placeList.get(position).getBusinessName());

            Place place = _placeList.get(position);
            CircleImageView circleImageView = convertView.findViewById(R.id.ivPic);
            if (!placeBitmapMap.containsKey(place.getId())) {
                getProfileImage(place, circleImageView);
            } else {
                Bitmap image = placeBitmapMap.get(place.getId());
                if (image != null) {
                    circleImageView.setImageBitmap(image);
                }
            }

            return convertView;
        }

        private void getProfileImage(final Place place, final ImageView iv) {
            new ImageLoaderReadImpl().getImage(fragmentMain.getContext(), place, new ImageLoaderRead.OnGetImageListener() {
                @Override
                public void onSuccess(Bitmap bitmap) {
                    placeBitmapMap.put(place.getId(), bitmap);
                    iv.setImageBitmap(bitmap);
                }

                @Override
                public void onFailure() {

                }
            });
        }

        @Override
        public int getCount() {
            return _placeList.size();
        }

        @Override
        public Object getItem(int position) {
            return _placeList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

    }
}
