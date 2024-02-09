package com.example.manga_app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent nextActivity = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(nextActivity);
                overridePendingTransition(R.anim.zoom_in_from_center, R.anim.zoom_out_to_center);
                finish();
            }
        },3000);
    }
}