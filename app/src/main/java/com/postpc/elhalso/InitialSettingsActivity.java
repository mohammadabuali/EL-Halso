package com.postpc.elhalso;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.postpc.elhalso.data.User;

public class InitialSettingsActivity extends AppCompatActivity {
    TextView tv;
    TextView radiusIndicator;
    SeekBar sb;
    Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial_settings);

        User user = ((AppLoader) getApplicationContext()).getUser();

        radiusIndicator = (TextView) findViewById(R.id.radius_indicator_textView);
        radiusIndicator.setText(getString(R.string.text_radius_indicator, user.getRadius()));
        sb = findViewById(R.id.distance_seekBar);
        int max = getResources().getInteger(R.integer.max_radius_km) * 2;
        sb.setMin(1);
        sb.setMax(max);
        sb.setProgress((int) (user.getRadius() * 2), true);
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float radius = (float) progress / 2f;
                radiusIndicator.setText(getString(R.string.text_radius_indicator, radius));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        btn = (Button) findViewById(R.id.rad_btn);
        btn.setOnClickListener((View.OnClickListener) v -> {
            AppLoader appLoader = (AppLoader) getApplicationContext();
            appLoader.setRadius((double) sb.getProgress() / 2);
            appLoader.getUser().setFirstLogin(true);
            FirebaseHandler.getInstance().updateUserFirstLogin(appLoader.getUser());
            Intent intent;
            intent = new Intent((AppLoader) getApplicationContext(), MainMapActivity.class);
            startActivity(intent);
            finish();
        });
    }


}