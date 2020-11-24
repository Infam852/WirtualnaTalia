package com.example.wirtualnatalia.network.service;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

import com.example.wirtualnatalia.network.FixedRateRequest;

import java.util.ArrayList;
import java.util.HashMap;


public class ServiceManager {
    public static final String TAG = "Service Manager";

    private NsdManager nsdManager;
    private NsdManager.DiscoveryListener discoveryListener;

    private ArrayList<NsdServiceInfo> discoveryServices;
    private HashMap<String, NsdServiceInfo> servicesMap;  // maps service name to service object


    public ServiceManager(Context context) {
        discoveryServices = new ArrayList<>();
        servicesMap = new HashMap<>();
        nsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
    }

    public void start_discovery() {
        stopDiscovery();  // Cancel any existing discovery request
        initializeDiscoveryListener();
        Log.i(TAG, "Start nsdManager discover");
        nsdManager.discoverServices(
                VirtualDeckService.SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener);
    }

    public void stopDiscovery() {
        if (discoveryListener != null) {
            try {
                nsdManager.stopServiceDiscovery(discoveryListener);
            } finally {
                discoveryListener = null;
            }
        }
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
                Log.i(TAG, "Service found!!, name: " + service.getServiceName() +
                        " Service type: " + VirtualDeckService.SERVICE_TYPE);
                if (!service.getServiceType().equals(VirtualDeckService.SERVICE_TYPE)){
                    Log.i(TAG, "Unknown service found: " + service);
                }
                else if (service.getServiceName().contains(VirtualDeckService.SERVICE_NAME)){
                    Log.d(TAG, "Status service found: " + service);
                    Log.d(TAG, "Status service in: " + containNsdService(service));
                    if (!containNsdService(service)){
                        discoveryServices.add(service);
                        servicesMap.put(getServiceDescription(service), service);
                    }
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
                //
                for (NsdServiceInfo serviceInfo: discoveryServices){
                    if (serviceInfo.toString().equals(service.toString())){
                        toRemove = serviceInfo;
                        break;
                    }
                }
                if (toRemove != null){
                    discoveryServices.remove(toRemove);
                    servicesMap.remove(toRemove);
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

    public void tearDown(NsdManager.RegistrationListener registrationListener) {
        try {
            nsdManager.unregisterService(registrationListener);
            nsdManager.stopServiceDiscovery(discoveryListener);
        } catch (IllegalArgumentException e){
            Log.w(TAG, "Listener already unregistered");
        }
    }

    public void unregisterService(NsdManager.RegistrationListener registrationListener) {
        try {
            nsdManager.unregisterService(registrationListener);
        } catch (IllegalArgumentException e){
            Log.w(TAG, "Listener already unregistered");
        }
    }

    public ArrayList<NsdServiceInfo> getServices(){
        return discoveryServices;
    }

    public ArrayList<String> getStringServices(){
        Log.i(TAG, "Get found services: " + discoveryServices);
        ArrayList<String> stringServices = new ArrayList<>();
        for (NsdServiceInfo serviceInfo: discoveryServices){
            stringServices.add(getServiceDescription(serviceInfo));
        }
        Log.i(TAG, "String services: " + stringServices);
        return stringServices;
    }

    private String getServiceDescription(NsdServiceInfo serviceInfo) {
        return String.format("%s",
                serviceInfo.getServiceName());
    }

    private boolean containNsdService(NsdServiceInfo target){
        for (NsdServiceInfo service: discoveryServices){
            if (service.toString().equals(target.toString())){
                return true;
            }
        }
        return false;
    }

    public HashMap<String, NsdServiceInfo> getServicesMap() {
        return servicesMap;
    }
}
