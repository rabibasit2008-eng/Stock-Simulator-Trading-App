package com.StockSim.Storage;

/**
 * Reads and writes portfolio.txt (balance + realized P&L only).
 * File format — always exactly 2 lines:
 *   balance=10000.0
 *   realizedPL=0.0
 * Holdings and transactions are handled by their own stores.
 */
public class PortfolioStore {

    /** Save (overwrite) balance and realized P&L for a user. */
    public void save(String username, double balance, double realizedPL) {
        java.util.List<String> lines = new java.util.ArrayList<>();
        lines.add("balance="    + balance);
        lines.add("realizedPL=" + realizedPL);
        FileHelper.writeLines(FileHelper.portfolioFile(username), lines);
    }

    /** Load balance. Returns 10000.0 (starting balance) if file doesn't exist yet. */
    public double getBalance(String username) {
        return readField(username, "balance=", 10000.0);
    }

    /** Load realized P&L. Returns 0.0 if file doesn't exist yet. */
    public double getRealizedPL(String username) {
        return readField(username, "realizedPL=", 0.0);
    }

    // ── helper ────────────────────────────────────────────────────────────

    private double readField(String username, String prefix, double defaultValue) {
        for (String line : FileHelper.readLines(FileHelper.portfolioFile(username))) {
            if (line.startsWith(prefix)) {
                return Double.parseDouble(line.substring(prefix.length()));
            }
        }
        return defaultValue;
    }
}