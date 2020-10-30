package com.jonathanhense.stockwatch;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.ScrollingMovementMethod;
import android.util.JsonWriter;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener {

    private final String TAG = "MainActivity";
    private RecyclerView recyclerView;
    private StockAdapter stockAdapter;
    private final List<Stock> stocks = new ArrayList<>();
    private String selection;
    //private static final String marketWatchURL = "http://www.marketwatch.com/investing/stock/";
    private SwipeRefreshLayout swiper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //setTitle("Stock Watch");

        recyclerView = findViewById(R.id.recycler);
        stockAdapter = new StockAdapter(stocks, this);
        recyclerView.setAdapter(stockAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        SymbolNameDownloader snd = new SymbolNameDownloader();
        new Thread(snd).start();


        swiper = findViewById(R.id.swiper);
        swiper.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                doRefresh();

            }
        });

        //Collections.sort(stocks);
        readJSONData(stocks);

    }

    public void doRefresh() {
        List<String> symbolList = new ArrayList<>();
        List<Stock> tempStockList = new ArrayList<>();
        for(Stock stock : stocks){
            symbolList.add(stock.getSymbol());
        }
        if(checkNetworkConnection()) {
            writeJSONData();
            stocks.removeAll(stocks);
            //readJSONData(tempStockList);
            for (int i = 0; i<symbolList.size(); i++) {
                StockDownloader stockDownloader = new StockDownloader(this, symbolList.get(i));
                new Thread(stockDownloader).start();
            }
            readJSONData(tempStockList);
            Collections.sort(tempStockList);
            stockAdapter.notifyDataSetChanged();
            swiper.setRefreshing(false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.addStock:
                makeStockDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void makeStockDialog() {
        if (!checkNetworkConnection()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("No Network Connection");
            builder.setMessage("Stocks Cannot Be Added Without A Network Connection");
            AlertDialog dialog = builder.create();
            dialog.show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        final EditText et = new EditText(this);
        et.setInputType(InputType.TYPE_CLASS_TEXT);
        et.setGravity(Gravity.CENTER_HORIZONTAL);
        et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);

        builder.setView(et);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                selection = et.getText().toString().trim();

                final ArrayList<String> results = SymbolNameDownloader.findMatches(selection);

                if (results.size() == 0) {
                    doNoAnswer(selection);
                } else if (results.size() == 1) {
                    doSelection(results.get(0));
                } else {
                    String[] array = results.toArray(new String[0]);

                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    //builder.setTitle("Stock Selection");
                    //builder.setMessage("Please enter a Stock Symbol");
                    builder.setItems(array, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            String symbol = results.get(which);
                            doSelection(symbol);
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    });
                    AlertDialog dialog2 = builder.create();
                    dialog2.show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });

        builder.setMessage("Please enter a Stock Symbol:");
        builder.setTitle("Stock Selection");

        AlertDialog dialog = builder.create();
        dialog.show();

    }

    private void doSelection(String sym) {
        String[] data = sym.split("-");
        StockDownloader stockDownloader = new StockDownloader(this, data[0].trim());
        new Thread(stockDownloader).start();
    }

    private void doNoAnswer(String symbol) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("No data for specified symbol/name");
        builder.setTitle("Symbol Not Found: " + symbol);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        writeJSONData();
    }
/*
    protected void onStop() {
        writeJSONData();
        super.onStop();
    }
*/
    protected void onResume() {
        readJSONData(stocks);
        List<String> symbolList = new ArrayList<>();
        for(Stock stock : stocks){
            symbolList.add(stock.getSymbol());
        }
        for (int i = 0; i<symbolList.size(); i++) {
            StockDownloader stockDownloader = new StockDownloader(this, symbolList.get(i));
            new Thread(stockDownloader).start();
        }
        super.onResume();
    }

    public void addStock(Stock stock) {
        if (stock == null) {
            badDataAlert(selection);
            return;
        }

        if (stocks.contains(stock)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setMessage(stock.getName() + " is already displayed");
            builder.setTitle("Duplicate Stock");
            builder.setIcon(R.drawable.error);

            AlertDialog dialog = builder.create();
            dialog.show();
            return;
        }

        stocks.add(stock);
        Collections.sort(stocks);
        stockAdapter.notifyDataSetChanged();
    }

    private void badDataAlert(String symbol) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("No data for stock selection");
        builder.setTitle("Symbol Not Found: " + symbol);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onClick(View v) {
        int pos = recyclerView.getChildLayoutPosition(v);
        Stock stock = stocks.get(pos);
        String symbol = stock.getSymbol();
        //String url = marketWatchURL + stock.getSymbol();
        //Intent i = new Intent(MainActivity.this, StockDetailActivity.class);
       // i.putExtra(Stock.class.getName(), stock);
        //startActivity(i);
        String url = "http://www.marketwatch.com/investing/stock/" + symbol;

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }

    @Override
    public boolean onLongClick(View v) {
        final int pos = recyclerView.getChildLayoutPosition(v);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.delete);
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                stocks.remove(pos);
                stockAdapter.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });

        builder.setMessage("Delete " + stocks.get(pos).getName() + "?");
        builder.setTitle("Delete Stock");

        AlertDialog dialog = builder.create();
        dialog.show();
        return true;
    }

    private boolean checkNetworkConnection() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    private void writeJSONData() {
        try {
            FileOutputStream fos = getApplicationContext().
                    openFileOutput(getString(R.string.stock_data_file), Context.MODE_PRIVATE);
            JsonWriter writer = new JsonWriter(new OutputStreamWriter(fos, StandardCharsets.UTF_8));
            writer.setIndent("  ");
            writer.beginArray();
            for (Stock s : stocks) {
                writer.name("symbol").value(s.getSymbol());
                writer.name("name").value(s.getName());
                writer.name("latestPrice").value(s.getPrice());
                writer.name("change").value(s.getChange());
                writer.name("changePercent").value(s.getPercentage());
                writer.endObject();
            }
            writer.endArray();
            writer.close();

        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "writeJSONData: " + e.getMessage());
        }
    }

    private void readJSONData(List<Stock> stocks) {
        try {
            FileInputStream fis = getApplicationContext().openFileInput(getString(R.string.stock_data_file));
            byte[] data = new byte[fis.available()];
            int loaded = fis.read(data);
            Log.d(TAG, "readJSONData: Loaded " + loaded + " bytes");
            fis.close();
            String json = new String(data);

            JSONArray noteArray = new JSONArray(json);
            for (int i = 0; i < noteArray.length(); i++) {
                JSONObject stockObject = noteArray.getJSONObject(i);
                String symbol = stockObject.getString("symbol");
                String name = stockObject.getString("name");
                double latestPrice = stockObject.getDouble("latestPrice");
                double change = stockObject.getDouble("change");
                double changePercent = stockObject.getDouble("changePercent");

                Stock stock = new Stock(symbol, name, latestPrice, change, changePercent);
                stocks.add(stock);
            }
            stockAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}