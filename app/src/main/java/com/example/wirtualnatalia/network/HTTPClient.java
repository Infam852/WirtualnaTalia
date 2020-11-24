package com.example.wirtualnatalia.network;

import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
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

    public int errCounter;

    // !TODO should be enum?
    public static final String
        STATUS_ENDPOINT = "status";

    public HTTPClient(InetAddress host, int port){
        this.port = port;
        this.host = host;
        Log.i(TAG, "Created HTTPClient for (" + host.toString() + ":" + port + ")");
    }

    public void sendStatusGET(Context context) {

        RequestQueue queue = Volley.newRequestQueue(context);
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, createURL(STATUS_ENDPOINT),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        response = response + ", " + new Timestamp(System.currentTimeMillis());
                        Log.d(TAG, "Got response GET /status: " + response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "That didn't work! logs: " + error.getMessage());
                errCounter += 1;
            }
        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    public void sendStatusPOST(Context context, String status){
        RequestQueue queue = Volley.newRequestQueue(context);
        JSONObject postData = new JSONObject();
        // fill with data
        try {
            postData.put("status", "(x,y,z)");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                createURL(STATUS_ENDPOINT), postData, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.i(TAG, "Got response: " + response.toString());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "ERROR");
                error.printStackTrace();
            }
        });

        queue.add(jsonObjectRequest);
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
