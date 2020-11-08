package com.postpc.elhalso;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.postpc.elhalso.adapters.FavMainRecyclerAdapter;
import com.postpc.elhalso.data.Business;
import com.postpc.elhalso.data.FavSection;
import com.postpc.elhalso.data.User;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class UserProfileActivity extends AppCompatActivity {
    private List<FavSection> sectionList = new ArrayList<>();

    private TextView numpadTextView;
    private SeekBar distanceSeekBar;

    private List<Business> businesses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_menu);


        setSupportActionBar((Toolbar) findViewById(R.id.user_profile_toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.favorites));


        Gson gson = new Gson();
        Type listType = new TypeToken<ArrayList<Business>>() {
        }.getType();
        businesses = gson.fromJson(getIntent().getStringExtra("businesses"), listType);

        TextView usernameTextView = findViewById(R.id.username_textView);
        distanceSeekBar = findViewById(R.id.radius_seekBar);
        numpadTextView = findViewById(R.id.numpad_textView);

        AppLoader appLoader = (AppLoader) getApplicationContext();
        usernameTextView.setText(getString(R.string.user_name, appLoader.getUser().getName()));

        setDistanceBar();

        initData();
        if (sectionList.size() > 0) {
            findViewById(R.id.no_fav_textView).setVisibility(View.INVISIBLE);
        }
        RecyclerView mainRecyclerView = findViewById(R.id.fav_recyclerView);
        FavMainRecyclerAdapter mainRecyclerAdapter = new FavMainRecyclerAdapter(sectionList, this);
        mainRecyclerView.setAdapter(mainRecyclerAdapter);
        mainRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.user_profile_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                ((AppLoader) getApplicationContext()).logout(this);
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setDistanceBar() {
        int maxRadius = getResources().getInteger(R.integer.max_radius_km) * 2;
        distanceSeekBar.setMin(1);
        distanceSeekBar.setMax(maxRadius);
        AppLoader context = (AppLoader) getApplicationContext();
        distanceSeekBar.setProgress((int) (context.getUser().getRadius() * 2f));
        numpadTextView.setText(getString(R.string.distance_km, (float) distanceSeekBar.getProgress() / 2));
        distanceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                ((AppLoader) getApplicationContext()).getUser().setRadius(progress);
                numpadTextView.setText(getString(R.string.distance_km, (float) progress / 2));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                ((AppLoader) getApplicationContext()).setRadius((double) seekBar.getProgress() / 2);
            }
        });
    }

    private void initData() {
        AppLoader appLoader = (AppLoader) getApplicationContext();

        String[] categories = getResources().getStringArray(R.array.categories);

        for (String category : categories) {
            List<Business> businesses = new ArrayList<>();
            for (Business business : getFavorites(appLoader.getUser())) {
                if (business.getCategory().equals(category)) {
                    businesses.add(business);
                }
            }
            if (businesses.size() > 0) {
                sectionList.add(new FavSection(category, businesses));
            }
        }
    }

    private List<Business> getFavorites(User user) {
        List<Business> favs = new ArrayList<>();
        for (Business b : businesses) {
            if (user.getFavorites().contains(b.getId())) {
                favs.add(b);
            }
        }
        return favs;
    }
}