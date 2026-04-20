package com.example.trailtrack.receivers;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.trailtrack.activities.MainActivity;

public class RecordatorioBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if ("recordatorioRuta".equals(intent.getAction())) {
            mostrarNotificacion(context);
        }
    }

    private void mostrarNotificacion(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager)
                    context.getSystemService(Context.NOTIFICATION_SERVICE);

            NotificationChannel canal = new NotificationChannel(
                    "RecordatorioCanal",
                    "Recordatorio de actividad",
                    NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(canal);

            Intent activityIntent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context, 0, activityIntent,
                    PendingIntent.FLAG_IMMUTABLE);

            NotificationCompat.Builder builder =
                    new NotificationCompat.Builder(context, "RecordatorioCanal")
                            .setSmallIcon(android.R.drawable.ic_menu_compass)
                            .setContentTitle("¡Te echamos de menos!")
                            .setContentText("Llevas varios días sin grabar una ruta. ¡Sal a correr!")
                            .setAutoCancel(true)
                            .setContentIntent(pendingIntent);

            Notification notification = builder.build();
            manager.notify(2, notification);
        }
    }
}