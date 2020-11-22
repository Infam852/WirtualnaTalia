package com.example.wirtualnatalia.network.service;

import android.app.Activity;
import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;
import android.widget.TextView;

import com.example.wirtualnatalia.R;
import com.example.wirtualnatalia.network.HTTPClient;
import com.example.wirtualnatalia.network.StatusPuller;
import com.example.wirtualnatalia.network.StatusPusher;
import com.example.wirtualnatalia.utils.Interaction;

import org.w3c.dom.Text;

import java.net.InetAddress;
import java.util.logging.Handler;

public class ServiceConnection {
    public NsdManager.ResolveListener resolveListener;
    public NsdManager.RegistrationListener registrationListener;
    private NsdManager nsdManager;

    private NsdServiceInfo connectedService;
    public String SERVICE_NAME;  // identifies the device (android will automatically append characters if not unique)
    public String SERVICE_TYPE;  // identifies the service

    public String SERVICE_NAME_CONNECTED;

    private int port;
    private InetAddress host;

    private Context context;
    private HTTPClient httpClient;
    private Handler handler;

    private TextView serviceNameServerTxt;
    private TextView serviceNameClientTxt;

    public static final String TAG = "ServiceConnection";

    public ServiceConnection(Context context, String serviceName, String serviceType,
                             TextView serviceNameServerTxt, TextView serviceNameClientTxt) {
        this.context = context;
        nsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
        SERVICE_NAME = serviceName;
        SERVICE_TYPE = serviceType;

        this.serviceNameServerTxt = serviceNameServerTxt;
        this.serviceNameClientTxt = serviceNameClientTxt;

        initializeResolveListener();
    }

    public void connect(NsdServiceInfo service){
        nsdManager.resolveService(service, resolveListener);
    }

    public void registerService(Context context, int port) {
        initializeRegistrationListener();
        Log.i(TAG, "Start register");
        NsdServiceInfo statusService = new NsdServiceInfo();

        statusService.setServiceName(SERVICE_NAME);
        statusService.setServiceType(SERVICE_TYPE);
        statusService.setPort(port);

        NsdManager nsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
        nsdManager.registerService(
                statusService, NsdManager.PROTOCOL_DNS_SD, registrationListener);
        Log.i(TAG, "quit register service");
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
                if (serviceInfo.getServiceName().equals(SERVICE_NAME)){
                    Log.w(TAG, "Connected to the same device, abort...");
                    Interaction.showAlert(context, "Error", "You cannot connect to your service!");
//                    return;  !TODO remove comment
                }
                connectedService = serviceInfo;
                port = connectedService.getPort();
                host = connectedService.getHost();
                SERVICE_NAME_CONNECTED = connectedService.getServiceName();
                Log.i(TAG, "Connected to the service: port: " + port + ", host: " + host.toString());
                httpClient = new HTTPClient(host, port);
                StatusPuller statusPuller = StatusPuller.getInstance(httpClient);
                statusPuller.start(context);
                StatusPusher statusPusher = StatusPusher.getInstance(httpClient);
                statusPusher.start(context);

                ServiceData.getInstance().setConnectedToServiceName(SERVICE_NAME_CONNECTED);
                Interaction.showToast(context, "You have connected to: " + SERVICE_NAME_CONNECTED);
                ((Activity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        serviceNameClientTxt.setText(SERVICE_NAME_CONNECTED);
                    }
                });
            }
        };
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
                ServiceData.getInstance().setServiceStarted(true);
                ServiceData.getInstance().setServiceName(SERVICE_NAME);
                ((Activity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        serviceNameServerTxt.setText(SERVICE_NAME);
                    }
                });
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
                ServiceData.getInstance().setServiceStarted(false);
                ServiceData.getInstance().setServiceName(null);
                ServiceData.getInstance().setConnectedToService(false);
                ServiceData.getInstance().setConnectedToServiceName(null);

                ((Activity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        serviceNameServerTxt.setText(R.string.NOT_STARTED);
                    }
                });
            }

            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Unregistration failed. Put debugging code here to determine why.
            }
        };
    }

    public void stopClient(){
        if (httpClient == null) {
            Log.e(TAG, "Null client, return.");
            return;
        }
        StatusPuller statusPuller = StatusPuller.getInstance(httpClient);
        statusPuller.stop();
        StatusPusher statusPusher = StatusPusher.getInstance(httpClient);
        statusPusher.stop();
        ServiceData.getInstance().setConnectedToService(false);
        ServiceData.getInstance().setConnectedToServiceName(null);
        ((Activity) context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                serviceNameClientTxt.setText(R.string.NOT_CONNECTED);
            }
        });
    }
}
