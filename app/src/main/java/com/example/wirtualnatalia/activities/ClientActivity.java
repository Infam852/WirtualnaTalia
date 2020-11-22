package com.example.wirtualnatalia.activities;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.wirtualnatalia.R;
import com.example.wirtualnatalia.network.HTTPClient;


public class ClientActivity extends Activity {
    private Button sndGetBtn;
    private Button sndPostBtn;
    private TextView responseView;
    private TextView responseTxt;

    private String STATUS_URL = "http://192.168.0.24:5050/status";

    private HTTPClient client;

    private static final String TAG = "ClientActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        sndGetBtn = (Button) findViewById(R.id.sndGetBtn);
        sndGetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleGetStatus();
            }
        });

        sndPostBtn = (Button) findViewById(R.id.sndPostBtn);
        sndPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handlePostStatus();
            }
        });

        responseView = (TextView) findViewById(R.id.responeView);

//        client = new HTTPClient();
    }

    public void handleGetStatus() {
        Log.d(TAG, "Send GET request to the server...");
        client.sendStatusGET(this);
    }

    public void handlePostStatus() {
        Log.d(TAG, "Send POST request to the server...");
        client.sendStatusPOST(this, "responseView");
    }

}
