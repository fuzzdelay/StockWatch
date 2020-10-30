package com.jonathanhense.stockwatch;

import android.net.Uri;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class StockDownloader implements Runnable {

    private final String TAG = "StockDownloader: ";
    private MainActivity mainActivity;
    private String searchTarget;
    private static final String DATA_URL = "https://cloud.iexapis.com/stable/stock/";
    private static final String DATA_URL2 = "/quote?token=";
    private static final String API_KEY = "pk_1d6ee1d0f521410092eb14c8a07406ee";

    StockDownloader(MainActivity mainActivity, String searchTarget){
        this.mainActivity = mainActivity;
        this.searchTarget = searchTarget;
    }

    @Override
    public void run() {
        Uri.Builder uriBuilder = Uri.parse(DATA_URL+searchTarget.trim()+DATA_URL2+API_KEY).buildUpon();
        String urlToUse = uriBuilder.toString();

        Log.d(TAG, "run: " + urlToUse);

        StringBuilder sb = new StringBuilder();
        try{
            URL url = new URL(urlToUse);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            if(connection.getResponseCode() != HttpURLConnection.HTTP_OK){
                Log.d(TAG, "run: HTTP ResponseCode NOT OK: "+ connection.getResponseCode());
                return;
            }

            InputStream input = connection.getInputStream();
            BufferedReader reader = new BufferedReader((new InputStreamReader(input)));

            String line;
            while((line = reader.readLine()) != null){
                sb.append(line).append('\n');

            }

            Log.d(TAG, "run: "+ sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        //System.out.println(sb.toString());
        process(sb.toString());
    }

    private void process(String s){
        try{
            JSONObject jStock = new JSONObject(s);

            String symbol = jStock.getString("symbol");
            String name = jStock.getString("companyName");

            String latestPrice = jStock.getString("latestPrice");
            double lp = 0;
            if(!latestPrice.trim().isEmpty() && !latestPrice.trim().equals("null")){
                lp = Double.parseDouble(latestPrice);
            }

            String change = jStock.getString("change");
            double ch = 0;
            if(!change.trim().isEmpty() && !change.trim().equals("null")){
                ch = Double.parseDouble(change);
            }

            String changePercent = jStock.getString("changePercent");
            double chPer = 0;
            if(!changePercent.trim().isEmpty() && !changePercent.trim().equals("null")){
                chPer = Double.parseDouble(changePercent);
            }

            final Stock stock = new Stock(symbol, name, lp, ch, chPer);

            mainActivity.runOnUiThread(new Runnable(){
                @Override
                public void run(){
                    mainActivity.addStock(stock);
                }
            });


        } catch (JSONException e) {
            Log.d(TAG, "parseJSON: " + e.getMessage());
            e.printStackTrace();
        }
    }


}
