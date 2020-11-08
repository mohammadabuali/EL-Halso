package com.postpc.elhalso;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.postpc.elhalso.adapters.BusinessListAdapter;
import com.postpc.elhalso.callbacks.OnBusinessClick;
import com.postpc.elhalso.data.Business;
import com.postpc.elhalso.data.User;
import com.postpc.elhalso.data.LocationInfo;
import com.postpc.elhalso.utils.Utils;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BusinessListActivity extends AppCompatActivity implements OnBusinessClick {

    private static final String TAG = "BusinessListActivity";

    private BusinessListAdapter adapter;
    private RecyclerView recyclerView;
    private List<Business> businesses;
    private String category;
    private LocationInfo locationInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_list);

        Gson gson = new Gson();
        String payload = getIntent().getStringExtra("business_list");
        if (payload == null || payload.length() == 0) {
            payload = "[]";
        }

        Type listType = new TypeToken<ArrayList<Business>>() {
        }.getType();
        businesses = gson.fromJson(payload, listType);
        payload = getIntent().getStringExtra("user_location");
        locationInfo = null;
        if (payload != null && payload.length() > 0) {
            locationInfo = gson.fromJson(payload, LocationInfo.class);
        }

        category = getIntent().getStringExtra("cat_name");

        setSupportActionBar(findViewById(R.id.user_toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(category);

        recyclerView = findViewById(R.id.business_list_recycler);
        adapter = new BusinessListAdapter(locationInfo, this);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        populateList();
    }

    private void populateList() {
        User user = ((AppLoader) getApplicationContext()).getUser();
        if (user == null) {
            return;
        }
        for (Business business : businesses) {
            if (user.getFavorites().contains(business.getId())) {
                adapter.mValues.add(business);
            }
        }
        for (Business business : businesses) {
            if (!user.getFavorites().contains(business.getId())) {
                adapter.mValues.add(business);
            }
        }
        adapter.mValues.sort(businessComparator);
        adapter.notifyDataSetChanged();
    }

    private Comparator<Business> businessComparator = (b1, b2) -> {
        User user = ((AppLoader) getApplicationContext()).getUser();
        int favCompare = Boolean.compare(user.getFavorites().contains(b1.getId()), user.getFavorites().contains(b2.getId()));
        if (favCompare != 0 || locationInfo == null) {
            return -favCompare;
        }

        LatLng loc1;
        LatLng loc2;
        try {
            loc1 = new LatLng(b1.getCoordinates().getLatitude(), b1.getCoordinates().getLongitude());
            loc2 = new LatLng(b2.getCoordinates().getLatitude(), b2.getCoordinates().getLongitude());
        } catch (NullPointerException e) { // either b1 or b2 does not have a location
            if (b1.getCoordinates() != null) {
                return 1;
            } else if (b2.getCoordinates() != null) {
                return -1;
            } else {
                return -Float.compare(b1.getReviewsScore(), b2.getReviewsScore());
            }
        }
        float dist1 = Utils.distanceBetween(locationInfo.toLatLng(), loc1);
        float dist2 = Utils.distanceBetween(locationInfo.toLatLng(), loc2);

        int distCompare = Float.compare(dist1, dist2);
        if (distCompare != 0) {
            return distCompare;
        } else {
            return -Float.compare(b1.getReviewsScore(), b2.getReviewsScore());
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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
                ((AppLoader) getApplicationContext()).openProfile(this, businesses);
                break;
            case android.R.id.home:
                onBackPressed();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBusinessClick(Business business) {
        Intent intent = new Intent(this, BusinessActivity.class);
        intent.putExtra("business", business);
        startActivity(intent);
    }
}