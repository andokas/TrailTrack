package com.example.trailtrack.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.example.trailtrack.R;
import com.example.trailtrack.activities.DetalleRutaActivity;
import com.example.trailtrack.adapters.RutasAdapter;
import com.example.trailtrack.models.Ruta;
import com.example.trailtrack.network.ApiService;
import com.example.trailtrack.network.RetrofitClient;
import com.example.trailtrack.utils.SessionManager;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RutasFragment extends Fragment {

    private RecyclerView rvRutas;
    private RutasAdapter adapter;
    private List<Ruta> listaRutas = new ArrayList<>();
    private SessionManager sessionManager;
    private ApiService apiService;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_rutas, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = new SessionManager(requireContext());
        apiService = RetrofitClient.getApiService();

        rvRutas = view.findViewById(R.id.rvRutas);
        rvRutas.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new RutasAdapter(requireContext(), listaRutas,
                new RutasAdapter.OnRutaClickListener() {
                    @Override
                    public void onRutaClick(Ruta ruta) {
                        Intent intent = new Intent(requireContext(),
                                DetalleRutaActivity.class);
                        intent.putExtra("ruta_id", ruta.getId());
                        startActivity(intent);
                    }
                });
        rvRutas.setAdapter(adapter);

        cargarRutas();
    }

    private void cargarRutas() {
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

                            listaRutas.clear();
                            listaRutas.addAll(rutas);
                            adapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                        Toast.makeText(requireContext(),
                                "Error al cargar rutas: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        cargarRutas();
    }
}