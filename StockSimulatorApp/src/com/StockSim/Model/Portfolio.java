package com.StockSim.Model;

/**
 * Portfolio data (balance, holdings, realized P&L, transactions) is persisted
 * entirely through the Storage layer:
 *   - PortfolioStore   → balance + realized P&L
 *   - HoldingStore     → current open positions
 *   - TransactionStore → full trade history
 *
 * This class is intentionally empty. It exists only so that any GUI code
 * that calls user.getPortfolio() still compiles while you upgrade the UI.
 * Do NOT add fields here — read/write through the stores instead.
 */
public class Portfolio {
    // Deliberately empty — see Storage layer.
}