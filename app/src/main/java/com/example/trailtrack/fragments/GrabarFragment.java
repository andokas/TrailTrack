package com.example.trailtrack.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.trailtrack.R;
import com.example.trailtrack.models.Ruta;
import com.example.trailtrack.network.ApiService;
import com.example.trailtrack.network.RetrofitClient;
import com.example.trailtrack.providers.RutasProvider;
import com.example.trailtrack.services.TrackingService;
import com.example.trailtrack.utils.DatabaseHelper;
import com.example.trailtrack.utils.SessionManager;

import android.content.ContentValues;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GrabarFragment extends Fragment {

    private MapView map;
    private MyLocationNewOverlay locationOverlay;
    private Polyline polyline;
    private Button btnIniciarParar;
    private TextView tvDistancia, tvTiempo;
    private boolean grabando = false;
    private List<GeoPoint> puntos = new ArrayList<>();
    private float distanciaTotal = 0f;
    private long tiempoInicio = 0;
    private Handler timerHandler = new Handler();
    private SessionManager sessionManager;
    private ApiService apiService;
    private int rutaId = -1;
    private ActivityResultLauncher<Intent> cameraLauncher;

    private BroadcastReceiver locationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            double lat = intent.getDoubleExtra(TrackingService.EXTRA_LAT, 0);
            double lng = intent.getDoubleExtra(TrackingService.EXTRA_LNG, 0);
            distanciaTotal = intent.getFloatExtra(TrackingService.EXTRA_DISTANCIA, 0);

            GeoPoint punto = new GeoPoint(lat, lng);
            puntos.add(punto);

            if (map != null) {
                polyline.setPoints(puntos);
                map.getController().animateTo(punto);
                map.invalidate();
            }

            tvDistancia.setText(String.format("%.2f km", distanciaTotal / 1000));
        }
    };

    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            long millis = SystemClock.elapsedRealtime() - tiempoInicio;
            int segundos = (int) (millis / 1000);
            int minutos = segundos / 60;
            int horas = minutos / 60;
            segundos = segundos % 60;
            minutos = minutos % 60;
            tvTiempo.setText(String.format("%02d:%02d:%02d", horas, minutos, segundos));
            timerHandler.postDelayed(this, 1000);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Cargar configuración OSMDroid antes de inflar el layout
        Context ctx = requireContext().getApplicationContext();
        Configuration.getInstance().load(ctx,
                PreferenceManager.getDefaultSharedPreferences(ctx));

        return inflater.inflate(R.layout.fragment_grabar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = new SessionManager(requireContext());
        apiService = RetrofitClient.getApiService();

        btnIniciarParar = view.findViewById(R.id.btnIniciarParar);
        tvDistancia = view.findViewById(R.id.tvDistancia);
        tvTiempo = view.findViewById(R.id.tvTiempo);

        // Configurar mapa
        map = view.findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.getZoomController().setVisibility(
                CustomZoomButtonsController.Visibility.ALWAYS);
        map.setMultiTouchControls(true);

        // Punto inicial centrado en Vitoria-Gasteiz
        GeoPoint startPoint = new GeoPoint(42.8467, -2.6731);
        map.getController().setZoom(15.0);
        map.getController().setCenter(startPoint);

        // Overlay de localización
        locationOverlay = new MyLocationNewOverlay(
                new GpsMyLocationProvider(requireContext()), map);
        locationOverlay.enableMyLocation();
        locationOverlay.enableFollowLocation();
        map.getOverlays().add(locationOverlay);

        // Inicializar polyline para dibujar la ruta
        polyline = new Polyline();
        polyline.getOutlinePaint().setColor(0xFF2E7D32);
        polyline.getOutlinePaint().setStrokeWidth(8f);
        map.getOverlays().add(polyline);

        // Configurar cámara
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == getActivity().RESULT_OK
                                && result.getData() != null) {
                            Bitmap bitmap = (Bitmap) result.getData()
                                    .getExtras().get("data");
                            subirFotoRuta(bitmap);
                        }
                    }
                });

        btnIniciarParar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!grabando) {
                    iniciarGrabacion();
                } else {
                    pararGrabacion();
                }
            }
        });
    }

    private void iniciarGrabacion() {
        if (ActivityCompat.checkSelfPermission(requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        grabando = true;
        puntos.clear();
        distanciaTotal = 0f;
        btnIniciarParar.setText("Parar Ruta");
        btnIniciarParar.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(0xFFD32F2F));

        Intent intent = new Intent(requireContext(), TrackingService.class);
        requireContext().startForegroundService(intent);

        IntentFilter filter = new IntentFilter(TrackingService.ACTION_LOCATION_UPDATE);
        requireContext().registerReceiver(locationReceiver, filter,
                Context.RECEIVER_NOT_EXPORTED);

        tiempoInicio = SystemClock.elapsedRealtime();
        timerHandler.postDelayed(timerRunnable, 0);
    }

    private void pararGrabacion() {
        grabando = false;
        btnIniciarParar.setText("Iniciar Ruta");
        btnIniciarParar.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(0xFF2E7D32));

        requireContext().stopService(
                new Intent(requireContext(), TrackingService.class));

        try {
            requireContext().unregisterReceiver(locationReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }

        timerHandler.removeCallbacks(timerRunnable);
        guardarRuta();
    }

    private void guardarRuta() {
        if (puntos.isEmpty()) {
            Toast.makeText(requireContext(),
                    "No hay puntos grabados", Toast.LENGTH_SHORT).show();
            return;
        }

        Ruta ruta = new Ruta();
        ruta.setUsuario_id(sessionManager.getId());
        ruta.setNombre("Ruta " + new java.util.Date().toString());
        ruta.setDistancia(distanciaTotal / 1000);

        long duracionSegundos = (SystemClock.elapsedRealtime() - tiempoInicio) / 1000;
        ruta.setDuracion((int) duracionSegundos);

        List<Ruta.Punto> listaPuntos = new ArrayList<>();
        for (GeoPoint geoPoint : puntos) {
            listaPuntos.add(new Ruta.Punto(
                    geoPoint.getLatitude(), geoPoint.getLongitude()));
        }
        ruta.setPuntos(listaPuntos);

        apiService.guardarRuta(ruta).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call,
                                   Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    rutaId = ((Double) response.body().get("id")).intValue();
                    Toast.makeText(requireContext(),
                            "Ruta guardada correctamente",
                            Toast.LENGTH_SHORT).show();

                    // Guardar también en SQLite local mediante Content Provider
                    ContentValues values = new ContentValues();
                    values.put(DatabaseHelper.COL_NOMBRE, ruta.getNombre());
                    values.put(DatabaseHelper.COL_DISTANCIA, ruta.getDistancia());
                    values.put(DatabaseHelper.COL_DURACION, ruta.getDuracion());
                    values.put(DatabaseHelper.COL_FECHA, new java.util.Date().toString());
                    requireContext().getContentResolver().insert(
                            RutasProvider.CONTENT_URI, values);

                    // Ofrecer hacer foto al terminar la ruta
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    cameraLauncher.launch(intent);
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(requireContext(),
                        "Error al guardar ruta: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void subirFotoRuta(Bitmap bitmap) {
        if (bitmap == null || rutaId == -1) return;

        try {
            File fotoFile = new File(requireContext().getCacheDir(), "foto_ruta.jpg");
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos);
            FileOutputStream fos = new FileOutputStream(fotoFile);
            fos.write(bos.toByteArray());
            fos.flush();
            fos.close();

            RequestBody usuarioIdBody = RequestBody.create(
                    MediaType.parse("text/plain"),
                    String.valueOf(sessionManager.getId()));
            RequestBody tipoBody = RequestBody.create(
                    MediaType.parse("text/plain"), "ruta");
            RequestBody fileBody = RequestBody.create(
                    MediaType.parse("image/jpeg"), fotoFile);
            MultipartBody.Part fotoPart = MultipartBody.Part.createFormData(
                    "foto", fotoFile.getName(), fileBody);

            apiService.subirFoto(usuarioIdBody, tipoBody, fotoPart)
                    .enqueue(new Callback<Map<String, Object>>() {
                        @Override
                        public void onResponse(Call<Map<String, Object>> call,
                                               Response<Map<String, Object>> response) {
                            Toast.makeText(requireContext(),
                                    "Foto guardada", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(Call<Map<String, Object>> call,
                                              Throwable t) {
                            Toast.makeText(requireContext(),
                                    "Error al subir foto",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        map.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        map.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (grabando) {
            requireContext().stopService(
                    new Intent(requireContext(), TrackingService.class));
            try {
                requireContext().unregisterReceiver(locationReceiver);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        timerHandler.removeCallbacks(timerRunnable);
    }
}