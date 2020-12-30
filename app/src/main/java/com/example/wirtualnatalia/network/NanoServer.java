package com.example.wirtualnatalia.network;

import android.util.Log;

import com.example.wirtualnatalia.common.cards.Card;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

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

    // card list that will be synchronized
    private ArrayList<Card> cards = new ArrayList<>();

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
        else if (uri.equals("/card")){
            if (session.getMethod() == Method.GET){
                return handleCardsGET();
            }
            if (session.getMethod() == Method.POST){
                return handleCardsPOST(session);
            }
        }
        return newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT,
                "The requested resource does not exist");
    }

    private Response handleCardsPOST(IHTTPSession session){
        try {
            Log.i(TAG, "Got POST request");
//            session.parseBody(new HashMap<>());
//            String requestBody = session.getQueryParameterString();
            final HashMap<String, String> params = new HashMap<String, String>();
            session.parseBody(params);
            JSONObject json = new JSONObject(params.get("postData"));
            Log.i(TAG, "POST json=" + json + ", suit: " + json.getString("suit") + ", symbol" + json.getString("symbol"));
            // add cards to global array list
            cards.add(new Card(json.getString("symbol"), json.getString("suit"), json.getString("uuid")));

            JSONObject responseJson = new JSONObject();
            responseJson.put("response", "Got card!");
            return newFixedLengthResponse(Response.Status.OK, MIME_PLAINTEXT,
                    responseJson.toString());
        } catch (IOException | ResponseException | JSONException e) {
            // handle
            Log.e(TAG, "Error: " + Arrays.toString(e.getStackTrace()));
        }
        return newFixedLengthResponse(Response.Status.BAD_REQUEST, MIME_PLAINTEXT,
                "Failed to parse the request");
    }

    private Response handleCardsGET(){
        Gson gson = new Gson();
        String serialized = gson.toJson(cards);
//        ArrayList<Card> cards = gson.fromJson(serialized, ArrayList.class);
//        Log.i(TAG, "Deserialized cards: " + cards.toString());
        return newFixedLengthResponse(Response.Status.OK, MIME_PLAINTEXT, serialized);
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
