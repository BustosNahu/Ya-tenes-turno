package com.yatenesturno.database;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import com.yatenesturno.object_interfaces.CustomUser;
import com.yatenesturno.object_interfaces.Place;

public interface ImageLoaderWrite {

    void writeImage(Context context, CustomUser name, Bitmap image, OnWriteImageListener listener);

    void writeImage(Context context, Place place, Bitmap image, OnWriteImageListener listener);

    void invalidateImage(Context context, CustomUser user, OnInvalidateImageListener listener);

    void invalidateImage(Context context, Place place, OnInvalidateImageListener listener);

    void writeImageToRemote(Context context, String placeId, Uri uri, OnWriteImageListener listener);

    interface OnWriteImageListener {
        void onSuccess();
    }

    interface OnInvalidateImageListener {
        void onSuccess();

        void onFailure();
    }
}
