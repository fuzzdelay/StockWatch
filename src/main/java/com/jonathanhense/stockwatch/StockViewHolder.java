package com.jonathanhense.stockwatch;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class StockViewHolder extends RecyclerView.ViewHolder{
    public TextView symbol;
    public TextView name;
    public TextView price;
    public TextView change;

    public StockViewHolder(View view) {
        super(view);
        symbol = view.findViewById(R.id.symbolData);
        name = view.findViewById(R.id.nameData);
        price = view.findViewById(R.id.priceData);
        change = view.findViewById(R.id.changeData);

    }
}
