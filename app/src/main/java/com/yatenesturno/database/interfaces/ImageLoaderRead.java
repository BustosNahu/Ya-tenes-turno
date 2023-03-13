package com.yatenesturno.database.interfaces;

import android.content.Context;
import android.graphics.Bitmap;

import com.yatenesturno.object_interfaces.CustomUser;
import com.yatenesturno.object_interfaces.Place;

public interface ImageLoaderRead {

    void getImage(Context context, CustomUser user, OnGetImageListener listenerImageLoader);

    void getImage(Context context, Place place, OnGetImageListener listenerImageLoader);

    interface OnGetImageListener {

        void onSuccess(Bitmap bitmap);

        void onFailure();
    }
}
