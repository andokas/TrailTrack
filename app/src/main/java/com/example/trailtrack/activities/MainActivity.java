package com.example.trailtrack.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.trailtrack.R;
import com.example.trailtrack.fragments.RutasFragment;
import com.example.trailtrack.fragments.GrabarFragment;
import com.example.trailtrack.fragments.PerfilFragment;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private static final String KEY_SELECTED_TAB = "selected_tab";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Aplicar modo oscuro/claro antes de crear la actividad
        boolean darkMode = getSharedPreferences("settings", MODE_PRIVATE)
                .getBoolean("dark_mode", false);
        if (darkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_home);

        bottomNav = findViewById(R.id.bottomNav);

        // Restaurar pestaña activa si se recreó la actividad
        int selectedTab = R.id.nav_rutas;
        if (savedInstanceState != null) {
            selectedTab = savedInstanceState.getInt(KEY_SELECTED_TAB, R.id.nav_rutas);
        }

        // Cargar fragment según la pestaña guardada
        cargarFragmentPorId(selectedTab);
        bottomNav.setSelectedItemId(selectedTab);

        bottomNav.setOnItemSelectedListener(item -> {
            cargarFragmentPorId(item.getItemId());
            return true;
        });
    }

    private void cargarFragmentPorId(int id) {
        Fragment fragment;
        if (id == R.id.nav_rutas) {
            fragment = new RutasFragment();
        } else if (id == R.id.nav_grabar) {
            fragment = new GrabarFragment();
        } else if (id == R.id.nav_perfil) {
            fragment = new PerfilFragment();
        } else {
            fragment = new RutasFragment();
        }
        cargarFragment(fragment);
    }

    private void cargarFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    // Guardar la pestaña activa antes de recrear la actividad
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_SELECTED_TAB, bottomNav.getSelectedItemId());
    }
}