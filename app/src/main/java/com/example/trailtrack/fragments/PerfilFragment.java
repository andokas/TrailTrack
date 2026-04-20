package com.example.trailtrack.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.example.trailtrack.R;
import com.example.trailtrack.activities.EditarPerfilActivity;
import com.example.trailtrack.activities.LoginActivity;
import com.example.trailtrack.models.Ruta;
import com.example.trailtrack.network.ApiService;
import com.example.trailtrack.network.RetrofitClient;
import com.example.trailtrack.utils.AlarmUtils;
import com.example.trailtrack.utils.SessionManager;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PerfilFragment extends Fragment {

    private ImageView ivFotoPerfil;
    private TextView tvNombre, tvEmail, tvTotalRutas, tvTotalKm;
    private Button btnCerrarSesion, btnEditarPerfil;
    private SessionManager sessionManager;
    private ApiService apiService;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_perfil, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = new SessionManager(requireContext());
        apiService = RetrofitClient.getApiService();

        ivFotoPerfil = view.findViewById(R.id.ivFotoPerfil);
        tvNombre = view.findViewById(R.id.tvNombre);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvTotalRutas = view.findViewById(R.id.tvTotalRutas);
        tvTotalKm = view.findViewById(R.id.tvTotalKm);
        btnCerrarSesion = view.findViewById(R.id.btnCerrarSesion);
        btnEditarPerfil = view.findViewById(R.id.btnEditarPerfil);

        // Switch modo oscuro
        SwitchMaterial switchDarkMode = view.findViewById(R.id.switchDarkMode);

        // Comprobar el modo actual
        int currentMode = AppCompatDelegate.getDefaultNightMode();
        switchDarkMode.setChecked(currentMode == AppCompatDelegate.MODE_NIGHT_YES);

        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
            // Guardar preferencia
            requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)
                    .edit()
                    .putBoolean("dark_mode", isChecked)
                    .apply();
        });

        // Mostrar datos del usuario
        tvNombre.setText(sessionManager.getNombre());
        tvEmail.setText(sessionManager.getEmail());

        // Cargar foto de perfil con Glide
        String foto = sessionManager.getFoto();
        if (foto != null && !foto.isEmpty()) {
            Glide.with(requireContext())
                    .load(foto)
                    .circleCrop()
                    .placeholder(R.mipmap.ic_launcher)
                    .into(ivFotoPerfil);
        }

        cargarEstadisticas();

        btnEditarPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(requireContext(),
                        EditarPerfilActivity.class));
            }
        });

        btnCerrarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlarmUtils.cancelarAlarma(requireContext());
                sessionManager.cerrarSesion();
                Intent intent = new Intent(requireContext(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
    }

    private void cargarEstadisticas() {
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

                            int totalRutas = rutas.size();
                            float totalKm = 0f;
                            for (Ruta ruta : rutas) {
                                totalKm += ruta.getDistancia();
                            }

                            tvTotalRutas.setText(String.valueOf(totalRutas));
                            tvTotalKm.setText(String.format("%.2f", totalKm));
                        }
                    }

                    @Override
                    public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                        Toast.makeText(requireContext(),
                                "Error al cargar estadísticas",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        tvNombre.setText(sessionManager.getNombre());
        cargarEstadisticas();
    }
}