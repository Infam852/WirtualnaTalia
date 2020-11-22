package com.example.wirtualnatalia.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;


import com.example.wirtualnatalia.R;
import com.example.wirtualnatalia.network.service.ServiceManager;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private Button statusServiceBtn;

//    private StatusService statusService;
    private ServiceManager serviceManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusServiceBtn= (Button) findViewById(R.id.serviceActivityBtn);

        serviceManager = new ServiceManager(this);
        statusServiceBtn.setOnClickListener(v -> launchStatusServiceActivity());
    }

    @Override
    protected void onPause() {
        super.onPause();
//        !TODO
    }

    private void launchStatusServiceActivity() {
        Intent intent = new Intent(this, StatusServiceActivity.class);
        startActivity(intent);
    }
}