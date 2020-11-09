package com.example.wirtualnatalia.activities;

import android.app.Activity;
import android.app.ListActivity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.wirtualnatalia.R;
import com.example.wirtualnatalia.network.service.ServiceManager;

import java.util.ArrayList;

public class StatusServiceActivity extends Activity {
    // UI elements
    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView servicesList;

    private ServiceManager serviceManager;

    public static final String TAG = "Connection Activity";

    private ArrayList<String> services;
    private ArrayAdapter <String> adapter;

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

        serviceManager.start_discovery();
    }

    private void initUI(){
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
        servicesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
                String item = (String) adapter.getItemAtPosition(position);
                Log.i(TAG, "Item clicked: " + item);
            }
        });
    }

    private void refreshServices(){
        services.clear();
        services.addAll(serviceManager.getStringServices());

        adapter.notifyDataSetChanged();
        // remove progress indicator
        swipeRefreshLayout.setRefreshing(false);
    }
}
