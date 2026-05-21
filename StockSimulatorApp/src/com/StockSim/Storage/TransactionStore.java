package com.StockSim.Storage;
import com.StockSim.Model.Transaction;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads and writes transactions.txt (full trade history — never overwritten).
 * File format — one transaction per line, appended forever:
 *   id|symbol|quantity|price|type|timestamp
 */

public class TransactionStore {
    private static final String SEP = "|";
    /** Append one transaction to the end of the file. Never overwrites. */
    public void save(String username, Transaction tx) {
        String line = String.join(SEP,
                tx.getId(),
                tx.getSymbol(),
                String.valueOf(tx.getQuantity()),
                String.valueOf(tx.getPrice()),
                tx.getType().name(),
                tx.getTimestamp().toString()
        );
        FileHelper.appendLine(FileHelper.transactionsFile(username), line);
    }
    /** Load full history, newest first. */
    public List<Transaction> loadAll(String username) {
        List<Transaction> list = parse(username);
        // reverse so newest is first
        java.util.Collections.reverse(list);
        return list;
    }
    /** Load history filtered by symbol. */
    public List<Transaction> loadBySymbol(String username, String symbol) {
        final String upper = symbol.toUpperCase().trim();
        List<Transaction> result = new ArrayList<>();
        for (Transaction tx : loadAll(username)) {
            if (tx.getSymbol().equals(upper)) result.add(tx);
        }
        return result;
    }
    /** Load only BUY or only SELL transactions. */
    public List<Transaction> loadByType(String username, Transaction.Type type) {
        List<Transaction> result = new ArrayList<>();
        for (Transaction tx : loadAll(username)) {
            if (tx.getType() == type) result.add(tx);
        }
        return result;
    }
    // ── helper ────────────────────────────────────────────────────────────
    private List<Transaction> parse(String username) {
        List<Transaction> list = new ArrayList<>();
        for (String line : FileHelper.readLines(FileHelper.transactionsFile(username))) {
            String[] p = line.split("\\" + SEP, 6);
            if (p.length == 6) {
                list.add(new Transaction(
                        p[0],                               // id
                        p[1],                               // symbol
                        Integer.parseInt(p[2]),             // quantity
                        Double.parseDouble(p[3]),           // price
                        Transaction.Type.valueOf(p[4]),     // BUY or SELL
                        LocalDateTime.parse(p[5])           // timestamp
                ));
            }
        }
        return list;
    }
}