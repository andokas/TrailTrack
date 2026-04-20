package com.example.trailtrack.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.trailtrack.activities.MainActivity;
import com.example.trailtrack.utils.SessionManager;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ServicioFirebase extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        SessionManager sessionManager = new SessionManager(this);
        if (sessionManager.isLoggedIn()) {
            guardarTokenEnServidor(token, sessionManager.getId());
        }
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // Si el mensaje viene con datos
        if (remoteMessage.getData().size() > 0) {
            String titulo = remoteMessage.getData().get("titulo");
            String mensaje = remoteMessage.getData().get("mensaje");
            mostrarNotificacion(titulo, mensaje);
        }

        // Si el mensaje es una notificación
        if (remoteMessage.getNotification() != null) {
            String titulo = remoteMessage.getNotification().getTitle();
            String mensaje = remoteMessage.getNotification().getBody();
            mostrarNotificacion(titulo, mensaje);
        }
    }

    private void mostrarNotificacion(String titulo, String mensaje) {
        // Según PDF diapositiva 8: crear canal y notificación
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager)
                    getSystemService(Context.NOTIFICATION_SERVICE);

            NotificationChannel canal = new NotificationChannel(
                    "FCMCanal",
                    "Notificaciones TrailTrack",
                    NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(canal);

            Intent intent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

            NotificationCompat.Builder builder =
                    new NotificationCompat.Builder(this, "FCMCanal")
                            .setSmallIcon(android.R.drawable.ic_menu_compass)
                            .setContentTitle(titulo)
                            .setContentText(mensaje)
                            .setAutoCancel(true)
                            .setContentIntent(pendingIntent);

            manager.notify(3, builder.build());
        }
    }

    private void guardarTokenEnServidor(String token, int usuarioId) {
        Map<String, String> datos = new HashMap<>();
        datos.put("usuario_id", String.valueOf(usuarioId));
        datos.put("token", token);

        com.example.trailtrack.network.RetrofitClient.getApiService()
                .actualizarToken(datos)
                .enqueue(new Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(Call<Map<String, Object>> call,
                                           Response<Map<String, Object>> response) {
                    }

                    @Override
                    public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                    }
                });
    }
}