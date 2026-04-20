package com.example.trailtrack.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

public class TrackingService extends Service {

    public static final String CHANNEL_ID = "TrackingChannel";
    public static final String ACTION_LOCATION_UPDATE = "com.tunombre.trailtrack.LOCATION_UPDATE";
    public static final String EXTRA_LAT = "lat";
    public static final String EXTRA_LNG = "lng";
    public static final String EXTRA_DISTANCIA = "distancia";

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private Location ultimaLocalizacion = null;
    private float distanciaTotal = 0f;

    @Override
    public void onCreate() {
        super.onCreate();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        crearCanalNotificacion();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(1, crearNotificacion());
        iniciarTracking();
        return START_STICKY;
    }

    private void iniciarTracking() {
        LocationRequest locationRequest = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, 3000)
                .setMinUpdateIntervalMillis(2000)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) return;

                for (Location location : locationResult.getLocations()) {
                    // Calcular distancia acumulada
                    if (ultimaLocalizacion != null) {
                        distanciaTotal += ultimaLocalizacion.distanceTo(location);
                    }
                    ultimaLocalizacion = location;

                    // Enviar broadcast con la nueva posición
                    Intent broadcastIntent = new Intent(ACTION_LOCATION_UPDATE);
                    broadcastIntent.putExtra(EXTRA_LAT, location.getLatitude());
                    broadcastIntent.putExtra(EXTRA_LNG, location.getLongitude());
                    broadcastIntent.putExtra(EXTRA_DISTANCIA, distanciaTotal);
                    sendBroadcast(broadcastIntent);
                }
            }
        };

        try {
            fusedLocationClient.requestLocationUpdates(
                    locationRequest, locationCallback, Looper.getMainLooper());
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void crearCanalNotificacion() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Tracking de ruta",
                NotificationManager.IMPORTANCE_LOW);
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);
    }

    private Notification crearNotificacion() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("TrailTrack")
                .setContentText("Grabando ruta...")
                .setSmallIcon(android.R.drawable.ic_menu_compass)
                .setOngoing(true)
                .build();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}