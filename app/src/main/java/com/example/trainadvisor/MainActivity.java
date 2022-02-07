package com.example.trainadvisor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final CardView trainView = findViewById(R.id.trainView);
        final CardView routeView = findViewById(R.id.routeView);
        final CardView lineStatView = findViewById(R.id.lineStatView);

        trainView.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, TrainSpaceActivity2.class)));
        routeView.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, RouteActivity.class)));
        lineStatView.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, LineStatusActivity.class)));
    }
}