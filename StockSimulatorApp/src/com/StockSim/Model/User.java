package com.StockSim.Model;
import java.util.UUID;

public class User {
    private String id;
    private String username;
    private String passwordHash;
    private Portfolio portfolio;

    public User(String username, String passwordHash) {
        this.id = UUID.randomUUID().toString();
        this.username = username;
        this.passwordHash = passwordHash;
        this.portfolio = new Portfolio();
    }
    public User(String id, String username, String passwordHash) {
        this.id           = id;
        this.username     = username;
        this.passwordHash = passwordHash;
        this.portfolio    = new Portfolio();
    }

    public String    getId()           { return id; }
    public String    getUsername()     { return username; }
    public String    getPasswordHash() { return passwordHash; }
    public Portfolio getPortfolio()    { return portfolio; }

}

