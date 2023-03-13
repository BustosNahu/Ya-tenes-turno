package com.yatenesturno.database;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.yatenesturno.Constants;
import com.yatenesturno.database.interfaces.ImageLoaderWrite;
import com.yatenesturno.object_interfaces.CustomUser;
import com.yatenesturno.object_interfaces.Place;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageLoaderWriteImpl implements ImageLoaderWrite {

    @Override
    public void writeImage(Context context, CustomUser user, Bitmap image, OnWriteImageListener listener) {
        File file = getFileForUser(context, user);
        save(file, image, listener);
    }

    @Override
    public void writeImage(Context context, Place place, Bitmap image, OnWriteImageListener listener) {
        File file = getFileForPlace(context, place);
        save(file, image, listener);
    }

    @Override
    public void invalidateImage(Context context, CustomUser user, OnInvalidateImageListener listener) {
        File file = getFileForUser(context, user);
        if (file.delete()) {
            listener.onSuccess();
        } else {
            listener.onFailure();
        }
    }

    @Override
    public void invalidateImage(Context context, Place place, OnInvalidateImageListener listener) {
        File file = getFileForPlace(context, place);

        if (file.delete()) {
            listener.onSuccess();
        } else {
            listener.onFailure();
        }
    }

    @Override
    public void writeImageToRemote(Context context, String placeId, Uri uri, final OnWriteImageListener listener) {
        StorageReference reference = FirebaseStorage.getInstance().getReference().child(placeId);

        Bitmap bitmap = compressImage(context, uri);
        bitmap = rotateBitmap(context, bitmap, uri);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        bitmap.recycle();

        reference.putBytes(byteArray).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                listener.onSuccess();
            }
        });
    }

    private Bitmap rotateBitmap(Context context, Bitmap bitmap, Uri uri) {
        Matrix matrix = new Matrix();
        int rotation = getOrientation(context, uri);
        matrix.postRotate(rotation);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private int getOrientation(Context context, Uri photoUri) {
        Cursor cursor = context.getContentResolver().query(photoUri,
                new String[]{MediaStore.Images.ImageColumns.ORIENTATION}, null, null, null);

        if (cursor.getCount() != 1) {
            cursor.close();
            return -1;
        }

        cursor.moveToFirst();
        int orientation = cursor.getInt(0);
        cursor.close();
        return orientation;
    }

    public Bitmap compressImage(Context context, Uri uri) {
        InputStream input;
        Bitmap bitmap;
        try {
            input = context.getContentResolver().openInputStream(uri);
            bitmap = BitmapFactory.decodeStream(input);
            input.close();

            int oneMegaByte = 1000 * 1000;
            while (bitmap.getByteCount() > oneMegaByte) {
                bitmap = Bitmap.createScaledBitmap(bitmap,
                        (int) (bitmap.getWidth() * 0.8),
                        (int) (bitmap.getHeight() * 0.8),
                        true);
            }
            return bitmap;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void save(final File file, final Bitmap image, final OnWriteImageListener listener) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(file);
                    image.compress(Bitmap.CompressFormat.PNG, 100, fos);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    listener.onSuccess();
                }
            }
        }, 100);
    }

    public File getFileForUser(Context context, CustomUser user) {
        ContextWrapper cw = new ContextWrapper(context);
        File directory = cw.getDir(Constants.PROFILE_IMAGE_DIRECTORY, Context.MODE_PRIVATE);

        String fileName = user.getId();
        return new File(directory, fileName);
    }

    private File getFileForPlace(Context context, Place place) {
        ContextWrapper cw = new ContextWrapper(context);
        File directory = cw.getDir(Constants.PLACE_IMAGE_DIRECTORY, Context.MODE_PRIVATE);

        String fileName = place.getId();
        return new File(directory, fileName);
    }

}
