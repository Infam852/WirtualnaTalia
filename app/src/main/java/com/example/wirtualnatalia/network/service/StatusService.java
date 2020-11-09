package com.example.wirtualnatalia.network.service;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

public class StatusService {
    private NsdManager.RegistrationListener registrationListener;
    public static String SERVICE_NAME = "Virtual Deck";  // identifies the device (android will automatically append characters if not unique)
    public static String SERVICE_TYPE = "_virtualdeck._tcp.";  // identifies the service

    public static final String TAG = "Status Service";

    public void registerService(Context context, int port) {
        Log.i(TAG, "Start register");
        NsdServiceInfo statusService = new NsdServiceInfo();

        statusService.setServiceName(SERVICE_NAME);
        statusService.setServiceType(SERVICE_TYPE);
        statusService.setPort(port);

        NsdManager nsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
        initializeRegistrationListener();

        nsdManager.registerService(
                statusService, NsdManager.PROTOCOL_DNS_SD, registrationListener);
        Log.i(TAG, "quit register service");
    }

    public void initializeRegistrationListener() {
        registrationListener = new NsdManager.RegistrationListener() {

            @Override
            public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
                // Save the service name. Android may have changed it in order to
                // resolve a conflict, so update the name you initially requested
                // with the name Android actually used.
                SERVICE_NAME = NsdServiceInfo.getServiceName();
                Log.i(TAG, "Service registered: " + SERVICE_NAME + ", " + SERVICE_TYPE);
            }

            @Override
            public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Registration failed! Put debugging code here to determine why.
                Log.d(TAG, "Service registration failed with code: " + errorCode);
            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo arg0) {
                // Service has been unregistered. This only happens when you call
                // NsdManager.unregisterService() and pass in this listener.
            }

            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Unregistration failed. Put debugging code here to determine why.
            }
        };
    }
}
