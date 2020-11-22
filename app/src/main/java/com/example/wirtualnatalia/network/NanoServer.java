package com.example.wirtualnatalia.network;

import android.content.Context;
import android.content.res.Resources;
import android.net.nsd.NsdManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

import fi.iki.elonen.NanoHTTPD;


public class NanoServer extends NanoHTTPD {
    public static final String TAG = "HTTP Server";
    public static boolean WORKING = false;

    public final static int PORT = 8080;

    private static final String MIME_JSON = "application/json";

    // HTTP status codes
    public static final String
        HTTP_OK = "200 OK",
        HTTP_NOCONTENT = "204 No Content",
        HTTP_BADREQUEST = "400 Bad Request",
        HTTP_FORBIDDEN = "403 Forbidden",
        HTTP_NOTFOUND = "404 Not Found",
        HTTP_INTERNALERROR = "500 Internal Server Error",
        HTTP_NOTIMPLEMENTED = "501 Not Implemented";


    public NanoServer() {
        super(PORT);
        Log.i(TAG, "Server initialized on port: " + PORT);
        WORKING = true;
    }

    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();
        Log.d(TAG, "Got request for: " + uri);
        if (uri.equals("/status")) {
            if (session.getMethod() == Method.GET){
                return handleStatusGET();
            }
            else if (session.getMethod() == Method.POST){
                return handleStatusPOST(session);
            }
        }
        return newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT,
                "The requested resource does not exist");
    }

    private Response handleStatusGET(){
        String response = "Server is working";  // !TODO create response
        return newFixedLengthResponse(Response.Status.OK, MIME_PLAINTEXT, response);
    }

    private Response handleStatusPOST(IHTTPSession session){
        try {
            Log.i(TAG, "Got POST request");
//            session.parseBody(new HashMap<>());
//            String requestBody = session.getQueryParameterString();
            final HashMap<String, String> params = new HashMap<String, String>();
            session.parseBody(params);
            JSONObject json = new JSONObject(params.get("postData"));
            Log.i(TAG, "POST json=" + json + ", status: " + json.getString("status"));

            JSONObject responseJson = new JSONObject();
            responseJson.put("response", "Status updated");
            return newFixedLengthResponse(Response.Status.OK, MIME_PLAINTEXT,
                    responseJson.toString());
        } catch (IOException | ResponseException | JSONException e) {
            // handle
            Log.e(TAG, "Error: " + Arrays.toString(e.getStackTrace()));
        }
        return newFixedLengthResponse(Response.Status.BAD_REQUEST, MIME_PLAINTEXT,
                "Failed to parse the request");
    }

    public void stop() {
        super.stop();
        Log.i(TAG, "Server stopped");
    }
}
