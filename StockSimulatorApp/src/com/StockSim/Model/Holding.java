package com.StockSim.Model;

public class Holding{
    private String symbol;
    private int quantity;
    private double averagePrice;

    public Holding(String symbol, int quantity, double averagePrice) {
        this.symbol = symbol;
        this.quantity = quantity;
        this.averagePrice = averagePrice;
    }

    public String getSymbol() { return symbol; }
    public int getQuantity() { return quantity; }
    public double getAveragePrice() { return averagePrice; }

    public void addShares(int qty, double price) {
        double totalCost = (averagePrice * quantity) + (price * qty);
        quantity += qty;
        averagePrice = totalCost / quantity;
    }

    public void removeShares(int qty) {
        if (qty > quantity) {
            throw new RuntimeException("Not enough shares");
        }
        quantity -= qty;
    }
    public double getUnrealizedPL(double currentPrice) {
        return (currentPrice - averagePrice) * quantity;
    }
}

