package com.example.wirtualnatalia.network.service;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

import java.net.InetAddress;

public class ServiceConnection {
    public static final String TAG = "Service Connection";

    private NsdManager.ResolveListener resolveListener;
    private NsdManager nsdManager;

    private NsdServiceInfo connectedService;

    public ServiceConnection(Context context) {
        nsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
        initializeResolveListener();
    }

    public void connect(NsdServiceInfo service){
        nsdManager.resolveService(service, resolveListener);
    }

    public void initializeResolveListener() {
        resolveListener = new NsdManager.ResolveListener() {

            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Called when the resolve fails. Use the error code to debug.
                Log.e(TAG, "Resolve failed: " + errorCode);
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                Log.i(TAG, "Resolve Succeeded. " + serviceInfo);

                connectedService = serviceInfo;
                int port = connectedService.getPort();
                InetAddress host = connectedService.getHost();
                Log.i(TAG, "Service port, host: " + port + ", " + host.toString());
            }
        };
    }
}
