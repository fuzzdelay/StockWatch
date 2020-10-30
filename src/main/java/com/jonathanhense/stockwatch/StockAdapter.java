package com.jonathanhense.stockwatch;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

import static android.graphics.Color.GREEN;
import static android.graphics.Color.RED;

public class StockAdapter extends RecyclerView.Adapter<StockViewHolder> {

    private static final String TAG = "StockAdapter";
    private List<Stock> stocks;
    private MainActivity mainActivity;

    public StockAdapter(List<Stock> stocksList, MainActivity ma){
        this.stocks = stocksList;
        mainActivity = ma;
    }

    @NonNull
    @Override
    public StockViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: ");
        View itemView = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.stock_row, parent, false);
        itemView.setOnClickListener(mainActivity);
        itemView.setOnLongClickListener(mainActivity);

        return new StockViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull StockViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: ");
        Stock stockPick = stocks.get(position);

        holder.symbol.setText(stockPick.getSymbol());
        holder.name.setText(stockPick.getName());
        holder.price.setText(String.format("$%s", stockPick.getPrice()));
        holder.change.setText(String.format("%.2f (%.2f%%)", stockPick.getChange(), stockPick.getPercentage()));


        if(stockPick.getChange()>0){
            holder.change.setText( "▲ "+String.format(Locale.US, "%.2f", stockPick.getChange()) +"(" +
                    String.format( "%.2f", stockPick.getPercentage()) + "%)");
            holder.symbol.setTextColor(GREEN);
            holder.name.setTextColor(GREEN);
            holder.price.setTextColor(GREEN);
            holder.change.setTextColor(GREEN);
        }else{
            holder.change.setText( "▼ "+String.format(Locale.US, "%.2f", stockPick.getChange()) +"(" +
                    String.format( "%.2f", stockPick.getPercentage()) + "%)");
            holder.symbol.setTextColor(RED);
            holder.name.setTextColor(RED);
            holder.price.setTextColor(RED);
            holder.change.setTextColor(RED);
        }
    }

    @Override
    public int getItemCount() {
        return stocks.size();
    }
}
