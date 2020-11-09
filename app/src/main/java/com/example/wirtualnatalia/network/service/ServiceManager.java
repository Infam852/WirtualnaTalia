package com.example.wirtualnatalia.network.service;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

import java.net.InetAddress;
import java.util.ArrayList;

import javax.net.ssl.SSLEngineResult;

public class ServiceManager {
    public static final String TAG = "Service Manager";

    private NsdManager nsdManager;
    private NsdManager.ResolveListener resolveListener;
    private NsdServiceInfo mService;
    private NsdManager.DiscoveryListener discoveryListener;

    private ArrayList<NsdServiceInfo> discoveryServices;


    public ServiceManager(Context context) {
        discoveryServices = new ArrayList<>();
        nsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
        initializeResolveListener();
        initializeDiscoveryListener();
    }

    public void start_discovery() {
        nsdManager.discoverServices(
                StatusService.SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener);
    }

    public void connectToService(NsdServiceInfo service){
        nsdManager.resolveService(service, resolveListener);
    }

    public void initializeDiscoveryListener() {

        // Instantiate a new DiscoveryListener
        discoveryListener = new NsdManager.DiscoveryListener() {

            // Called as soon as service discovery begins.
            @Override
            public void onDiscoveryStarted(String regType) {
                Log.d(TAG, "Service discovery started");
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                // A service was found! Do something with it.
                Log.i(TAG, "Type: " + service.getServiceName() + "vs " + StatusService.SERVICE_TYPE);
                if (!service.getServiceType().equals(StatusService.SERVICE_TYPE)){
                    Log.i(TAG, "Unknown service found: " + service);
                }
                else if (service.getServiceName().contains(StatusService.SERVICE_NAME)){
                    Log.d(TAG, "Status service found: " + service);
                    discoveryServices.add(service);
                }
                else {
                    Log.i(TAG, "Service: " + service);
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                // When the network service is no longer available.
                // Internal bookkeeping code goes here.
                Log.e(TAG, "service lost: " + service);
                NsdServiceInfo toRemove = null;
                for (NsdServiceInfo serviceInfo: discoveryServices){
                    if (serviceInfo.toString().equals(service.toString())){
                        toRemove = serviceInfo;
                        break;
                    }
                }
                if (toRemove != null){
                    discoveryServices.remove(toRemove);
                }
                Log.i(TAG, "discovery services: " + discoveryServices + ", " + discoveryServices.contains(service));
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.i(TAG, "Discovery stopped: " + serviceType);
            }


            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                nsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                nsdManager.stopServiceDiscovery(this);
            }
        };
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

                mService = serviceInfo;
                int port = mService.getPort();
                InetAddress host = mService.getHost();
                Log.i(TAG, "Service port, host: " + port + ", " + host.toString());
            }
        };
    }

    public void tearDown(NsdManager.RegistrationListener registrationListener) {
        nsdManager.unregisterService(registrationListener);
        nsdManager.stopServiceDiscovery(discoveryListener);
    }

    public ArrayList<NsdServiceInfo> getServices(){
        return discoveryServices;
    }

    public ArrayList<String> getStringServices(){
        Log.i(TAG, "Get found services: " + discoveryServices);
        ArrayList<String> stringServices = new ArrayList<>();
        for (NsdServiceInfo serviceInfo: discoveryServices){
            stringServices.add(serviceInfo.toString());
        }
        Log.i(TAG, "String services: " + stringServices);
        return stringServices;
    }
}
