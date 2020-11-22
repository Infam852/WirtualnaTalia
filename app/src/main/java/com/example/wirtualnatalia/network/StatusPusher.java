package com.example.wirtualnatalia.network;

import android.content.Context;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class StatusPusher {
    private HTTPClient httpClient;
    private ScheduledExecutorService executorService;

    private static StatusPusher INSTANCE;

    public static StatusPusher getInstance(HTTPClient httpClient) {
        if (INSTANCE == null){
            INSTANCE = new StatusPusher(httpClient);
        }
        return INSTANCE;
    }

    private StatusPusher(HTTPClient httpClient) {
        this.httpClient = httpClient;
        executorService = Executors.newScheduledThreadPool(1);  // 1 task
    }

    public void start(Context context) {
        // schedule to pool /status periodically
        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                httpClient.sendStatusPOST(context, "(x, y, z)");
            }
        }, 0, 500, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        executorService.shutdown();
    }
}
