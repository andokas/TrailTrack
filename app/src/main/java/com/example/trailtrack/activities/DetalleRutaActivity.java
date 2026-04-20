package com.example.trailtrack.activities;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.example.trailtrack.R;
import com.example.trailtrack.adapters.FotosAdapter;
import com.example.trailtrack.models.Ruta;
import com.example.trailtrack.network.ApiService;
import com.example.trailtrack.network.RetrofitClient;
import com.example.trailtrack.utils.SessionManager;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polyline;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetalleRutaActivity extends AppCompatActivity {

    private MapView mapDetalle;
    private TextView tvNombre, tvDistancia, tvDuracion, tvFecha;
    private RecyclerView rvFotos;
    private ApiService apiService;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Cargar configuración OSMDroid
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx,
                PreferenceManager.getDefaultSharedPreferences(ctx));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_ruta);

        apiService = RetrofitClient.getApiService();
        sessionManager = new SessionManager(this);

        tvNombre = findViewById(R.id.tvNombreDetalle);
        tvDistancia = findViewById(R.id.tvDistanciaDetalle);
        tvDuracion = findViewById(R.id.tvDuracionDetalle);
        tvFecha = findViewById(R.id.tvFechaDetalle);
        rvFotos = findViewById(R.id.rvFotosDetalle);

        // Configurar mapa
        mapDetalle = findViewById(R.id.mapDetalle);
        mapDetalle.setTileSource(TileSourceFactory.MAPNIK);
        mapDetalle.setMultiTouchControls(true);

        // Configurar RecyclerView de fotos horizontal
        rvFotos.setLayoutManager(new LinearLayoutManager(
                this, LinearLayoutManager.HORIZONTAL, false));

        // Cargar rutas y encontrar la seleccionada
        int rutaId = getIntent().getIntExtra("ruta_id", -1);
        if (rutaId != -1) {
            cargarRuta(rutaId);
        }
    }

    private void cargarRuta(int rutaId) {
        apiService.getMisRutas(sessionManager.getId())
                .enqueue(new Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(Call<Map<String, Object>> call,
                                           Response<Map<String, Object>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Gson gson = new Gson();
                            Type tipoLista = new TypeToken<List<Ruta>>() {}.getType();
                            String json = gson.toJson(response.body().get("rutas"));
                            List<Ruta> rutas = gson.fromJson(json, tipoLista);

                            for (Ruta ruta : rutas) {
                                if (ruta.getId() == rutaId) {
                                    mostrarRuta(ruta);
                                    break;
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                        Toast.makeText(DetalleRutaActivity.this,
                                "Error al cargar ruta", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void mostrarRuta(Ruta ruta) {
        tvNombre.setText(ruta.getNombre());
        tvDistancia.setText(String.format("%.2f km", ruta.getDistancia()));

        int duracion = ruta.getDuracion();
        int horas = duracion / 3600;
        int minutos = (duracion % 3600) / 60;
        int segundos = duracion % 60;
        tvDuracion.setText(String.format("%02d:%02d:%02d", horas, minutos, segundos));
        tvFecha.setText(ruta.getFecha());

        // Dibujar ruta en el mapa
        if (ruta.getPuntos() != null && !ruta.getPuntos().isEmpty()) {
            List<GeoPoint> geoPoints = new ArrayList<>();
            for (Ruta.Punto punto : ruta.getPuntos()) {
                geoPoints.add(new GeoPoint(punto.getLat(), punto.getLng()));
            }

            Polyline polyline = new Polyline();
            polyline.setPoints(geoPoints);
            polyline.getOutlinePaint().setColor(0xFF2E7D32);
            polyline.getOutlinePaint().setStrokeWidth(8f);
            mapDetalle.getOverlays().add(polyline);

            // Centrar mapa en la ruta
            mapDetalle.zoomToBoundingBox(
                    BoundingBox.fromGeoPoints(geoPoints), true, 100);
            mapDetalle.invalidate();
        }

        // Mostrar fotos si las hay
        if (ruta.getFotos() != null && !ruta.getFotos().isEmpty()) {
            FotosAdapter fotosAdapter = new FotosAdapter(this, ruta.getFotos());
            rvFotos.setAdapter(fotosAdapter);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mapDetalle.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapDetalle.onPause();
    }
}