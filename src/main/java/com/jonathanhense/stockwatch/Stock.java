package com.jonathanhense.stockwatch;

import java.io.Serializable;
import java.util.Objects;

public class Stock implements  Serializable, Comparable<Stock> {

    private String symbol;
    private String name;
    private double price;
    private double change;
    private double percentage;


    public Stock(String symbol, String name, double price, double change, double percentage){
        this.symbol = symbol;
        this.name = name;
        this.price = price;
        this.change = change;
        this.percentage = percentage;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getName() {
        return name;
    }

   // public void setName(String name) {
        //this.name = name;
    //}

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getChange() {
        return change;
    }

    public void setChange(double change) {
        this.change = change;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Stock stock = (Stock) o;
        return name.equals(stock.name) &&
                symbol.equals(stock.symbol);
    }

    @Override
    public int hashCode(){
        return Objects.hash(symbol, name);
    }

    @Override
    public int compareTo(Stock stock) {
        return name.compareTo(stock.getName());
    }

}
