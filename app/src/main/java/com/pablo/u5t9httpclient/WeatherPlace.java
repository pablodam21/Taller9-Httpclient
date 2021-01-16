package com.pablo.u5t9httpclient;

public class WeatherPlace {

    private double lat;

    private double longi;

    private double humedad;

    private double temperatura;

    private String descripcion;

    public WeatherPlace(double lat, double longi, double humedad, double temperatura, String descripcion) {
        this.lat = lat;
        this.longi = longi;
        this.humedad = humedad;
        this.temperatura = temperatura;
        this.descripcion = descripcion;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLongi() {
        return longi;
    }

    public void setLongi(double longi) {
        this.longi = longi;
    }

    public double getHumedad() {
        return humedad;
    }

    public void setHumedad(double humedad) {
        this.humedad = humedad;
    }

    public double getTemperatura() {
        return temperatura;
    }

    public void setTemperatura(double temperatura) {
        this.temperatura = temperatura;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    @Override
    public String toString() {
        return "WeatherPlace{" +
                "lat=" + lat + ", longi=" + longi + '\'' +
                ", humedad=" + humedad + '\'' +
                ", temperatura=" + temperatura + '\'' +
                ", descripcion='" + descripcion + '\'' +
                '}';
    }
}
