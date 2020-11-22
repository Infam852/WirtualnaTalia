package com.example.wirtualnatalia.network;

import android.content.Context;
import android.content.res.Resources;
import android.net.nsd.NsdManager;
import android.util.Log;

import java.io.IOException;
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


    public NanoServer() throws IOException {
        super(PORT);
        Log.i(TAG, "Server start listening on port: " + PORT);
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
            session.parseBody(new HashMap<>());
            String requestBody = session.getQueryParameterString();
            return newFixedLengthResponse(Response.Status.OK, MIME_PLAINTEXT,
                    "Request body = " + requestBody);
        } catch (IOException | ResponseException e) {
            // handle
        }
        return newFixedLengthResponse(Response.Status.BAD_REQUEST, MIME_PLAINTEXT,
                "Failed to parse the request");
    }

    public void stop() {
        super.stop();
        Log.i(TAG, "Server stopped");
    }
}
