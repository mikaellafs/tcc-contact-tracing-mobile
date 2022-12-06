package pg.contact_tracing.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import pg.contact_tracing.ui.activities.MainActivity;

public class NotificationCreator {
    public static int pushNotificationId = 3;
    public static Notification foregroundServiceNotification = null;

    public Notification createNotification(Context context, String channelId, String channelName, String title, String subtitle, Integer icon) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
            createNotificationChannel(context, channelId, channelName);

        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,
                0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setContentTitle(title)
                .setContentText(subtitle)
                .setContentIntent(pendingIntent);

        return icon != null ?
                builder.setSmallIcon(icon).build()
                : builder.build();
    }

    private void createNotificationChannel(Context context, String channelId, String channelName) {
        NotificationChannel serviceChannel = new NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
        );
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        manager.createNotificationChannel(serviceChannel);
    }
}
