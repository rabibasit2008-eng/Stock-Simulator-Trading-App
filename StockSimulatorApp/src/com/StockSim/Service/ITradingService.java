package com.StockSim.Service;

import com.StockSim.Model.Transaction;
import com.StockSim.Model.User;
import java.util.List;

public interface ITradingService {
    void buyStock(User user, String symbol, int quantity);
    void sellStock(User user, String symbol, int quantity);
    List<Transaction> getHistory(User user);
    List<Transaction> getHistoryBySymbol(User user, String symbol);
    double getPortfolioValue(User user);
    double getTotalPL(User user);
}
