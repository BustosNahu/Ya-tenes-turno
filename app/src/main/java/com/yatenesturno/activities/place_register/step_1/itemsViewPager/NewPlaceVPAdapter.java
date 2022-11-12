package com.yatenesturno.activities.place_register.step_1.itemsViewPager;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.yatenesturno.R;
import com.yatenesturno.objects.NewPlaceIntroItem;

import java.util.List;

public class NewPlaceVPAdapter extends RecyclerView.Adapter<NewPlaceVPAdapter.NewPlaceVPViewHolder> {

    private List<NewPlaceIntroItem> newPlaceIntroItems;
    ImageView ivPlace;
    TextView description;
    public NewPlaceVPAdapter(List<NewPlaceIntroItem> newPlaceIntroItems) {
        this.newPlaceIntroItems = newPlaceIntroItems;
    }

    @Override
    public void onBindViewHolder(@NonNull NewPlaceVPAdapter.NewPlaceVPViewHolder holder, int position) {
        holder.setIntro(newPlaceIntroItems.get(position));
    }

    @Override
    public int getItemCount() {
        return newPlaceIntroItems.size();
    }

    @NonNull
    @Override
    public NewPlaceVPAdapter.NewPlaceVPViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NewPlaceVPViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.item_container_new_place_intro_vp, parent, false)
        );
    }

    class NewPlaceVPViewHolder extends RecyclerView.ViewHolder {
        public NewPlaceVPViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPlace = itemView.findViewById(R.id.image_new_intro_container);
            description = itemView.findViewById(R.id.tv_item_new_place_intro_container_1);
        }

        void setIntro(NewPlaceIntroItem item) {
            description.setText(item.getDescription());
            ivPlace.setImageDrawable(ContextCompat.getDrawable(ivPlace.getContext(), item.getImage()));
        }
    }
}

