package com.example.trailtrack.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "TrailTrackSession";
    private static final String KEY_ID = "id";
    private static final String KEY_NOMBRE = "nombre";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_FOTO = "foto_perfil";
    private static final String KEY_LOGGED_IN = "isLoggedIn";

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void guardarSesion(int id, String nombre, String email, String foto) {
        editor.putBoolean(KEY_LOGGED_IN, true);
        editor.putInt(KEY_ID, id);
        editor.putString(KEY_NOMBRE, nombre);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_FOTO, foto);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_LOGGED_IN, false);
    }

    public int getId() {
        return prefs.getInt(KEY_ID, -1);
    }

    public String getNombre() {
        return prefs.getString(KEY_NOMBRE, null);
    }

    public String getEmail() {
        return prefs.getString(KEY_EMAIL, null);
    }

    public String getFoto() {
        return prefs.getString(KEY_FOTO, null);
    }

    public void cerrarSesion() {
        editor.clear();
        editor.apply();
    }
}