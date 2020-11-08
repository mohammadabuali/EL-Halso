package com.postpc.elhalso;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.postpc.elhalso.callbacks.BusinessListReadyCallback;
import com.postpc.elhalso.callbacks.OnBusinessesReady;
import com.postpc.elhalso.data.Business;
import com.postpc.elhalso.data.User;
import com.postpc.elhalso.fragments.CategoriesFragment;
import com.postpc.elhalso.fragments.MapsFragment;
import com.postpc.elhalso.data.LocationInfo;
import com.postpc.elhalso.location.LocationTracker;
import com.postpc.elhalso.utils.Utils;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainMapActivity extends AppCompatActivity implements BusinessListReadyCallback {

    private static final String TAG = "MainMapActivity";

    public LocationTracker locationTracker;

    private Menu menu;

    public Map<String, OnBusinessesReady> callbacks; // callbacks for when businesses list changed
    public List<Business> businessList;

    // Fragments
    private final Fragment mapFragment = new MapsFragment();
    private final Fragment catsFragment = CategoriesFragment.newInstance(1);
    private Fragment active = mapFragment;

    private final FragmentManager fm = getSupportFragmentManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_map);

        setSupportActionBar((Toolbar) findViewById(R.id.user_toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setTitle(getResources().getString(R.string.app_name));

        BottomNavigationView navigation = findViewById(R.id.nav_view);
        fm.beginTransaction().add(R.id.nav_container, catsFragment, "cats").hide(catsFragment).commit();
        fm.beginTransaction().add(R.id.nav_container, mapFragment, "map").commit();

        navigation.setOnNavigationItemSelectedListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.navigation_map:
                    fm.beginTransaction().hide(active).show(mapFragment).commit();
                    active = mapFragment;
                    return true;
                case R.id.navigation_list:
                    fm.beginTransaction().hide(active).show(catsFragment).commit();
                    active = catsFragment;
                    return true;
                default:
                    return false;
            }
        });
        locationTracker = new LocationTracker(this);

        callbacks = new HashMap<>();
        FirebaseHandler.getInstance().businessListener(this);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.main_screen_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                ((AppLoader) getApplicationContext()).logout(this);
                break;
            case R.id.action_settings:
                ((AppLoader) getApplicationContext()).openProfile(this, businessList);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        locationTracker.startTracking();
    }

    @Override
    public void onPause() {
        super.onPause();
        locationTracker.stopTracking();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        locationTracker.stopTracking();
        callbacks.clear();
    }

    /**
     * Returns a filtered list of the available business with a certain radius
     */
    public List<Business> filterBusinesses() {
        List<Business> filtered = new ArrayList<>();
        User user = ((AppLoader) getApplicationContext()).getUser();
        if (user == null || businessList == null || locationTracker == null || locationTracker.getLastLocation() == null) {
            return filtered;
        }
        LocationInfo location = locationTracker.getLastLocation();
        for (Business business : businessList) {
            GeoPoint bLoc = business.getCoordinates();
            if (bLoc == null) {
                continue;
            }
            float distance = Utils.distanceBetween(new LatLng(bLoc.getLatitude(), bLoc.getLongitude()), location.toLatLng());
            if (distance < user.getRadius() * 1000) {
                filtered.add(business);
            }
        }
        return filtered;
    }

    @Override
    public void onBusinessListReady(List<Business> businessList) {
        this.businessList = businessList;
        for (OnBusinessesReady callback : callbacks.values()) {
            callback.call();
        }
    }
}