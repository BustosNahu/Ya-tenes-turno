package com.yatenesturno.objects;

import android.text.Spanned;

public class NewPlaceIntroItem {
    private int image;
    private Spanned description;

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public Spanned getDescription() {
        return description;
    }

    public void setDescription(Spanned description) {
        this.description = description;
    }
}
