package com.StockSim.Storage;
import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;


public class FileHelper {
    // Root folder where all data lives — change this if you want it elsewhere
    public static final String DATA_DIR = resolveDataDir();

    private static String resolveDataDir() {
        String dir = System.getenv("STOCKSIM_DATA_DIR");
        if (dir != null && !dir.isBlank()) return dir.trim();
        dir = System.getProperty("stocksim.data.dir");
        if (dir != null && !dir.isBlank()) return dir.trim();
        return "data";
    }

    /** Read every line from a file. Returns empty list if file doesn't exist yet. */
    public static List<String> readLines(String filePath) {
        List<String> lines = new ArrayList<>();
        File file = new File(filePath);
        if (!file.exists()) return lines;   // first run — no file yet, that's fine
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.isBlank()) lines.add(line);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file: " + filePath, e);
        }
        return lines;
    }
    /** Overwrite a file with a list of lines. Creates the file (and folders) if needed. */
    public static void writeLines(String filePath, List<String> lines) {
        ensureParentExists(filePath);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath, false))) {
            for (String line : lines) {
                bw.write(line);
                bw.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to write file: " + filePath, e);
        }
    }
    /** Append a single line to a file (used for transaction history). */
    public static void appendLine(String filePath, String line) {
        ensureParentExists(filePath);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath, true))) {
            bw.write(line);
            bw.newLine();
        } catch (IOException e) {
            throw new RuntimeException("Failed to append to file: " + filePath, e);
        }
    }
    /** Make sure the parent directory exists before writing. */
    private static void ensureParentExists(String filePath) {
        File parent = new File(filePath).getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
    }
    // ── Path helpers — one place that knows every file location ──────────
    public static String usersFile() {
        return DATA_DIR + "/users.txt";
    }
    public static String portfolioFile(String username) {
        return DATA_DIR + "/portfolios/" + username + "/portfolio.txt";
    }
    public static String holdingsFile(String username) {
        return DATA_DIR + "/portfolios/" + username + "/holdings.txt";
    }
    public static String transactionsFile(String username) {
        return DATA_DIR + "/portfolios/" + username + "/transactions.txt";
    }
}