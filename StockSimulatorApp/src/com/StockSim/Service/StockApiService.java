package com.StockSim.Service;

import com.StockSim.Model.Stock;
import com.StockSim.Storage.FileHelper;
import org.json.JSONObject;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class StockApiService implements IStockApiService {

    // ── Config file location ───────────────────────────────────────────────
    // Sits next to the data/ folder: data/../stocksim.properties
    private static final String CONFIG_FILE = FileHelper.DATA_DIR + "/data/stocksim.properties";
    private static final String PROP_KEY    = "finnhub.api.key";
    private static final String ENV_KEY     = "FINNHUB_API_KEY";

    // ── Key loaded via fallback chain ──────────────────────────────────────
    private volatile String apiKey = loadApiKey();

    private static String loadApiKey() {
        // 1. Config file
        File cfg = new File(CONFIG_FILE);
        if (cfg.exists()) {
            try (InputStream in = new FileInputStream(cfg)) {
                Properties props = new Properties();
                props.load(in);
                String val = props.getProperty(PROP_KEY, "").trim();
                if (!val.isEmpty()) return val;
            } catch (IOException ignored) {}
        }
        // 2. Environment variable
        String env = System.getenv(ENV_KEY);
        if (env != null && !env.isBlank()) return env.trim();

        // 3. JVM property (e.g. -Dfinnhub.api.key=xxx)
        String prop = System.getProperty(PROP_KEY, "").trim();
        if (!prop.isEmpty()) return prop;

        // 4. No key found — GUI will prompt the user
        return "";
    }

    // ── IStockApiService ───────────────────────────────────────────────────
    @Override public String getApiKey() { return apiKey; }

    @Override
    public void setApiKey(String key) {
        this.apiKey = (key == null) ? "" : key.trim();
        saveApiKey(this.apiKey);
        clearCache();
    }

    /** Persist the key so it survives restarts. */
    private void saveApiKey(String key) {
        try {
            File cfg = new File(CONFIG_FILE);
            cfg.getParentFile().mkdirs();
            Properties props = new Properties();
            if (cfg.exists()) {
                try (InputStream in = new FileInputStream(cfg)) { props.load(in); }
            }
            props.setProperty(PROP_KEY, key);
            try (OutputStream out = new FileOutputStream(cfg)) {
                props.store(out, "StockSim configuration — do NOT commit this file");
            }
        } catch (IOException e) {
            System.err.println("Warning: could not save API key to config: " + e.getMessage());
        }
    }

    // ── HTTP + cache ───────────────────────────────────────────────────────
    private final HttpClient         client    = HttpClient.newHttpClient();
    private final Map<String, Stock> cache     = new ConcurrentHashMap<>();
    private final Map<String, Long>  cacheTime = new ConcurrentHashMap<>();
    private static final long CACHE_DURATION = 30_000;

    @Override
    public void clearCache() { cache.clear(); cacheTime.clear(); }

    @Override
    public Stock fetchStock(String symbol) {
        if (apiKey.isEmpty()) return null; // no key — caller must prompt user

        try {
            symbol = symbol.toUpperCase().trim();
            long now = System.currentTimeMillis();
            if (cache.containsKey(symbol) && now - cacheTime.get(symbol) < CACHE_DURATION)
                return cache.get(symbol);

            String url = "https://finnhub.io/api/v1/quote?symbol=" + symbol + "&token=" + apiKey;
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            JSONObject json  = new JSONObject(response.body());
            double     price = json.optDouble("c", 0.0);
            if (price == 0.0) throw new RuntimeException("Invalid symbol or no data: " + symbol);

            Stock stock = new Stock(symbol, symbol, price);
            cache.put(symbol, stock);
            cacheTime.put(symbol, now);
            return stock;

        } catch (Exception e) {
            System.out.println("API ERROR (" + symbol + "): " + e.getMessage());
            if (cache.containsKey(symbol)) {
                Stock stale = cache.get(symbol);
                return new Stock(stale.getSymbol(), stale.getName(), stale.getCurrentPrice(), true);
            }
            return null;
        }
    }
}