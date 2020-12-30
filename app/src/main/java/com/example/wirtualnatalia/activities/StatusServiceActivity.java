package com.example.wirtualnatalia.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.wirtualnatalia.cloudanchor.CloudAnchorActivity;
import com.example.wirtualnatalia.network.FixedRateRequest;
import com.example.wirtualnatalia.network.service.ServiceConnection;
import com.example.wirtualnatalia.network.service.ServiceData;
import com.example.wirtualnatalia.network.service.VirtualDeckService;
import com.example.wirtualnatalia.utils.Interaction;
import com.example.wirtualnatalia.R;
import com.example.wirtualnatalia.network.NanoServer;
import com.example.wirtualnatalia.network.service.ServiceManager;
import com.example.wirtualnatalia.utils.User;

import java.util.ArrayList;

public class StatusServiceActivity extends Activity {
    // UI elements
    private EditText serviceName;
    private TextView isRegisteredTxt;
    private TextView connectedToTxt;
    private Button setServiceNameBtn;
    private Button startStatusServiceBtn;
    private Button stopConnectionBtn;
    private Button stopServerBtn;
    private Button arActivityBtn;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView servicesList;

    private ServiceManager serviceManager;

    public static final String TAG = "Connection Activity";

    private ArrayList<String> services;
    private ArrayAdapter <String> adapter;

    private ServiceConnection connection;

    private Handler handlerUI;
    public static final int
        MSG_UPDATE_CONNECTION = 1,
        MSG_REGISTERED = 2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);
        initHandler();

        serviceManager = new ServiceManager(this);
        services = new ArrayList<>();
        adapter = new ArrayAdapter<String> (
                this, android.R.layout.simple_list_item_1, services
        ){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                TextView textView = (TextView) view.findViewById(android.R.id.text1);
                textView.setTextColor(Color.WHITE);

                return view;
            }
        };
        initUI();
        initServiceData();
        initConnectionIfNotExist();

        // it sets up discovery listener which should be initialized only once
        refreshServices();
    }

    private void initHandler(){
        handlerUI = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                Log.i(TAG, "UI handler got message: " + msg);
                switch (msg.what) {
                    case MSG_UPDATE_CONNECTION:
                        String txt = (String) msg.obj;
                        connectedToTxt.setText(txt);
                        break;
                    case MSG_REGISTERED:
                        String registered = getResources().getString((int)msg.obj);
                        isRegisteredTxt.setText(registered);
                        break;
                }
            }
        };
    }

    private void initUI(){
        serviceName = findViewById(R.id.serviceNameInput);
        isRegisteredTxt = findViewById(R.id.serviceRegisteredTxt);

        connectedToTxt = findViewById(R.id.connectedToTxt);
        String connectedTo = ServiceData.getInstance().getConnectedToServiceName();
        Log.i(TAG, "Connected to " + connectedTo);
        if (connectedTo != null) { connectedToTxt.setText(connectedTo); }

        setServiceNameBtn = findViewById(R.id.setServiceNameBtn);
        setServiceNameBtn.setOnClickListener(v -> {
            Log.d(TAG, "Set service name button");
            User.getInstance().setNickname(serviceName.getText().toString());
        });

        startStatusServiceBtn = findViewById(R.id.startStatusServiceBtn);
        startStatusServiceBtn.setOnClickListener(v -> {
            Log.d(TAG, "Start service button clicked");
            registerService();
        });

        stopConnectionBtn = findViewById(R.id.stopConnectionBtn);
        stopConnectionBtn.setOnClickListener(v -> {
            Log.d(TAG, "Disconnect button clicked");
            handleDisconnect();
        });

        stopServerBtn = findViewById(R.id.unregisterBtn);
        stopServerBtn.setOnClickListener(v -> {
            Log.d(TAG, "Stop button clicked");
            handleUnregister();
        });

        swipeRefreshLayout = findViewById(R.id.swipeRefreshConnections);
        swipeRefreshLayout.setOnRefreshListener(
            new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    refreshServices();
                    Log.i(TAG, "onRefresh, services: " + services);
                }
            }
        );
        servicesList = findViewById(R.id.servicesList);
        servicesList.setAdapter(adapter);
        servicesList.setOnItemClickListener(
                (adapter, view, position, id) -> handleConnection(adapter, position));

        arActivityBtn = (Button) findViewById(R.id.launchArActivityBtn);
        arActivityBtn.setOnClickListener(v -> launchArActivity());
    }

    private void launchArActivity() {
        Intent intent = new Intent(this, CloudAnchorActivity.class);
        intent.putExtra("HTTPClient", connection.getHttpClient());
        Log.i(TAG, "HTTP client: " + connection.getHttpClient());
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        if (serviceManager != null) {
            connection.tearDown();
            Log.i(TAG, "onPause called");
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (serviceManager != null) {
            initConnectionIfNotExist();
            if (connection.serviceRegistered){
                registerService();
                Log.i(TAG, "Register service because it was registered before resume");
            }
            serviceManager.start_discovery();
        }
    }

    @Override
    protected void onDestroy() {
        connection.tearDown();
        Log.i(TAG, "onDestroy called");
        super.onDestroy();
    }

    private void initServiceData(){
        ServiceData serviceData = ServiceData.getInstance();
        if (serviceData.getServiceName() == null){
            serviceData.setServiceName(VirtualDeckService.SERVICE_NAME);
        }
    }

    private void handleConnection(AdapterView<?> adapter, int position){
        if (ServiceData.getInstance().getConnectedToServiceName() != null){
            Interaction.showAlert(this, "Warning",
                    "You cannot connect to more than 1 service");
            return;
        }
        String item = (String) adapter.getItemAtPosition(position);
        NsdServiceInfo serviceClicked = serviceManager.getServicesMap().get(item);
        Log.i(TAG, "Item clicked: " + item);
        Log.i(TAG, "Service clicked: " + serviceClicked);

        connection.connect(serviceClicked);
    }

    private void handleDisconnect(){
        connection.tearDown();
        ServiceData.getInstance().setConnectedToServiceName(null);
        FixedRateRequest.getInstance().stopAll();
        ((Activity) this).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                connectedToTxt.setText(R.string.NOT_CONNECTED);
            }
        });
        ServiceData.getInstance().setConnectedToServiceName(null);
    }

    private void handleStop(){
        connection.tearDown();
        connection.stopServer();

        // if we are connected to local server disconnect client
        if (ServiceData.getInstance().getServiceName()!= null &&
                ServiceData.getInstance().getServiceName().equals(connection.SERVICE_NAME)){
            ServiceData.getInstance().setConnectedToServiceName(null);
            FixedRateRequest.getInstance().stopAll();
            ((Activity) this).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    isRegisteredTxt.setText(R.string.NOT_STARTED);
                    connectedToTxt.setText(R.string.NOT_CONNECTED);
                }
            });
        }
        Interaction.showToast(this, "Your server has been stopped");
    }

    private void handleUnregister(){
        connection.tearDown();
    }

    private void refreshServices(){
        services.clear();
        services.addAll(serviceManager.getStringServices());

        adapter.notifyDataSetChanged();
        // remove progress indicator
        swipeRefreshLayout.setRefreshing(false);
    }

    private void registerService(){
        Log.i(TAG, String.format("Try to register service on port (%d)", NanoServer.PORT));
        connection.registerService(this, NanoServer.PORT);
    }

    private void initConnectionIfNotExist(){
        connection = new ServiceConnection(this,
                VirtualDeckService.SERVICE_NAME, VirtualDeckService.SERVICE_TYPE, handlerUI);
    }
}
