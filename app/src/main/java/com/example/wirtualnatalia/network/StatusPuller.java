package com.example.wirtualnatalia.network;

import android.content.Context;
import android.widget.TextView;

import com.example.wirtualnatalia.utils.User;

import java.net.HttpCookie;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class StatusPuller {
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
        executorService = Executors.newScheduledThreadPool(1);  // 1 task
    }

    public void start(Context context) {
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
    }
}
