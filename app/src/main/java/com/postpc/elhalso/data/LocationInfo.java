package com.postpc.elhalso.data;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.GeoPoint;

import java.util.Objects;

public class LocationInfo {
    private double latitude;
    private double longitude;
    private float accuracy;

    public LocationInfo(double latitude, double longitude, float accuracy) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.accuracy = accuracy;
    }
    public LatLng toLatLng() {
        return new LatLng(latitude, longitude);
    }
    public GeoPoint toGeoPoint() {
        return new GeoPoint(latitude, longitude);
    }

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

    public float getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LocationInfo that = (LocationInfo) o;
        return Double.compare(that.latitude, latitude) == 0 &&
                Double.compare(that.longitude, longitude) == 0 &&
                Float.compare(that.accuracy, accuracy) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude, accuracy);
    }

    @Override
    public String toString() {
        return "LocationInfo{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                ", accuracy=" + accuracy +
                '}';
    }
}
