package com.example.wirtualnatalia.activities;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.wirtualnatalia.R;
import com.example.wirtualnatalia.network.NanoServer;

import java.io.IOException;

public class ServerActivity extends Activity {
    public NanoServer server;
    private Button mBtGoBack;
    private Button startBtn;
    private TextView statusView;

    private boolean running = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);
        mBtGoBack = (Button) findViewById(R.id.backBtn);
        mBtGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        startBtn = (Button) findViewById(R.id.startBtn);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleStartBtn();
            }
        });

        statusView = (TextView) findViewById(R.id.statusView);
        try {
            server = new NanoServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleStartBtn(){
        if (running) {
            stop();
        }
        else {
            start();
        }
    }

    private void start() {
        try {
            server.start();
            running = true;
            Log.d("server", "Starting server...");
            statusView.setText(R.string.STARTED);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stop() {
        server.stop();
        running = false;
        Log.d("server", "Shutdown server...");
        statusView.setText(R.string.NOT_STARTED);
    }
}
