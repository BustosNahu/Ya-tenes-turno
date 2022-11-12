package com.yatenesturno.activities.place_view;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yatenesturno.Constants;
import com.yatenesturno.R;
import com.yatenesturno.custom_views.LoadingOverlay;
import com.yatenesturno.database.ImageLoaderRead;
import com.yatenesturno.database.ImageLoaderReadImpl;
import com.yatenesturno.functionality.ManagerPlace;
import com.yatenesturno.object_interfaces.Place;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdminPlacesActivity extends AppCompatActivity {
    private OnPlaceClickListener recyclerViewOwnedPlacesListener;
    private AdapterRecyclerViewOwnedPlaces adapterOwnedPlaces;
    private RecyclerView recyclerViewOwnedPlaces;
    private List<Place> ownedPlacesList;

    private LoadingOverlay loadingOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_places);

        getSupportActionBar().setBackgroundDrawable(getDrawable(R.color.white));
        getSupportActionBar().setElevation(0);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        setTitle(getString(R.string.my_places));

        loadingOverlay = new LoadingOverlay(findViewById(R.id.root));

        loadingOverlay.show();
        new Handler().postDelayed(this::initUI, 100);
    }

    private void initUI() {
        loadingOverlay.show();
        ManagerPlace.getInstance().getOwnedPlaces(new ManagerPlace.OnPlaceListFetchListener() {
            @Override
            public void onFetch(List<Place> placeList) {
                loadingOverlay.hide();
                refreshUI(placeList);
            }

            @Override
            public void onFailure() {
                Toast.makeText(getApplicationContext(), getString(R.string.error_de_conexion), Toast.LENGTH_SHORT).show();
                loadingOverlay.hide();
            }
        });
    }

    public void refreshUI(List<Place> list) {
        ownedPlacesList = list;
        recyclerViewOwnedPlaces = findViewById(R.id.recyclerViewOwnedPlaces);
        recyclerViewOwnedPlacesListener = new OnPlaceClickListener();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerViewOwnedPlaces.setLayoutManager(layoutManager);

        adapterOwnedPlaces = new AdapterRecyclerViewOwnedPlaces(ownedPlacesList);
        recyclerViewOwnedPlaces.setAdapter(adapterOwnedPlaces);

        loadingOverlay.hide();
        if (ownedPlacesList.size() > 0) {
            displayHasPlaces();
            adapterOwnedPlaces.notifyDataSetChanged();
        } else {
            displayNoPlaces();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void displayHasPlaces() {
        recyclerViewOwnedPlaces.setVisibility(View.VISIBLE);
        findViewById(R.id.noPlacesView).setVisibility(View.GONE);
    }

    private void displayNoPlaces() {
        recyclerViewOwnedPlaces.setVisibility(View.GONE);
        findViewById(R.id.noPlacesView).setVisibility(View.VISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == Constants.RC_EDIT_PLACE) {
                initUI();
            } else {
                super.onActivityResult(requestCode, resultCode, data);
            }
            setResult(resultCode);
        }
    }

    private class OnPlaceClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            int position = recyclerViewOwnedPlaces.getChildLayoutPosition(view);
            Place place = ownedPlacesList.get(position);

            Bundle bundle = new Bundle();
            bundle.putString("placeId", place.getId());

            Intent intent = new Intent(getApplicationContext(), PlaceViewActivity.class);
            intent.putExtras(bundle);
            startActivityForResult(intent, Constants.RC_EDIT_PLACE);
        }
    }

    private class AdapterRecyclerViewOwnedPlaces extends RecyclerView.Adapter<AdapterRecyclerViewOwnedPlaces.ViewHolderOwnedPlaces> {

        private final List<Place> ownedPlaceList;
        private final Map<String, Bitmap> placeBitmapMap;

        public AdapterRecyclerViewOwnedPlaces(List<Place> ownedPlaces) {
            this.ownedPlaceList = ownedPlaces;
            this.placeBitmapMap = new HashMap<>();
        }

        @Override
        public ViewHolderOwnedPlaces onCreateViewHolder(ViewGroup parent,
                                                        int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.main_place_item_layout, parent, false);

            v.setOnClickListener(recyclerViewOwnedPlacesListener);
            return new ViewHolderOwnedPlaces(v);
        }

        @Override
        public void onBindViewHolder(ViewHolderOwnedPlaces holder, int position) {
            holder.labelBusinessName.setText(ownedPlaceList.get(position).getBusinessName());

            Place place = ownedPlaceList.get(position);
            if (!placeBitmapMap.containsKey(place.getId())) {
                getProfileImage(place, holder.ivPic);
            } else {
                Bitmap image = placeBitmapMap.get(place.getId());
                if (image != null) {
                    holder.ivPic.setImageBitmap(image);
                }
            }
        }

        private void getProfileImage(final Place place, final ImageView iv) {
            new ImageLoaderReadImpl().getImage(getApplicationContext(), place, new ImageLoaderRead.OnGetImageListener() {
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
        public int getItemCount() {
            return ownedPlaceList.size();
        }

        public class ViewHolderOwnedPlaces extends RecyclerView.ViewHolder {
            public View relativeLayout;
            public TextView labelBusinessName;
            public CircleImageView ivPic;

            public ViewHolderOwnedPlaces(View v) {
                super(v);
                this.relativeLayout = v;
                this.labelBusinessName = v.findViewById(R.id.labelBusinessName);
                this.ivPic = v.findViewById(R.id.ivPic);
            }
        }
    }
}