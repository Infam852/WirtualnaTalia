package com.example.wirtualnatalia.network;

import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import com.example.wirtualnatalia.utils.User;

import java.net.HttpCookie;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class StatusPuller {
    private static final String TAG = "StatusPusher";

    private HTTPClient httpClient;
    private ScheduledExecutorService executorService;

    private static StatusPuller INSTANCE;

    public static StatusPuller getInstance(HTTPClient httpClient) {
        if (INSTANCE == null){
            INSTANCE = new StatusPuller(httpClient);
        }
        return INSTANCE;
    }

    private StatusPuller(HTTPClient httpClient) {
        this.httpClient = httpClient;
    }

    public void start(Context context) {
        // force creation due to possible shutdown
        executorService = Executors.newScheduledThreadPool(1);  // 1 tasks
        // schedule to pool /status periodically
        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                httpClient.sendStatusGET(context);
            }
        }, 0, 500, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        executorService.shutdown();
        Log.i(TAG, "stopped");
    }
}
