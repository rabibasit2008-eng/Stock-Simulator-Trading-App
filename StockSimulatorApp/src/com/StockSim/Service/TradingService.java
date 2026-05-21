package com.StockSim.Service;

import com.StockSim.Model.*;
import com.StockSim.Storage.*;

import java.util.List;

/**
 * Buy/sell trades — everything is persisted to .txt files immediately.
 */
public class TradingService implements ITradingService{
    private final java.util.concurrent.ConcurrentHashMap<String, java.util.concurrent.locks.ReentrantLock> userLocks =
            new java.util.concurrent.ConcurrentHashMap<>();

    private final StockApiService    stockApi;
    private final PortfolioStore     portfolioStore  = new PortfolioStore();
    private final HoldingStore       holdingStore    = new HoldingStore();
    private final TransactionStore   txStore         = new TransactionStore();
    private java.util.concurrent.locks.ReentrantLock lockFor(String username) {
        return userLocks.computeIfAbsent(username, k -> new java.util.concurrent.locks.ReentrantLock());
    }
    public TradingService(StockApiService stockApi) {
        this.stockApi = stockApi;
    }

    // ── Buy ────────────────────────────────────────────────────────────────

    public void buyStock(User user, String symbol, int quantity) {
        validate(user, symbol, quantity);
        symbol = symbol.toUpperCase().trim();
        final String sym = symbol;

        var lock = lockFor(user.getUsername());
        lock.lock();
        try {
            Stock  stock     = fetchOrThrow(sym);
            double price     = stock.getCurrentPrice();
            double totalCost = price * quantity;
            double balance   = portfolioStore.getBalance(user.getUsername());

            if (balance < totalCost)
                throw new RuntimeException("Insufficient balance");

            List<Holding> holdings = holdingStore.loadAll(user.getUsername());
            Holding holding = findHolding(holdings, sym);
            if (holding == null) {
                holding = new Holding(sym, 0, 0.0); // BUG-3 fix: 0.0 not price
                holdings.add(holding);
            }
            holding.addShares(quantity, price);
            holdingStore.saveAll(user.getUsername(), holdings);

            portfolioStore.save(
                    user.getUsername(),
                    balance - totalCost,
                    portfolioStore.getRealizedPL(user.getUsername())
            );

            txStore.save(user.getUsername(),
                    new Transaction(sym, quantity, price, Transaction.Type.BUY));
        } finally {
            lock.unlock();
        }
    }
    // ── Sell ───────────────────────────────────────────────────────────────

    public void sellStock(User user, String symbol, int quantity) {
        validate(user, symbol, quantity);
        symbol = symbol.toUpperCase().trim();
        final String sym = symbol;

        var lock = lockFor(user.getUsername());
        lock.lock();
        try {
            List<Holding> holdings = holdingStore.loadAll(user.getUsername());
            Holding holding = findHolding(holdings, sym);

            if (holding == null || holding.getQuantity() < quantity)
                throw new RuntimeException("Not enough shares to sell");

            // Read both balance and realizedPL BEFORE any writes so we never
            // re-read from disk mid-transaction (fixes non-atomic read-modify-write).
            double balance    = portfolioStore.getBalance(user.getUsername());
            double realizedPL = portfolioStore.getRealizedPL(user.getUsername());

            Stock  stock     = fetchOrThrow(sym);
            double price     = stock.getCurrentPrice();
            double totalGain = price * quantity;
            double profit    = (price - holding.getAveragePrice()) * quantity;

            holding.removeShares(quantity);
            if (holding.getQuantity() == 0) holdings.remove(holding);
            holdingStore.saveAll(user.getUsername(), holdings);

            portfolioStore.save(
                    user.getUsername(),
                    balance + totalGain,
                    realizedPL + profit
            );

            txStore.save(user.getUsername(),
                    new Transaction(sym, quantity, price, Transaction.Type.SELL));
        } finally {
            lock.unlock();
        }
    }
    // ── Queries ────────────────────────────────────────────────────────────

    public List<Transaction> getHistory(User user) {
        return txStore.loadAll(user.getUsername());
    }

    public List<Transaction> getHistoryBySymbol(User user, String symbol) {
        return txStore.loadBySymbol(user.getUsername(), symbol);
    }

    public double getPortfolioValue(User user) {
        double total = portfolioStore.getBalance(user.getUsername());
        for (Holding h : holdingStore.loadAll(user.getUsername())) {
            Stock s = stockApi.fetchStock(h.getSymbol());
            if (s != null) total += s.getCurrentPrice() * h.getQuantity();
        }
        return total;
    }

    public double getTotalPL(User user) {
        double unrealized = 0.0;
        for (Holding h : holdingStore.loadAll(user.getUsername())) {
            Stock s = stockApi.fetchStock(h.getSymbol());
            if (s != null) unrealized += h.getUnrealizedPL(s.getCurrentPrice());
        }
        return portfolioStore.getRealizedPL(user.getUsername()) + unrealized;
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private Holding findHolding(List<Holding> holdings, String symbol) {
        return holdings.stream()
                .filter(h -> h.getSymbol().equals(symbol))
                .findFirst().orElse(null);
    }

    private Stock fetchOrThrow(String symbol) {
        Stock s = stockApi.fetchStock(symbol);
        if (s == null) throw new RuntimeException("Could not fetch stock data for: " + symbol);
        if (s.isStale()) throw new RuntimeException(
                "Live price unavailable for " + symbol + " (API error). Trade blocked to avoid using a stale price.");
        return s;
    }

    private void validate(User user, String symbol, int quantity) {
        if (user == null)                       throw new IllegalArgumentException("User cannot be null");
        if (symbol == null || symbol.isBlank()) throw new IllegalArgumentException("Symbol cannot be empty");
        if (quantity <= 0)                      throw new IllegalArgumentException("Quantity must be positive");
    }
}