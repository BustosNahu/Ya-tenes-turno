package com.yatenesturno.object_views;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.LayoutRes;

import com.yatenesturno.R;
import com.yatenesturno.database.ImageLoaderRead;
import com.yatenesturno.database.ImageLoaderReadImpl;
import com.yatenesturno.object_interfaces.Place;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flexibleadapter.items.IFlexible;
import eu.davidea.viewholders.FlexibleViewHolder;

public class ViewPlace extends AbstractFlexibleItem<ViewPlace.ViewHolderPlace> {

    private final Place place;

    public ViewPlace(Place place) {
        this.place = place;
    }

    public Place getPlace() {
        return place;
    }

    @Override
    public boolean equals(Object o) {
        return false;
    }

    @LayoutRes
    @Override
    public int getLayoutRes() {
        return R.layout.item_place_search;
    }

    @Override
    public ViewHolderPlace createViewHolder(View view, FlexibleAdapter<IFlexible> adapter) {
        return new ViewHolderPlace(view, adapter);
    }

    @Override
    public void bindViewHolder(FlexibleAdapter<IFlexible> adapter, ViewHolderPlace holder, int position, List<Object> payloads) {
        holder.textViewBusinessName.setText(place.getBusinessName());
        getProfileImage(holder.ivPic);
    }

    private void getProfileImage(final ImageView iv) {
        new ImageLoaderReadImpl().getImage(iv.getContext(), place, new ImageLoaderRead.OnGetImageListener() {
            @Override
            public void onSuccess(Bitmap bitmap) {
                iv.setImageBitmap(bitmap);
            }

            @Override
            public void onFailure() {

            }
        });
    }

    public static class ViewHolderPlace extends FlexibleViewHolder {

        public TextView textViewBusinessName;
        public CircleImageView ivPic;

        public ViewHolderPlace(View view, FlexibleAdapter adapter) {
            super(view, adapter);
            this.textViewBusinessName = view.findViewById(R.id.textViewBusinessNamePlaceSearch);
            this.ivPic = view.findViewById(R.id.ivPic);
        }
    }
}
