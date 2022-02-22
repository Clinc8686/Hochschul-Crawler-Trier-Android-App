package de.clinc8686.hochschul_crawler;

import static android.app.Notification.DEFAULT_SOUND;
import static android.app.Notification.DEFAULT_VIBRATE;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.service.notification.StatusBarNotification;

import androidx.core.app.NotificationCompat;

public class Notification {
    private final Context context;

    Notification(Context context, String channel, String semester, String mod) {
        this.context = context;

        NotificationChannel notificationChannel;
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context.getApplicationContext(), channel)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setDefaults(DEFAULT_SOUND | DEFAULT_VIBRATE)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC).setContentIntent(PendingIntent.getActivity(this.context, 0, new Intent(this.context, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT));

        NotificationManager manager = (context.getApplicationContext().getSystemService(NotificationManager.class));

        switch (channel) {
            case "Hochschul Crawler":
                notificationChannel = new NotificationChannel(channel, channel, NotificationManager.IMPORTANCE_DEFAULT);
                notificationChannel.setShowBadge(false);
                manager.createNotificationChannel(notificationChannel);

                builder.setContentTitle(context.getString(R.string.NotificationTitleService))
                        .setContentText(context.getString(R.string.NotificationTextService))
                        .setOngoing(true);
                        //.setStyle(new NotificationCompat.BigTextStyle().bigText("QIS Hochschul Crawler läuft im Hintergrund."));

                manager.notify(54295, builder.build());
                break;
            case "Neue Noten":
                notificationChannel = new NotificationChannel(channel, channel, NotificationManager.IMPORTANCE_HIGH);
                manager.createNotificationChannel(notificationChannel);

                builder.setContentTitle("QIS Hochschul Crawler")
                        .setContentText("Es sind neue Noten verfügbar!")
                        .setStyle(new NotificationCompat.BigTextStyle().bigText("Es sind neue Noten für das " + semester + " in " + mod + " verfügbar!"));

                manager.notify(54296, builder.build());
                break;
            case "Prüfe neue Noten":
                notificationChannel = new NotificationChannel(channel, channel, NotificationManager.IMPORTANCE_LOW);
                notificationChannel.setShowBadge(false);
                manager.createNotificationChannel(notificationChannel);

                builder.setContentTitle("Prüfe auf neue Noten")
                        .setProgress(0, 0, true);

                manager.notify(54297, builder.build());
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + channel);
        }
    }

    public static void cancelAllNotifications(Context context) {
        new NotificationChannel("Hochschul-Crawler", "Hochschul-Crawler", NotificationManager.IMPORTANCE_HIGH);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(54295);
        notificationManager.cancel(54296);
        notificationManager.cancel(54297);
    }

    public void closeNotification() {
        NotificationManager notificationManager = (NotificationManager) (context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE));
        notificationManager.cancel(54297);
    }

    public static boolean isNotificationVisible(Context context) {
        boolean isRunning = false;
        NotificationManager notificationManager = (context.getApplicationContext().getSystemService(NotificationManager.class));
        StatusBarNotification[] sbn = notificationManager.getActiveNotifications();
        for (StatusBarNotification notification : sbn) {
            if (notification.getNotification().getChannelId().equals("Hochschul-Crawler")) {
                isRunning = true;
            }
        }
        return isRunning;
    }
}
