package com.yatenesturno.functionality.push_notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;

import com.google.android.gms.common.api.Api;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.loopj.android.http.AsyncHttpClient;
import com.yatenesturno.R;
import com.yatenesturno.activities.appointment_view.MonthViewActivity;
import com.yatenesturno.activities.main_screen.MainActivity;
import com.yatenesturno.utils.CalendarUtils;

import java.util.Map;

public class AppointmentNotificationService extends FirebaseMessagingService {

    public static final String YA_TENES_TURNO_ID = "YaTenesTurno";
    private static int count = 0;

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);

        saveToken(token);
    }

    private void saveToken(String token) {


        PushNotificationHandler.saveTokenToRemote(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        try {
            Map<String, String> data = remoteMessage.getData();

            NotificationCompat.Builder builder = null;

            switch (data.get("type")) {
                case "cancel":
                    builder = buildCancelNotification(data);
                    break;
                case "confirm":
                    builder = buildConfirmationNotification(data);
                    break;
            }

            NotificationManager mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            if (isOreoOrGreater()) {
                int importance = NotificationManager.IMPORTANCE_HIGH;
                NotificationChannel mChannel = new NotificationChannel(YA_TENES_TURNO_ID, YA_TENES_TURNO_ID, importance);
                mChannel.setDescription(data.get("body"));
                mChannel.enableVibration(true);
                mChannel.setVibrationPattern(new long[]{0, 250, 250, 250});

                mNotifyManager.createNotificationChannel(mChannel);
            }

            mNotifyManager.notify(count, builder.build());
            count++;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private NotificationCompat.Builder buildCancelNotification(Map<String, String> data) {
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, YA_TENES_TURNO_ID);

        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);

        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0,
                notificationIntent, 0);

        builder.setContentTitle(data.get("title"))
                .setContentText(data.get("body"))
                .setSmallIcon(R.drawable.logo)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.logo))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setSound(defaultSoundUri)
                .setColor(getColor(R.color.colorPrimary))
                .setChannelId(YA_TENES_TURNO_ID)
                .setPriority(NotificationCompat.PRIORITY_HIGH).build();

        return builder;
    }

    private NotificationCompat.Builder buildConfirmationNotification(Map<String, String> data) {
        Intent intent = new Intent(getApplicationContext(), MonthViewActivity.class);

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        Bundle bundle = new Bundle();
        bundle.putBoolean("fromPush", true);
        bundle.putString("appointmentId", data.get("appointment_id"));
        bundle.putString("jobId", data.get("job_id"));
        bundle.putString("placeId", data.get("place_id"));
        bundle.putSerializable("selectedDay", CalendarUtils.parseDateTime(data.get("date")));

        intent.putExtras(bundle);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(intent);
        Intent mainActivityIntent = stackBuilder.editIntentAt(0);
        mainActivityIntent.putExtras(bundle);

        PendingIntent pendingIntent =
                stackBuilder.getPendingIntent(Integer.parseInt(data.get("appointment_id")), PendingIntent.FLAG_UPDATE_CURRENT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, YA_TENES_TURNO_ID);
        builder.setContentTitle(data.get("title"))
                .setContentText(data.get("body"))
                .setSmallIcon(R.drawable.logo)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.logo))
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setColor(getColor(R.color.colorPrimary))
                .setContentIntent(pendingIntent)
                .setChannelId(YA_TENES_TURNO_ID)
                .setPriority(NotificationCompat.PRIORITY_HIGH).build();

        return builder;
    }

    private boolean isOreoOrGreater() {
        return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O;
    }

}