package com.StockSim.Service;

import com.StockSim.Model.Stock;

public interface IStockApiService {
    Stock fetchStock(String symbol);
    String getApiKey();
    void setApiKey(String key);
    void clearCache();
}