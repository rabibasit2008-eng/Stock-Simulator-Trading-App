# Stock-Simulator-Trading-App

Paper-trade real stocks with live prices. Java Swing desktop app with user auth, portfolio tracking, P\&L analytics, and Finnhub API integration — no database required.



\# 📈 Stock Simulator App



A Java desktop application that lets users paper-trade real stocks using live market data. Built with Swing for the GUI, backed by flat-file persistence, and powered by the \[Finnhub](https://finnhub.io/) API for real-time prices.



\---



\## Features



\- \*\*User Authentication\*\* — Register and log in with BCrypt-hashed passwords

\- \*\*Live Stock Prices\*\* — Fetches real-time quotes from the Finnhub API with a 30-second cache

\- \*\*Buy \& Sell Trades\*\* — Execute trades against a $10,000 starting balance

\- \*\*Portfolio Dashboard\*\* — View current holdings, total portfolio value, and unrealized P\&L

\- \*\*Realized P\&L Tracking\*\* — See profit/loss on closed positions

\- \*\*Transaction History\*\* — Full trade log filterable by symbol

\- \*\*Watchlist\*\* — Quick-view panel for 10 major stocks (AAPL, MSFT, GOOGL, NVDA, TSLA, and more)

\- \*\*50+ Stock Universe\*\* — Browse and trade across Technology, Finance, Consumer, Healthcare, and more sectors

\- \*\*Dark UI\*\* — Polished dark-themed Swing interface with custom colors and fonts



\---



\## Project Structure



```

StockSimulatorApp/

├── src/

│   └── com/StockSim/

│       ├── GUI/

│       │   └── StockSimApp.java       # Main JFrame — all screens \& UI logic

│       ├── Model/

│       │   ├── User.java              # User entity

│       │   ├── Portfolio.java         # Placeholder (data lives in Storage layer)

│       │   ├── Holding.java           # Open position with avg-cost tracking

│       │   ├── Stock.java             # Stock quote (symbol, price, stale flag)

│       │   └── Transaction.java       # Immutable trade record (BUY/SELL)

│       ├── Service/

│       │   ├── AuthService.java       # Register / login via BCrypt

│       │   ├── StockApiService.java   # Finnhub HTTP client with in-memory cache

│       │   ├── TradingService.java    # Buy/sell logic with per-user locking

│       │   ├── SessionManager.java    # Singleton session state

│       │   └── I\*.java                # Interfaces for Auth, StockApi, Trading

│       └── Storage/

│           ├── FileHelper.java        # Read/write/append helpers + path constants

│           ├── UserStore.java         # users.txt (id|username|passwordHash)

│           ├── PortfolioStore.java    # portfolio.txt (balance + realizedPL)

│           ├── HoldingStore.java      # holdings.txt (open positions)

│           └── TransactionStore.java  # transactions.txt (full trade log)

├── data/

│   ├── users.txt                      # Persisted user accounts

│   └── portfolios/

│       └── <username>/

│           ├── portfolio.txt

│           ├── holdings.txt

│           └── transactions.txt

└── .idea/                             # IntelliJ IDEA project files

```



\---



\## Getting Started



\### Prerequisites



\- Java 17 or later

\- IntelliJ IDEA (recommended) or any Java IDE

\- A free \[Finnhub API key](https://finnhub.io/register)



\### Dependencies



The following JARs are included in `.idea/libraries/`:



| Library | Purpose |

|---|---|

| `jbcrypt-0.4.jar` | BCrypt password hashing |

| `json-20251224.jar` | JSON parsing for API responses |

| `json-simple-1.1.1.jar` | Lightweight JSON utilities |



\### Setup



1\. \*\*Clone the repository\*\*

&#x20;  ```bash

&#x20;  git clone https://github.com/your-username/StockSimulatorApp.git

&#x20;  cd StockSimulatorApp

&#x20;  ```



2\. \*\*Open in IntelliJ IDEA\*\*

&#x20;  — File → Open → select the `StockSimulatorApp` folder.

&#x20;  The `.idea/` project files are included, so libraries should resolve automatically.



3\. \*\*Add your Finnhub API key\*\*

&#x20;  The default key in `StockApiService.java` is a demo key and may be rate-limited. Replace it with your own:

&#x20;  ```java

&#x20;  // StockApiService.java

&#x20;  private volatile String apiKey = "YOUR\_FINNHUB\_API\_KEY";

&#x20;  ```

&#x20;  Alternatively, the key can be updated at runtime from within the app's settings panel.



4\. \*\*Run the app\*\*

&#x20;  Set `com.StockSim.GUI.StockSimApp` as the main class and run.



\### Data Directory



By default, all data files are saved to a `data/` folder relative to the working directory. You can override this with an environment variable or JVM property:



```bash

\# Environment variable

STOCKSIM\_DATA\_DIR=/path/to/data java -jar StockSimulatorApp.jar



\# JVM property

java -Dstocksim.data.dir=/path/to/data -jar StockSimulatorApp.jar

```



\---



\## Data Storage



All data is stored as plain-text files — no database required.



| File | Format |

|---|---|

| `users.txt` | `id\\|username\\|passwordHash` — one user per line |

| `portfolio.txt` | `balance=10000.0` / `realizedPL=0.0` |

| `holdings.txt` | `symbol\\|quantity\\|averagePrice` — one holding per line |

| `transactions.txt` | `id\\|symbol\\|qty\\|price\\|BUY\\|SELL\\|timestamp` — append-only |



\---



\## Architecture Notes



\- \*\*Layered design\*\* — GUI → Service → Storage, with interfaces (`IAuthService`, `ITradingService`, `IStockApiService`) keeping layers decoupled.

\- \*\*Thread safety\*\* — `TradingService` uses per-user `ReentrantLock` to prevent race conditions on concurrent trades.

\- \*\*Stale-price protection\*\* — If the Finnhub API call fails, the cached price is flagged as stale and trades are blocked rather than executed at a potentially wrong price.

\- \*\*Average-cost basis\*\* — `Holding.addShares()` correctly recalculates the weighted average price on each buy.



\---



\## License



This project is for educational purposes. Stock data is provided by \[Finnhub](https://finnhub.io/) — see their terms of service for usage limits.

