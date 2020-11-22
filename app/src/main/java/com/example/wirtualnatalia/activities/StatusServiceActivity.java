package com.example.wirtualnatalia.activities;

import android.app.Activity;
import android.graphics.Color;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
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

import com.example.wirtualnatalia.network.service.ServiceConnection;
import com.example.wirtualnatalia.network.service.ServiceData;
import com.example.wirtualnatalia.network.service.VirtualDeckService;
import com.example.wirtualnatalia.utils.Interaction;
import com.example.wirtualnatalia.R;
import com.example.wirtualnatalia.network.NanoServer;
import com.example.wirtualnatalia.network.service.ServiceManager;
import com.example.wirtualnatalia.utils.User;

import java.io.IOException;
import java.util.ArrayList;

public class StatusServiceActivity extends Activity {
    // UI elements
    private EditText nicknameInput;
    private TextView serviceNameTxt;
    private TextView connectedToTxt;
    private Button setNicknameBtn;
    private Button startStatusServiceBtn;
    private Button stopConnectionBtn;
    private Button stopServerBtn;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView servicesList;

    private ServiceManager serviceManager;

    public static final String TAG = "Connection Activity";

    private ArrayList<String> services;
    private ArrayAdapter <String> adapter;

    private  ServiceConnection connection;
    private NanoServer nanoServer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);
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

        // it sets up discovery listener which should be initialized only once
        serviceManager.start_discovery();
        refreshServices();
    }

    private void initUI(){
        nicknameInput = findViewById(R.id.usrNicknameInput);
        serviceNameTxt = findViewById(R.id.serviceNameTxt);
        String serviceName = ServiceData.getInstance().getServiceName();
        if (serviceName != null) { serviceNameTxt.setText(serviceName); }

        connectedToTxt = findViewById(R.id.connectedToTxt);
        String connectedTo = ServiceData.getInstance().getConnectedToServiceName();
        if (connectedTo != null) { connectedToTxt.setText(connectedTo); }

        setNicknameBtn = findViewById(R.id.setNicknameBtn);
        setNicknameBtn.setOnClickListener(v -> {
            Log.d(TAG, "Set nickname button");
            User.getInstance().setNickname(nicknameInput.getText().toString());
        });

        startStatusServiceBtn = findViewById(R.id.startStatusServiceBtn);
        startStatusServiceBtn.setOnClickListener(v -> {
            Log.d(TAG, "Start service button clicked");
            startService();
        });

        stopConnectionBtn = findViewById(R.id.stopConnectionBtn);
        stopConnectionBtn.setOnClickListener(v -> {
            Log.d(TAG, "Disconnect button clicked");
            handleDisconnect();
        });

        stopServerBtn = findViewById(R.id.stopServerBtn);
        stopServerBtn.setOnClickListener(v -> {
            Log.d(TAG, "Stop button clicked");
            handleStop();
        });

        swipeRefreshLayout = findViewById(R.id.swipeRefreshConnections);
        swipeRefreshLayout.setOnRefreshListener(
            new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    Log.i(TAG, "onRefresh called from SwipeRefreshLayout");
                    refreshServices();
                    Log.i(TAG, "Services: " + services);
                }
            }
        );
        servicesList = findViewById(R.id.servicesList);
        servicesList.setAdapter(adapter);
        servicesList.setOnItemClickListener(
                (adapter, view, position, id) -> handleConnection(adapter, position));
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

        initConnectionIfNotExist();
        connection.connect(serviceClicked);
    }

    private void handleDisconnect(){
        initConnectionIfNotExist();
        connection.stopClient();
    }

    private void handleStop(){
        if (nanoServer != null){
            nanoServer.stop();
        }
        try{
            serviceManager.unregisterService(connection.registrationListener);
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "Cannot unregister: " + e.toString());
        }
        if (ServiceData.getInstance().getServiceName().equals(connection.SERVICE_NAME)){
            connection.stopClient();
        }
        Interaction.showToast(this, "Your server has been stopped");
    }

    private void refreshServices(){
        services.clear();
        services.addAll(serviceManager.getStringServices());

        adapter.notifyDataSetChanged();
        // remove progress indicator
        swipeRefreshLayout.setRefreshing(false);
    }

    private void startService(){
        Log.i(TAG, "Start service function");
        initConnectionIfNotExist();

        try {
            NanoServer newServer = new NanoServer();
            newServer.start();
            // in order to not overwrite nanoServer when start() throws exception
            nanoServer = newServer;
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Server not started");
            Interaction.showAlert(this, "Error", "You have already started service!");
            return;
        }

        Log.i(TAG, String.format("Try to register service on port (%d)", NanoServer.PORT));
        connection.registerService(this, NanoServer.PORT);
    }

    private void initConnectionIfNotExist(){
        if (ServiceData.getInstance().getConnection() == null){
            connection = new ServiceConnection(this,
                    VirtualDeckService.SERVICE_NAME, VirtualDeckService.SERVICE_TYPE,
                    serviceNameTxt, connectedToTxt);
            ServiceData.getInstance().setConnection(connection);
        }
        else if (connection == null) {
            connection = ServiceData.getInstance().getConnection();
        }
    }
}
