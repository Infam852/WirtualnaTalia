package com.example.wirtualnatalia.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


import com.example.wirtualnatalia.R;
import com.example.wirtualnatalia.cloudanchor.CloudAnchorActivity;
import com.example.wirtualnatalia.network.service.ServiceManager;
import com.google.android.gms.common.data.BitmapTeleporter;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private Button statusServiceBtn;
    private Button arActivityBtn;
    private Button exitBtn;

//    private StatusService statusService;
    private ServiceManager serviceManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusServiceBtn = (Button) findViewById(R.id.serviceActivityBtn);
        arActivityBtn = (Button) findViewById(R.id.arActivityBtn);
        exitBtn = (Button) findViewById(R.id.exitBtn);

        serviceManager = new ServiceManager(this);
        statusServiceBtn.setOnClickListener(v -> launchStatusServiceActivity());
        arActivityBtn.setOnClickListener(v -> launchArActivity());

        exitBtn.setOnClickListener(v -> exitApp());
    }

    private void launchStatusServiceActivity() {
        Intent intent = new Intent(this, StatusServiceActivity.class);
        startActivity(intent);
    }

    private void launchArActivity() {
        Intent intent = new Intent(this, CloudAnchorActivity.class);
        startActivity(intent);
    }

    private void exitApp() {
        finishAndRemoveTask();
    }
}