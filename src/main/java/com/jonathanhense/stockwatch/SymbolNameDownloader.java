package com.jonathanhense.stockwatch;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

public class SymbolNameDownloader implements Runnable{

    private final String TAG = "SymbolNameDownloader: ";

    private static final String DATA_URL = "https://api.iextrading.com/1.0/ref-data/symbols";

    public static HashMap<String, String> symbolNameMap = new HashMap<>();


    @Override
    public void run() {
        Uri dataUri = Uri.parse(DATA_URL);
        String urlToUse = dataUri.toString();
        Log.d(TAG, "run: " + urlToUse);

        StringBuilder sb = new StringBuilder();

        try {
            URL url = new URL(urlToUse);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.d(TAG, "run: HTTP ResponseCode NOT OK: " + conn.getResponseCode());
                return;
            }

            InputStream input = conn.getInputStream();
            BufferedReader reader = new BufferedReader((new InputStreamReader(input)));

            String line;
            while((line = reader.readLine()) != null){
                sb.append(line).append('\n');
            }
            Log.d(TAG, "run: " + sb.toString());
        } catch (Exception e) {
            Log.e(TAG, "run: ", e);
            return;
        }

        process(sb.toString());
        Log.d(TAG, "run: ");

    }

    private void process(String s){
        try {
            JSONArray jObjMain = new JSONArray(s);

            for (int i = 0; i < jObjMain.length(); i++) {
                JSONObject jStock = (JSONObject) jObjMain.get(i);

                String symbol = jStock.getString("symbol");
                String name = jStock.getString("name");

                symbolNameMap.put(symbol, name);
                //System.out.println(symbol + " - " +symbolNameMap.get(symbol));
            }
            Log.d(TAG, "process: ");
        }catch (JSONException e) {
            Log.d(TAG, "parseJSON: "+ e.getMessage());
            e.printStackTrace();
        }
    }

    public static ArrayList<String> findMatches(String str){
        String strToMatch = str.toUpperCase().trim();
        HashSet<String> matchSet = new HashSet<>();
        for(String symbol : symbolNameMap.keySet()){
            if(symbol.toUpperCase().trim().contains(strToMatch.toUpperCase())){
                matchSet.add(symbol + " - " + symbolNameMap.get(symbol));
            }
            String name = symbolNameMap.get(symbol);
            if(name != null && name.toUpperCase().trim().contains(strToMatch.toUpperCase())){
                matchSet.add(symbol + " - " + name);
            }
        }

        ArrayList<String> results = new ArrayList<>(matchSet);
        Collections.sort(results);

        return results;
    }
}
