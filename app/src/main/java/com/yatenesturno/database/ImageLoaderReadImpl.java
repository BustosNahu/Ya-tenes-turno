package com.yatenesturno.database;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.yatenesturno.Constants;
import com.yatenesturno.object_interfaces.CustomUser;
import com.yatenesturno.object_interfaces.Place;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import cz.msebera.android.httpclient.Header;


public class ImageLoaderReadImpl implements ImageLoaderRead {

    @Override
    public void getImage(final Context context, final CustomUser user, final OnGetImageListener listener) {

        new Handler(Looper.myLooper()).postDelayed(() -> {
            Bitmap bitmap = getFromStorage(getFileForUser(context, user));
            if (bitmap == null) {
                fetchFromRemote(context, user, listener);
            } else {
                listener.onSuccess(bitmap);
            }
        }, 100);
    }

    @Override
    public void getImage(final Context context, final Place place, final OnGetImageListener listener) {
        new Handler(Looper.myLooper()).postDelayed(() -> {
            Bitmap bitmap = getFromStorage(getFileForPlace(context, place));
            if (bitmap == null) {
                fetchFromRemote(context, place, listener);
            } else {
                listener.onSuccess(bitmap);
            }
        }, 100);
    }

    private Bitmap getFromStorage(File file) {
        try {
            if (file.exists()) {
                return BitmapFactory.decodeStream(new FileInputStream(file));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void fetchFromRemote(final Context context, final CustomUser user, final OnGetImageListener listener) {
        String imageUrl = user.getProfilePicUrl();
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(imageUrl, null, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headeilrs, byte[] responseBody) {
                Bitmap image = BitmapFactory.decodeByteArray(responseBody, 0, responseBody.length);
                listener.onSuccess(image);

                saveToLocal(image, context, user);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });
    }

    public void saveToLocal(Bitmap image, Context context, CustomUser user) {
        ImageLoaderWrite imageLoaderWrite = new ImageLoaderWriteImpl();
        imageLoaderWrite.writeImage(context, user, image, new ImageLoaderWrite.OnWriteImageListener() {
            @Override
            public void onSuccess() {

            }
        });
    }

    private void fetchFromRemote(final Context context, final Place place, final OnGetImageListener listener) {

        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(place.getId());
        long ONE_MEGABYTE = 1024 * 1024;
        storageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                listener.onSuccess(image);

                saveToLocal(image, context, place);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                listener.onFailure();
            }
        });
    }

    public void saveToLocal(Bitmap image, Context context, Place place) {
        ImageLoaderWrite imageLoaderWrite = new ImageLoaderWriteImpl();
        imageLoaderWrite.writeImage(context, place, image, new ImageLoaderWrite.OnWriteImageListener() {
            @Override
            public void onSuccess() {

            }
        });
    }

    public File getFileForUser(Context context, CustomUser user) {
        ContextWrapper cw = new ContextWrapper(context);
        File directory = cw.getDir(Constants.PROFILE_IMAGE_DIRECTORY, Context.MODE_PRIVATE);

        String fileName = user.getId();
        return new File(directory, fileName);
    }

    private File getFileForPlace(Context context, Place job) {
        ContextWrapper cw = new ContextWrapper(context);
        File directory = cw.getDir(Constants.PLACE_IMAGE_DIRECTORY, Context.MODE_PRIVATE);

        String fileName = job.getId();
        return new File(directory, fileName);
    }

}
