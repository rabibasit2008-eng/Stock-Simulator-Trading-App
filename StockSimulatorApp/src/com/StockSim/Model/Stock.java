package com.StockSim.Model;
public class Stock {
    private String symbol;
    private String name;
    private double currentPrice;
    /** True when this price came from the stale cache after an API failure. */
    private boolean stale;

    public Stock(String symbol, String name, double currentPrice) {
        this(symbol, name, currentPrice, false);
    }

    public Stock(String symbol, String name, double currentPrice, boolean stale) {
        this.symbol       = symbol.toUpperCase();
        this.name         = name;
        this.currentPrice = currentPrice;
        this.stale        = stale;
    }

    public String  getSymbol()       { return symbol; }
    public String  getName()         { return name; }
    public double  getCurrentPrice() { return currentPrice; }
    /** True means the price is from a stale cache hit — the API call failed. */
    public boolean isStale()         { return stale; }
}