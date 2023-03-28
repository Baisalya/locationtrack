package com.example.gpstask;

public class LocationModel {
    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public LocationModel(double latitude, double longitude, float speed) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.speed = speed;
    }

    private double latitude;
    private double longitude;
    private float speed;
    public LocationModel() {}


}
