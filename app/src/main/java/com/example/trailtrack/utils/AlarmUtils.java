package com.example.trailtrack.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.example.trailtrack.receivers.RecordatorioBroadcastReceiver;

public class AlarmUtils {

    private static final int ALARM_ID = 100;

    public static void programarAlarma(Context context) {
        AlarmManager alarmManager = (AlarmManager)
                context.getSystemService(Context.ALARM_SERVICE);

        if (alarmManager == null) return;

        Intent intent = new Intent(context, RecordatorioBroadcastReceiver.class);
        intent.setAction("recordatorioRuta");

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, ALARM_ID, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        long triggerTime = System.currentTimeMillis() + (3 * 24 * 60 * 60 * 1000L);

        alarmManager.set(
                AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
    }

    public static void cancelarAlarma(Context context) {
        AlarmManager alarmManager = (AlarmManager)
                context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, RecordatorioBroadcastReceiver.class);
        intent.setAction("recordatorioRuta");

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, ALARM_ID, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        alarmManager.cancel(pendingIntent);
    }
}