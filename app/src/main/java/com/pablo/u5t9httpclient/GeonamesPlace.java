package com.pablo.u5t9httpclient;

public class GeonamesPlace {

    private String descripcion;

    private int latitud;

    private int longitud;

    public GeonamesPlace(String descripcion, int latitud, int longitud) {
        this.descripcion = descripcion;
        this.latitud = latitud;
        this.longitud = longitud;
    }

    public int getLongitud() {
        return longitud;
    }

    public int getLatitud() {
        return latitud;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setLongitud(int longitud) {
        this.longitud = longitud;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setLatitud(int latitud) {
        this.latitud = latitud;
    }



    @Override
    public String toString() {
        return  descripcion + ", " +
                '\'' + "latitud=" + latitud +
                ", longitud=" + longitud;
    }
}
