package com.StockSim.Model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Transaction {

    public enum Type { BUY, SELL }

    private final String        id;
    private final String        symbol;
    private final int           quantity;
    private final double        price;
    private final Type          type;
    private final LocalDateTime timestamp;

    // Used when creating a NEW transaction
    public Transaction(String symbol, int quantity, double price, Type type) {
        if (symbol == null || symbol.isEmpty()) throw new IllegalArgumentException("Symbol cannot be empty");
        if (quantity <= 0)                      throw new IllegalArgumentException("Quantity must be positive");
        if (price <= 0)                         throw new IllegalArgumentException("Price must be positive");

        this.id        = UUID.randomUUID().toString();
        this.symbol    = symbol.toUpperCase();
        this.quantity  = quantity;
        this.price     = price;
        this.type      = type;
        this.timestamp = LocalDateTime.now();
    }

    // Used when LOADING a transaction back from the file
    public Transaction(String id, String symbol, int quantity, double price, Type type, LocalDateTime timestamp) {
        this.id        = id;
        this.symbol    = symbol;
        this.quantity  = quantity;
        this.price     = price;
        this.type      = type;
        this.timestamp = timestamp;
    }

    public String        getId()        { return id; }
    public String        getSymbol()    { return symbol; }
    public int           getQuantity()  { return quantity; }
    public double        getPrice()     { return price; }
    public Type          getType()      { return type; }
    public LocalDateTime getTimestamp() { return timestamp; }
}