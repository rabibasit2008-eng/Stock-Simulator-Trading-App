package com.StockSim.Storage;
import com.StockSim.Model.Holding;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads and writes holdings.txt (what the user currently owns).
 *
 * File format — one holding per line:
 *   symbol|quantity|averagePrice
 */

public class HoldingStore {
    private static final String SEP = "|";
    /** Overwrite the entire holdings file with the current list. */
    public void saveAll(String username, List<Holding> holdings) {
        List<String> lines = new ArrayList<>();
        for (Holding h : holdings) {
            lines.add(h.getSymbol() + SEP + h.getQuantity() + SEP + h.getAveragePrice());
        }
        FileHelper.writeLines(FileHelper.holdingsFile(username), lines);
    }
    /** Load all current holdings for a user. */
    public List<Holding> loadAll(String username) {
        List<Holding> holdings = new ArrayList<>();
        for (String line : FileHelper.readLines(FileHelper.holdingsFile(username))) {
            String[] p = line.split("\\" + SEP);
            if (p.length == 3) {
                holdings.add(new Holding(
                        p[0],
                        Integer.parseInt(p[1]),
                        Double.parseDouble(p[2])
                ));
            }
        }
        return holdings;
    }
}