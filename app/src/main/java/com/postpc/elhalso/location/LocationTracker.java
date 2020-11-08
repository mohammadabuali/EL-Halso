package com.postpc.elhalso.location;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.postpc.elhalso.callbacks.LocationReceivedCallback;
import com.postpc.elhalso.data.LocationInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles the logic of tracking the user's location
 */
public class LocationTracker extends LocationCallback {

    private static final String TAG = "LocationTracker";

    private Context context;
    private Map<String, LocationReceivedCallback> callbacks;

    private boolean trackerReady = false;
    private boolean tracking = false;
    private LocationInfo lastLocation = null;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;

    public LocationTracker(Context context) {
        super();
        this.context = context;
        this.callbacks = new HashMap<>();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);

        createLocationRequest();
    }

    @Override
    public void onLocationResult(LocationResult locationResult) {
        if (locationResult == null) {
            return;
        }

        for (Location location : locationResult.getLocations()) {
            LocationInfo loc = new LocationInfo(location.getLatitude(), location.getLongitude(), location.getAccuracy());
            Log.d(TAG, "onLocationResult: Got location: " + loc);

            if (loc != lastLocation) {
                lastLocation = loc;
                for (LocationReceivedCallback callback : callbacks.values()) {
                    callback.onLocationReceived(loc);
                }
            }
        }
    }

    public void startTracking() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
        ) {
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, this, Looper.getMainLooper());
        tracking = true;
    }

    public void stopTracking() {
        fusedLocationProviderClient.removeLocationUpdates(this)
                .addOnSuccessListener(aVoid -> {

                });
        tracking = false;
    }

    private void createLocationRequest() {
        locationRequest = LocationRequest.create()
                .setInterval(3000)
                .setFastestInterval(1000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);

        SettingsClient client = LocationServices.getSettingsClient(context);
        client.checkLocationSettings(builder.build())
                .addOnSuccessListener(it -> {
                    trackerReady = true;
                }).addOnFailureListener(e -> {
            if (e instanceof ResolvableApiException) {
                try {
                    ((ResolvableApiException) e).startResolutionForResult((Activity) context, 1);
                } catch (IntentSender.SendIntentException ignored) {}
            }
        });
    }

    public boolean isTrackerReady() {
        return trackerReady && lastLocation != null;
    }

    public boolean isTracking() {
        return tracking;
    }

    public LocationInfo getLastLocation() {
        return lastLocation;
    }

    public void registerCallback(String tag, LocationReceivedCallback callback) {
        callbacks.remove(tag);
        callbacks.put(tag, callback);
    }

    public void clearCallback(String tag) {
        callbacks.remove(tag);
    }
}

