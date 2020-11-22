package com.example.wirtualnatalia.network;

import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class HTTPClient {
    public String TAG = "HTTP CLIENT";

    private final int port;
    private final InetAddress host;
    public String STATUS_URL = "http://192.168.0.24:5050/status";

    // !TODO should be enum?
    public static final String
        STATUS_ENDPOINT = "status";

    public HTTPClient(InetAddress host, int port){
        this.port = port;
        this.host = host;
        Log.i(TAG, "Created HTTPClient for (" + host.toString() + ":" + port + ")");
    }

    public void sendStatusGET(Context context){

        RequestQueue queue = Volley.newRequestQueue(context);
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, createURL(STATUS_ENDPOINT),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        response = response + ", " + new Timestamp(System.currentTimeMillis());
                        Log.d(TAG, "Got response on GET /status: " + response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "That didn't work! logs: " + error.getMessage());
            }
        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    public void sendStatusPOST(Context context, TextView responseView){

        RequestQueue queue = Volley.newRequestQueue(context);
        // Request a string response from the provided URL.
        StringRequest postRequest = new StringRequest(Request.Method.POST, STATUS_URL,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        // response
                        Log.d("Response", response);
                        responseView.setText(response);
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.e(TAG, "That didn't work! logs: " + error.getMessage());
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("name", "Alif");

                return params;
            }
        };
        queue.add(postRequest);
    }

    private String getErrMessage(VolleyError error){
        try {
            String body = new String(error.networkResponse.data,"UTF-8");
            Log.e(TAG, "logs: " + body);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "Fail to parse error msg";
    }

    private String createURL(String endpoint){
        return String.format(Locale.getDefault(), "http://%s:%d/%s",
                this.host.toString(), this.port, endpoint);
    }

}
