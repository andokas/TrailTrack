package com.example.trailtrack.models;

public class Usuario {
    private int id;
    private String nombre;
    private String email;
    private String password;
    private String foto_perfil;
    private String fcm_token;

    public Usuario() {}

    public Usuario(String nombre, String email, String password) {
        this.nombre = nombre;
        this.email = email;
        this.password = password;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFoto_perfil() { return foto_perfil; }
    public void setFoto_perfil(String foto_perfil) { this.foto_perfil = foto_perfil; }

    public String getFcm_token() { return fcm_token; }
    public void setFcm_token(String fcm_token) { this.fcm_token = fcm_token; }
}