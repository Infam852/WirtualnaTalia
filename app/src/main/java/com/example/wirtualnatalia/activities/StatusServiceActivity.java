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
import com.example.wirtualnatalia.network.service.VirtualDeckService;
import com.example.wirtualnatalia.utils.Dialog;
import com.example.wirtualnatalia.R;
import com.example.wirtualnatalia.network.NanoServer;
import com.example.wirtualnatalia.network.service.ServiceManager;
import com.example.wirtualnatalia.utils.User;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;

public class StatusServiceActivity extends Activity {
    // UI elements
    private EditText nicknameInput;
    private TextView serviceNameTxt;
    private TextView serverResponseTxt;
    private Button setNicknameBtn;
    private Button startStatusServiceBtn;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView servicesList;

    private ServiceManager serviceManager;

    public static final String TAG = "Connection Activity";

    private ArrayList<String> services;
    private ArrayAdapter <String> adapter;

    private ServiceConnection connectionServer;
    private ServiceConnection connectionClient;
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
    }

    private void initUI(){
        nicknameInput = findViewById(R.id.usrNicknameInput);
        serviceNameTxt = findViewById(R.id.serviceNameTxt);
        serverResponseTxt = findViewById(R.id.serverResponseTxt);

        setNicknameBtn = findViewById(R.id.setNicknameBtn);
        setNicknameBtn.setOnClickListener(v -> {
            Log.i(TAG, "Set nickname button");
            User.getInstance().setNickname(nicknameInput.getText().toString());
        });

        startStatusServiceBtn = findViewById(R.id.startStatusServiceBtn);
        startStatusServiceBtn.setOnClickListener(v -> {
            Log.i(TAG, "Start service button clicked");
            startService();
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
        String item = (String) adapter.getItemAtPosition(position);
        NsdServiceInfo serviceClicked = serviceManager.getServicesMap().get(item);
        Log.i(TAG, "Item clicked: " + item);
        Log.i(TAG, "Service clicked: " + serviceClicked);
        connectionClient = new ServiceConnection(this,
                VirtualDeckService.SERVICE_NAME, VirtualDeckService.SERVICE_TYPE);
        connectionClient.connect(serviceClicked);
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
        connectionServer = new ServiceConnection(this,
                VirtualDeckService.SERVICE_NAME, VirtualDeckService.SERVICE_TYPE);
        try {
            nanoServer = new NanoServer();
            nanoServer.start();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Server not started");
            Dialog.showAlert(this, "Error", "You have already started service!");
            return;
        }

        Log.i(TAG, String.format("Try to register service on port (%d)", NanoServer.PORT));
        connectionServer.registerService(this, NanoServer.PORT);

        serviceNameTxt.setText(connectionServer.SERVICE_NAME);
    }
}
