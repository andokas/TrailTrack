package com.example.trailtrack.models;

import java.util.List;

public class Ruta {
    private int id;
    private int usuario_id;
    private String nombre;
    private float distancia;
    private int duracion;
    private String fecha;
    private List<Punto> puntos;
    private List<String> fotos;

    public static class Punto {
        private double lat;
        private double lng;

        public Punto(double lat, double lng) {
            this.lat = lat;
            this.lng = lng;
        }

        public double getLat() { return lat; }
        public double getLng() { return lng; }
    }

    public Ruta() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUsuario_id() { return usuario_id; }
    public void setUsuario_id(int usuario_id) { this.usuario_id = usuario_id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public float getDistancia() { return distancia; }
    public void setDistancia(float distancia) { this.distancia = distancia; }

    public int getDuracion() { return duracion; }
    public void setDuracion(int duracion) { this.duracion = duracion; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public List<Punto> getPuntos() { return puntos; }
    public void setPuntos(List<Punto> puntos) { this.puntos = puntos; }

    public List<String> getFotos() { return fotos; }
    public void setFotos(List<String> fotos) { this.fotos = fotos; }
}