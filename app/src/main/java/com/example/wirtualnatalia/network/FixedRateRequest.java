package com.example.wirtualnatalia.network;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class FixedRateRequest {
    private static final String TAG = "StatusPusher";

    public enum MethodType {
        GET_STATUS,
        POST_STATUS
    }

    private static int counter = 0;
    private static final int MAX_ERR_COUNTER = 3;

    private final ArrayList<ScheduledExecutorService> executors = new ArrayList<>();

    private static FixedRateRequest INSTANCE;
    public static FixedRateRequest getInstance() {
        if (INSTANCE == null){
            INSTANCE = new FixedRateRequest();
        }
        return INSTANCE;
    }

    public int start(Context context, MethodType method, HTTPClient httpClient, int period, int poolSize) {
        // force creation due to possible shutdown
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(poolSize + 1);
        // schedule to pool /status periodically
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(() -> {
            if (httpClient.errCounter > MAX_ERR_COUNTER) {
                stopAll();
                Log.w(TAG, "max err counter, stop all threads");
            }

            switch (method) {
                case GET_STATUS:
                    httpClient.sendStatusGET(context);
                    break;
                case POST_STATUS:
                    httpClient.sendStatusPOST(context, "MOCKED_BODY");
                    break;
            }

        }, 0, period, TimeUnit.MILLISECONDS);

        // new thread that awaits for exceptions
        scheduler.execute(() -> {
            try {
                future.get();
            } catch (ExecutionException e) {
                Log.e(TAG, "ExecutionException, shutdown executor");
                scheduler.shutdown();
                e.printStackTrace();
            } catch (InterruptedException e) {
                Log.e(TAG, "InterruptedException");
                scheduler.shutdown();
                e.printStackTrace();
            }
        });

        executors.add(scheduler);
        return counter++;
    }

    public void stopAll() {
        for (ScheduledExecutorService executorService: executors){
            executorService.shutdown();
            Log.i(TAG, "stopped: " + executorService.toString());
        }
    }

    public void stop(int idx){
        executors.get(idx).shutdown();
    }
}
