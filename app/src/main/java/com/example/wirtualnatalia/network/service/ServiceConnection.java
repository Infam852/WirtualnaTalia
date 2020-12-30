package com.example.wirtualnatalia.network.service;

import android.app.Activity;
import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

import com.example.wirtualnatalia.R;
import com.example.wirtualnatalia.activities.StatusServiceActivity;
import com.example.wirtualnatalia.network.HTTPClient;
import com.example.wirtualnatalia.network.FixedRateRequest;
import com.example.wirtualnatalia.network.NanoServer;
import com.example.wirtualnatalia.utils.Interaction;

import java.io.IOException;
import java.net.InetAddress;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;

public class ServiceConnection {
    public NsdManager.ResolveListener resolveListener;
    public NsdManager.RegistrationListener registrationListener;
    private NsdManager nsdManager;

    private NsdServiceInfo connectedService;
    public String SERVICE_NAME;  // identifies the device (android will automatically append characters if not unique)
    public String SERVICE_TYPE;  // identifies the service

    public String SERVICE_NAME_CONNECTED;
    public boolean serviceRegistered;

    private int port;
    private InetAddress host;

    private Context context;
    private HTTPClient httpClient;
    private NanoServer nanoServer;
    private Handler handler;
    private Handler handlerUI;

    private final int
        MSG_START_CONNECTION = 1;

    public static final String TAG = "ServiceConnection";

    public ServiceConnection(Context context, String serviceName, String serviceType,
                             Handler handlerUI) {
        this.context = context;
        nsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
        SERVICE_NAME = serviceName;
        SERVICE_TYPE = serviceType;

        this.handlerUI = handlerUI;

        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                Log.i(TAG, "Handler got message: " + msg);
                switch (msg.what) {
                    case MSG_START_CONNECTION:
                        handlerStartConnection();
                        break;
                }
            }
        };

        initServerIfNotExist();
        initializeResolveListener();
    }

    private void initServerIfNotExist(){
        try {
            NanoServer newServer = new NanoServer();
            newServer.start();
            // in order to not overwrite nanoServer when start() throws exception
            nanoServer = newServer;
        } catch (IOException e) {
            Log.e(TAG, "Server not started: " + e.getMessage());
        }
    }

    public void stopServer(){
        if (nanoServer != null){
            nanoServer.stop();
            nanoServer = null;
        }
    }

    private void handlerStartConnection() {
        if (connectedService.getServiceName().equals(SERVICE_NAME)){
            Log.w(TAG, "Connected to the same device, abort...");
            Interaction.showAlert(context, "Warning", "You try to connect to your service!");
//                    return;
        }

        port = connectedService.getPort();
        host = connectedService.getHost();
        SERVICE_NAME_CONNECTED = connectedService.getServiceName();
        Log.i(TAG, "Connected to the service: port: " + port + ", host: " + host.toString());
        httpClient = new HTTPClient(host, port);

        FixedRateRequest fixedRateRequest = FixedRateRequest.getInstance();
        // start status threads
        fixedRateRequest.start(context, FixedRateRequest.MethodType.GET_STATUS, httpClient, 500, 1);
        fixedRateRequest.start(context, FixedRateRequest.MethodType.POST_STATUS, httpClient, 500, 1);

        ServiceData.getInstance().setConnectedToServiceName(SERVICE_NAME_CONNECTED);
        Interaction.showToast(context, "You have connected to: " + SERVICE_NAME_CONNECTED);

        Message msg = handlerUI.obtainMessage();
        msg.what = StatusServiceActivity.MSG_UPDATE_CONNECTION;
        msg.obj = SERVICE_NAME_CONNECTED;
        handlerUI.sendMessage(msg);
    }
    public void connect(NsdServiceInfo service){
        initializeResolveListener();
        nsdManager.resolveService(service, resolveListener);
    }

    public void disconnect(){

    }

    public void registerService(Context context, int port) {
        tearDown();
        initializeRegistrationListener();
        Log.i(TAG, "Start register");
        NsdServiceInfo statusService = new NsdServiceInfo();

        statusService.setServiceName(SERVICE_NAME);
        statusService.setServiceType(SERVICE_TYPE);
        statusService.setPort(port);

        nsdManager.registerService(
                statusService, NsdManager.PROTOCOL_DNS_SD, registrationListener);
        serviceRegistered = true;
        Log.i(TAG, "quit register service");
    }

    public void tearDown() {
        if (registrationListener != null) {
            try {
                nsdManager.unregisterService(registrationListener);
            } finally {
            }
            registrationListener = null;
        }
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

                Message msg = handler.obtainMessage();
                msg.what = MSG_START_CONNECTION;
                handler.sendMessage(msg);
            }
        };
    }

    public void initializeRegistrationListener() {
        Log.e(TAG, "Registration listener initialized");
        registrationListener = new NsdManager.RegistrationListener() {

            @Override
            public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
                // Save the service name. Android may have changed it in order to
                // resolve a conflict, so update the name you initially requested
                // with the name Android actually used.
                Log.i(TAG, "NsdInfo: " + NsdServiceInfo.toString());
                if (SERVICE_NAME.equals(VirtualDeckService.SERVICE_NAME)){
                    Log.e(TAG, "Possible name collision");
                }
                SERVICE_NAME = NsdServiceInfo.getServiceName();
                Log.i(TAG, "Service registered: " + SERVICE_NAME + ", " + SERVICE_TYPE);
                ServiceData.getInstance().setServiceName(SERVICE_NAME);
                serviceRegistered = true;

                Message msg = handlerUI.obtainMessage();
                msg.what = StatusServiceActivity.MSG_REGISTERED;
                msg.obj = R.string.REGISTERED;
                handlerUI.sendMessage(msg);
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
                ServiceData.getInstance().setServiceName(null);
                Log.w(TAG, "Service unregister update UI!!!");

                Message msg = handlerUI.obtainMessage();
                msg.what = StatusServiceActivity.MSG_REGISTERED;
                msg.obj = R.string.UNREGISTERED;
                handlerUI.sendMessage(msg);

                serviceRegistered = false;
            }

            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Unregistration failed. Put debugging code here to determine why.
            }
        };
        Log.i(TAG, "Initialized new registration listener");
    }

    public HTTPClient getHttpClient(){
        return httpClient;
    }
}
