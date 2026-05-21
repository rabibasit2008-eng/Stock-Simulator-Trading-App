package com.StockSim.GUI;

import com.StockSim.Model.*;
import com.StockSim.Service.*;
import com.StockSim.Storage.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class StockSimApp extends JFrame {
    // ── Services ──────────────────────────────────────────────────────────
    private final AuthService     authService    = new AuthService();
    private final StockApiService stockApi       = new StockApiService();
    private final TradingService  tradingService = new TradingService(stockApi);

    // ── Colors: neutral black / charcoal (no blue-gray “slate” wash) ───────
    static final Color BG        = new Color(8, 8, 10);
    static final Color SURFACE   = new Color(16, 16, 18);
    static final Color SURFACE2  = new Color(28, 28, 31);
    static final Color SURFACE3  = new Color(44, 44, 48);
    static final Color BORDER    = new Color(58, 58, 64);
    static final Color BORDER2   = new Color(76, 76, 84);
    static final Color TEXT      = new Color(244, 244, 246);
    static final Color TEXT_SUB  = new Color(176, 176, 184);
    static final Color TEXT_MUTE = new Color(118, 118, 128);
    static final Color GREEN     = new Color(52, 211, 153);
    static final Color GREEN_BG  = new Color(14, 42, 34);
    static final Color RED       = new Color(251, 113, 133);
    static final Color RED_BG    = new Color(48, 18, 22);
    static final Color BLUE      = new Color(138, 170, 255);
    static final Color BLUE_DIM  = new Color(26, 32, 52);
    static final Color GOLD      = new Color(234, 192, 92);
    static final Color PURPLE    = new Color(180, 165, 245);
    static final Color CHART_LINE= new Color(138, 170, 255);
    static final Color CHART_FILL= new Color(138, 170, 255, 42);
    /** Alternating table row (matches {@link #styledTable}). */
    static final Color ROW_ALT   = new Color(20, 20, 24);

    private static final String UI_FAMILY;
    private static final String MONO_FAMILY;
    static final Font F_DISPLAY;
    static final Font F_TITLE;
    static final Font F_HEADING;
    static final Font F_LABEL;
    static final Font F_BODY;
    static final Font F_SMALL;
    static final Font F_MONO;
    static final Font F_MONO_SM;
    static final Font F_NUM_BIG;
    static final Font F_NUM_MED;
    static {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Set<String> avail = new HashSet<>(Arrays.asList(ge.getAvailableFontFamilyNames()));
        String ui = Font.DIALOG;
        for (String n : new String[]{"Segoe UI", "Segoe UI Variable", "Inter", "Dialog"}) {
            if (avail.contains(n)) { ui = n; break; }
        }
        String mono = Font.MONOSPACED;
        for (String n : new String[]{"Cascadia Mono", "Consolas", "JetBrains Mono", "Monospaced"}) {
            if (avail.contains(n)) { mono = n; break; }
        }
        UI_FAMILY = ui;
        MONO_FAMILY = mono;
        F_DISPLAY = new Font(UI_FAMILY, Font.BOLD, 26);
        F_TITLE   = new Font(UI_FAMILY, Font.BOLD, 20);
        F_HEADING = new Font(UI_FAMILY, Font.BOLD, 14);
        F_LABEL   = new Font(UI_FAMILY, Font.BOLD, 12);
        F_BODY    = new Font(UI_FAMILY, Font.PLAIN, 13);
        F_SMALL   = new Font(UI_FAMILY, Font.PLAIN, 11);
        F_MONO    = new Font(MONO_FAMILY, Font.PLAIN, 13);
        F_MONO_SM = new Font(MONO_FAMILY, Font.PLAIN, 11);
        F_NUM_BIG = new Font(UI_FAMILY, Font.BOLD, 30);
        F_NUM_MED = new Font(UI_FAMILY, Font.BOLD, 20);
    }

    // ── State ─────────────────────────────────────────────────────────────
    private User   currentUser;
    private final CardLayout cards = new CardLayout();
    private final JPanel     root  = new JPanel(cards);
    private Runnable jumpToTrade = () -> {};
    private JTextField authUserField;
    private JPasswordField authPassField;

    // ── Watchlist ─────────────────────────────────────────────────────────
    private static final String[] WATCHLIST = {
            "AAPL","MSFT","GOOGL","AMZN","NVDA","TSLA","META","NFLX","AMD","JPM"
    };

    // ── Full stock universe ───────────────────────────────────────────────
    private static final String[][] ALL_STOCKS = {
            {"AAPL","Apple Inc.","Technology"},           {"MSFT","Microsoft Corp.","Technology"},
            {"GOOGL","Alphabet Inc.","Technology"},       {"AMZN","Amazon.com Inc.","Consumer"},
            {"NVDA","NVIDIA Corp.","Technology"},          {"TSLA","Tesla Inc.","Automotive"},
            {"META","Meta Platforms Inc.","Technology"},  {"NFLX","Netflix Inc.","Entertainment"},
            {"AMD","Advanced Micro Devices","Technology"},{"JPM","JPMorgan Chase & Co.","Finance"},
            {"V","Visa Inc.","Finance"},                  {"MA","Mastercard Inc.","Finance"},
            {"DIS","Walt Disney Co.","Entertainment"},    {"PYPL","PayPal Holdings Inc.","Finance"},
            {"INTC","Intel Corp.","Technology"},           {"CSCO","Cisco Systems Inc.","Technology"},
            {"ADBE","Adobe Inc.","Technology"},            {"CRM","Salesforce Inc.","Technology"},
            {"ORCL","Oracle Corp.","Technology"},          {"QCOM","Qualcomm Inc.","Technology"},
            {"IBM","IBM Corp.","Technology"},              {"UBER","Uber Technologies","Transport"},
            {"LYFT","Lyft Inc.","Transport"},              {"SQ","Block Inc.","Finance"},
            {"SHOP","Shopify Inc.","Technology"},          {"SPOT","Spotify Technology","Entertainment"},
            {"SNAP","Snap Inc.","Technology"},             {"PINS","Pinterest Inc.","Technology"},
            {"ROKU","Roku Inc.","Technology"},             {"ZM","Zoom Video Comms.","Technology"},
            {"BA","Boeing Co.","Aerospace"},               {"GS","Goldman Sachs Group","Finance"},
            {"MS","Morgan Stanley","Finance"},             {"WMT","Walmart Inc.","Retail"},
            {"TGT","Target Corp.","Retail"},               {"COST","Costco Wholesale","Retail"},
            {"NKE","Nike Inc.","Consumer"},                {"SBUX","Starbucks Corp.","Consumer"},
            {"MCD","McDonald's Corp.","Consumer"},         {"KO","Coca-Cola Co.","Consumer"},
            {"PEP","PepsiCo Inc.","Consumer"},             {"JNJ","Johnson & Johnson","Healthcare"},
            {"PFE","Pfizer Inc.","Healthcare"},             {"MRNA","Moderna Inc.","Healthcare"},
            {"UNH","UnitedHealth Group","Healthcare"},     {"XOM","Exxon Mobil Corp.","Energy"},
            {"CVX","Chevron Corp.","Energy"},               {"ABNB","Airbnb Inc.","Travel"},
            {"COIN","Coinbase Global","Finance"},           {"PLTR","Palantir Technologies","Technology"},
    };

    // ── Entry ─────────────────────────────────────────────────────────────
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                UIManager.put("ToolTip.background", SURFACE2);
                UIManager.put("ToolTip.foreground", TEXT);
                UIManager.put("ToolTip.border", BorderFactory.createLineBorder(BORDER, 1));
                UIManager.put("ScrollBar.width", 10);
            } catch (Exception ignored) {}
            new StockSimApp().setVisible(true);
        });
    }

    public StockSimApp() {
        super("StockSim");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1180, 760);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG);
        getContentPane().add(root);
        root.setBackground(BG);
        root.add(buildAuthPanel(), "auth");
        cards.show(root, "auth");
    }

    // ══════════════════════════════════════════════════════════════════════
    //  AUTH PANEL
    // ══════════════════════════════════════════════════════════════════════
    private JPanel buildAuthPanel() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(BG);

        JPanel card = new JPanel();
        card.setBackground(SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1),
                new EmptyBorder(40, 36, 40, 36)));
        card.setPreferredSize(new Dimension(400, 520));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel logo = lbl("StockSim", F_DISPLAY, BLUE); logo.setAlignmentX(CENTER_ALIGNMENT);
        JLabel sub  = lbl("Log in or register. No real money.", F_SMALL, TEXT_MUTE); sub.setAlignmentX(CENTER_ALIGNMENT);

        JToggleButton btnLogin    = authTab("Sign In",true);
        JToggleButton btnRegister = authTab("Register",false);
        new ButtonGroup(){{ add(btnLogin); add(btnRegister); }};
        JPanel tabBar = new JPanel(new GridLayout(1, 2, 4, 0));
        tabBar.setOpaque(false);
        tabBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        tabBar.setAlignmentX(Component.CENTER_ALIGNMENT);
        tabBar.add(btnLogin); tabBar.add(btnRegister);

        authUserField = styledField("Username", Component.CENTER_ALIGNMENT);
        JTextField userField = authUserField;
        userField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        authPassField = styledPass(Component.CENTER_ALIGNMENT);
        JPasswordField passField = authPassField;

        // Password show/hide eye button
        JToggleButton eyeBtn = new JToggleButton("···") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setColor(isSelected()?SURFACE3:SURFACE2); g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
                g2.setColor(BORDER); g2.setStroke(new BasicStroke(1f)); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,8,8);
                g2.dispose(); super.paintComponent(g);
            }
        };
        eyeBtn.setFont(F_SMALL); eyeBtn.setForeground(TEXT_SUB); eyeBtn.setOpaque(false);
        eyeBtn.setToolTipText("Show password");
        eyeBtn.setContentAreaFilled(false); eyeBtn.setBorderPainted(false); eyeBtn.setFocusPainted(false);
        eyeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        eyeBtn.setPreferredSize(new Dimension(44, 40));
        eyeBtn.addActionListener(e -> {
            passField.setEchoChar(eyeBtn.isSelected() ? (char) 0 : '\u2022');
            eyeBtn.setToolTipText(eyeBtn.isSelected() ? "Hide password" : "Show password");
        });

        JPanel passRow = new JPanel(new BorderLayout(4,0));
        passRow.setOpaque(false); passRow.setMaximumSize(new Dimension(Integer.MAX_VALUE,40));
        passRow.add(passField, BorderLayout.CENTER); passRow.add(eyeBtn, BorderLayout.EAST);
        passRow.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel errLabel = lbl(" ",F_BODY,RED); errLabel.setAlignmentX(CENTER_ALIGNMENT);
        JButton submitBtn = primaryBtn("Sign In", Color.WHITE, new Color(42, 72, 168));
        submitBtn.setAlignmentX(CENTER_ALIGNMENT); submitBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE,42));

        boolean[] isReg = {false};
        btnLogin.addActionListener(e    -> { isReg[0]=false; submitBtn.setText("Sign In");        errLabel.setText(" "); });
        btnRegister.addActionListener(e -> { isReg[0]=true;  submitBtn.setText("Create Account"); errLabel.setText(" "); });

        ActionListener doAuth = e -> {
            String un = userField.getText().trim(), pw = new String(passField.getPassword());
            errLabel.setText(" ");
            try {
                currentUser = isReg[0] ? authService.register(un,pw) : authService.login(un,pw);
                showMainPanel();
            } catch (Exception ex) { errLabel.setText(ex.getMessage()); }
        };
        submitBtn.addActionListener(doAuth); passField.addActionListener(doAuth);

        card.add(logo); card.add(Box.createVerticalStrut(4)); card.add(sub);
        card.add(Box.createVerticalStrut(28)); card.add(tabBar);
        card.add(Box.createVerticalStrut(24)); card.add(fieldLbl("Username", Component.CENTER_ALIGNMENT));
        card.add(Box.createVerticalStrut(5));  card.add(userField);
        card.add(Box.createVerticalStrut(14)); card.add(fieldLbl("Password", Component.CENTER_ALIGNMENT));
        card.add(Box.createVerticalStrut(5));  card.add(passRow);
        card.add(Box.createVerticalStrut(8));  card.add(errLabel);
        card.add(Box.createVerticalStrut(18)); card.add(submitBtn);
        outer.add(card);
        return outer;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  MAIN SHELL
    // ══════════════════════════════════════════════════════════════════════
    private void showMainPanel() {
        JPanel main = new JPanel(new BorderLayout()); main.setBackground(BG);

        CardLayout cc = new CardLayout();
        JPanel content = new JPanel(cc); content.setBackground(BG);

        // Trade panel first so tradeSymHolder is available when other panels reference jumpToTrade
        JTextField[] tradeSymHolder = {null};
        JPanel tradePanel     = buildTradePanel(tradeSymHolder);
        JPanel marketPanel    = buildMarketPanel(cc, content, tradeSymHolder);
        JPanel portfolioPanel = buildPortfolioPanel();
        JPanel browsePanel    = buildBrowsePanel(cc, content, tradeSymHolder);
        JPanel historyPanel   = buildHistoryPanel();

        content.add(marketPanel,    "market");
        content.add(portfolioPanel, "portfolio");
        content.add(browsePanel,    "browse");
        content.add(tradePanel,     "trade");
        content.add(historyPanel,   "history");
        cc.show(content, "market");

        main.add(buildSidebar(cc, content), BorderLayout.WEST);
        main.add(content,                   BorderLayout.CENTER);
        main.add(buildStatusBar(),           BorderLayout.SOUTH);

        root.add(main, "main");
        cards.show(root, "main");
    }

    // ══════════════════════════════════════════════════════════════════════
    //  STATUS BAR
    // ══════════════════════════════════════════════════════════════════════
    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(BORDER);
                g2.drawLine(0, 0, getWidth(), 0);
                g2.dispose();
            }
        };
        bar.setBackground(SURFACE);
        bar.setBorder(new EmptyBorder(8, 22, 8, 22));
        bar.setPreferredSize(new Dimension(0, 34));

        JPanel liveDot = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(GREEN);
                g2.fillOval(getWidth() / 2 - 4, getHeight() / 2 - 4, 8, 8);
                g2.dispose();
            }
        };
        liveDot.setOpaque(false);
        liveDot.setPreferredSize(new Dimension(18, 18));

        JLabel statusLbl = lbl("OK", F_SMALL, TEXT_MUTE);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT,6,0)); left.setOpaque(false);
        left.add(liveDot); left.add(statusLbl);
        bar.add(left, BorderLayout.WEST);

        JLabel clock = lbl("",F_MONO_SM,TEXT_MUTE);
        bar.add(clock, BorderLayout.EAST);
        javax.swing.Timer clk = new javax.swing.Timer(1000,
                e -> clock.setText(java.time.LocalTime.now().toString().substring(0,8) + "  " + currentUser.getUsername()));
        clk.setInitialDelay(0); clk.start();
        return bar;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  SIDEBAR
    // ══════════════════════════════════════════════════════════════════════
    private JPanel buildSidebar(CardLayout cc, JPanel content) {
        JPanel sidebar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(BORDER);
                g2.drawLine(getWidth() - 1, 0, getWidth() - 1, getHeight());
                g2.dispose();
            }
        };
        sidebar.setBackground(SURFACE);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(224, 0));

        JPanel logoArea = new JPanel(new BorderLayout());
        logoArea.setBackground(SURFACE);
        logoArea.setBorder(new EmptyBorder(22, 20, 18, 16));
        logoArea.setMaximumSize(new Dimension(Integer.MAX_VALUE, 76));
        JPanel brand = new JPanel();
        brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));
        brand.setOpaque(false);
        brand.add(lbl("StockSim", F_TITLE, TEXT));
        logoArea.add(brand, BorderLayout.CENTER);
        sidebar.add(logoArea);
        JPanel sep = new JPanel();
        sep.setBackground(BORDER);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setPreferredSize(new Dimension(0, 1));
        sidebar.add(sep);
        sidebar.add(Box.createVerticalStrut(8));

        String[][] nav = {
                {"Market", "market"}, {"Portfolio", "portfolio"},
                {"Browse", "browse"}, {"Trade", "trade"}, {"History", "history"}
        };
        ButtonGroup navGroup = new ButtonGroup();
        JToggleButton[] navBtns = new JToggleButton[nav.length];
        for (int i = 0; i < nav.length; i++) {
            navBtns[i] = navButton(nav[i][0]);
            navGroup.add(navBtns[i]);
            final int idx = i;
            navBtns[i].addActionListener(e -> {
                cc.show(content, nav[idx][1]);
                for (JToggleButton b : navBtns) { b.setBackground(SURFACE); b.setForeground(TEXT_SUB); }
                navBtns[idx].setBackground(SURFACE3); navBtns[idx].setForeground(BLUE);
            });
            sidebar.add(navBtns[i]); sidebar.add(Box.createVerticalStrut(4));
        }
        navBtns[0].setSelected(true); navBtns[0].setBackground(SURFACE3); navBtns[0].setForeground(BLUE);
        sidebar.add(Box.createVerticalGlue());

        // Wire global jumpToTrade hook (used by market ticker cards & browse rows)
        jumpToTrade = () -> {
            cc.show(content,"trade");
            for (JToggleButton b : navBtns) { b.setBackground(SURFACE); b.setForeground(TEXT_SUB); }
            navBtns[3].setSelected(true); navBtns[3].setBackground(SURFACE3); navBtns[3].setForeground(BLUE);
        };

        sidebar.add(new JSeparator(){{ setForeground(BORDER); setMaximumSize(new Dimension(Integer.MAX_VALUE,1)); }});

        // User area
        JPanel userArea = new JPanel(); userArea.setLayout(new BoxLayout(userArea,BoxLayout.Y_AXIS));
        userArea.setBackground(SURFACE); userArea.setBorder(new EmptyBorder(14, 20, 20, 16));
        userArea.setMaximumSize(new Dimension(Integer.MAX_VALUE,100));

        JPanel avatar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BLUE_DIM); g2.fillOval(0,0,34,34);
                g2.setColor(BLUE); g2.setFont(F_HEADING);
                FontMetrics fm=g2.getFontMetrics();
                String ch=currentUser.getUsername().substring(0,1).toUpperCase();
                g2.drawString(ch,(34-fm.stringWidth(ch))/2,(34-fm.getHeight())/2+fm.getAscent()); g2.dispose();
            }
        };
        avatar.setPreferredSize(new Dimension(34,34)); avatar.setOpaque(false);

        JPanel nameStack = new JPanel(); nameStack.setLayout(new BoxLayout(nameStack,BoxLayout.Y_AXIS));
        nameStack.setBackground(SURFACE); nameStack.setBorder(new EmptyBorder(0,10,0,0));
        nameStack.add(lbl(currentUser.getUsername(), F_LABEL, TEXT));

        JPanel avatarRow = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
        avatarRow.setBackground(SURFACE); avatarRow.setMaximumSize(new Dimension(Integer.MAX_VALUE,40));
        avatarRow.add(avatar); avatarRow.add(nameStack);

        JButton logoutBtn = new JButton("Sign Out") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed()?RED.darker():RED_BG); g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
                g2.setColor(RED); g2.setStroke(new BasicStroke(1f)); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,8,8);
                g2.dispose(); super.paintComponent(g);
            }
        };
        logoutBtn.setFont(F_SMALL); logoutBtn.setForeground(RED); logoutBtn.setOpaque(false);
        logoutBtn.setContentAreaFilled(false); logoutBtn.setBorderPainted(false); logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE,30));
        logoutBtn.addActionListener(e -> { SessionManager.logout(); currentUser=null; if(authUserField!=null) authUserField.setText(""); if(authPassField!=null) authPassField.setText(""); cards.show(root,"auth"); });

        userArea.add(avatarRow); userArea.add(Box.createVerticalStrut(10)); userArea.add(logoutBtn);
        sidebar.add(userArea);
        return sidebar;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  MARKET PANEL
    // ══════════════════════════════════════════════════════════════════════
    private JPanel buildMarketPanel(CardLayout cc, JPanel content, JTextField[] tradeSymHolder) {
        JPanel p = bg(new BorderLayout());
        p.add(pageHeader("Market",
                currentUser.getUsername() + " - watchlist every 60s"), BorderLayout.NORTH);

        JPanel heroBanner = new JPanel(new BorderLayout());
        heroBanner.setBackground(SURFACE);
        heroBanner.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1),
                new EmptyBorder(18, 22, 18, 22)));

        JLabel heroBal = lbl("…", F_NUM_BIG, TEXT);
        JPanel heroLeft = bp(BoxLayout.Y_AXIS);
        heroLeft.add(lbl("Total", F_HEADING, TEXT_SUB));
        heroLeft.add(Box.createVerticalStrut(8));
        heroLeft.add(heroBal);
        heroLeft.add(Box.createVerticalStrut(4));
        heroLeft.add(lbl("Cash + stock", F_SMALL, TEXT_MUTE));

        String[][] quickActions = {{"Trade", "trade"}, {"Portfolio", "portfolio"}, {"Browse", "browse"}};
        JPanel heroRight = new JPanel(new GridLayout(1, 3, 12, 0));
        heroRight.setOpaque(false);
        heroRight.setBorder(new EmptyBorder(4, 24, 4, 0));
        for (String[] qa : quickActions) heroRight.add(quickActionCard(qa[0], qa[1], cc, content));
        heroBanner.add(heroLeft,BorderLayout.WEST); heroBanner.add(heroRight,BorderLayout.EAST);

        JPanel heroWrap = bg(new BorderLayout());
        heroWrap.setBorder(new EmptyBorder(16, 26, 10, 26));
        heroWrap.add(heroBanner);
        new SwingWorker<String,Void>(){
            protected String doInBackground(){try{return fmt(tradingService.getPortfolioValue(currentUser));}catch(Exception e){return "$10,000.00";}}
            protected void done(){try{heroBal.setText(get());}catch(Exception ignored){}}
        }.execute();

        // Ticker grid
        JPanel tickHdr = new JPanel(new BorderLayout());
        tickHdr.setBackground(BG);
        tickHdr.setBorder(new EmptyBorder(12, 26, 4, 26));
        tickHdr.add(lbl("Watchlist", F_HEADING, TEXT), BorderLayout.WEST);
        tickHdr.add(lbl(WATCHLIST.length + " tickers", F_SMALL, TEXT_MUTE), BorderLayout.EAST);
        JPanel tickerGrid = new JPanel(new GridLayout(2, 5, 12, 12));
        tickerGrid.setBackground(BG);
        tickerGrid.setBorder(new EmptyBorder(8, 26, 18, 26));
        Map<String,JLabel> priceLabels=new LinkedHashMap<>(), changeLabels=new LinkedHashMap<>();
        for (String sym : WATCHLIST) tickerGrid.add(tickerCard(sym,priceLabels,changeLabels,tradeSymHolder));

        // Market data table
        JPanel trendHdr = new JPanel(new BorderLayout());
        trendHdr.setBackground(BG);
        trendHdr.setBorder(new EmptyBorder(8, 26, 4, 26));
        JPanel trendTitleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        trendTitleRow.setOpaque(false);
        trendTitleRow.add(lbl("All rows", F_HEADING, TEXT));
        JLabel lastRefresh = lbl("…", F_SMALL, TEXT_MUTE);
        trendHdr.add(trendTitleRow,BorderLayout.WEST); trendHdr.add(lastRefresh,BorderLayout.EAST);

        DefaultTableModel mktModel = new DefaultTableModel(new String[]{"Symbol","Price","Change","% Change"},0){
            public boolean isCellEditable(int r,int c){return false;}
        };
        JTable mktTable = styledTable(mktModel);
        mktTable.setAutoCreateRowSorter(true);
        int[] mw={100,120,120,100}; for(int i=0;i<mw.length;i++) mktTable.getColumnModel().getColumn(i).setPreferredWidth(mw[i]);

        JPanel tableWrap = bg(new BorderLayout(0, 8));
        tableWrap.setBorder(new EmptyBorder(4, 26, 26, 26));
        tableWrap.add(trendHdr,BorderLayout.NORTH); tableWrap.add(styledScroll(mktTable),BorderLayout.CENTER);

        Map<String,Double> prevPrices = new HashMap<>();
        Runnable doRefresh = () -> new SwingWorker<Void,Void>(){
            Stock[] stocks = new Stock[WATCHLIST.length];
            protected Void doInBackground(){for(int i=0;i<WATCHLIST.length;i++) stocks[i]=stockApi.fetchStock(WATCHLIST[i]); return null;}
            protected void done(){
                mktModel.setRowCount(0);
                for(int i=0;i<WATCHLIST.length;i++){
                    Stock s=stocks[i]; String sym=WATCHLIST[i];
                    if(s==null){priceLabels.get(sym).setText("—");changeLabels.get(sym).setText("N/A");continue;}
                    double cur=s.getCurrentPrice(), prev=prevPrices.getOrDefault(sym,cur), diff=cur-prev, pct=prev==0?0:(diff/prev)*100;
                    prevPrices.put(sym,cur);
                    priceLabels.get(sym).setText("$"+String.format("%.2f",cur));
                    JLabel cl=changeLabels.get(sym);
                    if(diff>=0){cl.setText("+"+String.format("%.2f",diff));cl.setForeground(GREEN);}
                    else       {cl.setText(String.format("%.2f",diff));cl.setForeground(RED);}
                    mktModel.addRow(new Object[]{sym,"$"+String.format("%.2f",cur),
                            (diff>=0?"+":"")+String.format("%.2f",diff),(pct>=0?"+":"")+String.format("%.2f",pct)+"%"});
                }
                colorColumn(mktTable,2,s->s.startsWith("-")?RED:GREEN,SwingConstants.RIGHT);
                colorColumn(mktTable,3,s->s.startsWith("-")?RED:GREEN,SwingConstants.RIGHT);
                lastRefresh.setText("Updated "+java.time.LocalTime.now().toString().substring(0,8));
            }
        }.execute();
        doRefresh.run();
        new javax.swing.Timer(60_000, e->doRefresh.run()).start();

        JPanel topHalf = bp(BoxLayout.Y_AXIS); topHalf.setBackground(BG);
        topHalf.add(heroWrap); topHalf.add(tickHdr); topHalf.add(tickerGrid);
        JPanel center = bg(new BorderLayout()); center.add(topHalf,BorderLayout.NORTH); center.add(tableWrap,BorderLayout.CENTER);
        JScrollPane outerScroll = new JScrollPane(center); outerScroll.setBorder(null); outerScroll.getViewport().setBackground(BG);
        p.add(outerScroll,BorderLayout.CENTER);
        return p;
    }

    private JPanel quickActionCard(String label, String target, CardLayout cc, JPanel content) {
        JPanel card = new JPanel(new BorderLayout());
        card.setOpaque(true);
        card.setBackground(SURFACE2);
        card.setBorder(new CompoundBorder(new LineBorder(BORDER, 1), new EmptyBorder(12, 10, 12, 10)));
        JLabel lb = lbl(label, F_BODY, TEXT);
        lb.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(lb, BorderLayout.CENTER);
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                card.setBackground(SURFACE3);
                card.setBorder(new CompoundBorder(new LineBorder(BLUE, 1), new EmptyBorder(12, 10, 12, 10)));
            }
            public void mouseExited(MouseEvent e) {
                card.setBackground(SURFACE2);
                card.setBorder(new CompoundBorder(new LineBorder(BORDER, 1), new EmptyBorder(12, 10, 12, 10)));
            }
            public void mouseClicked(MouseEvent e) { cc.show(content, target); }
        });
        return card;
    }

    private JPanel tickerCard(String sym, Map<String,JLabel> priceMap, Map<String,JLabel> changeMap, JTextField[] tradeSymHolder) {
        final float[] glow = {0f};
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(interp(SURFACE, SURFACE3, glow[0]));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(interp(BORDER, BORDER2, glow[0]));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
            }
        };
        card.setOpaque(false); card.setLayout(new BoxLayout(card,BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(12,14,12,14)); card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        startHover(card,glow);
        // Click: pre-fill symbol and jump to Trade
        card.addMouseListener(new MouseAdapter(){
            public void mouseClicked(MouseEvent e){
                if(tradeSymHolder[0]!=null) tradeSymHolder[0].setText(sym);
                jumpToTrade.run();
            }
        });
        JLabel priceLabel=lbl("—",F_NUM_MED,TEXT), changeLabel=lbl("—",F_SMALL,TEXT_MUTE);
        priceMap.put(sym,priceLabel); changeMap.put(sym,changeLabel);
        card.add(lbl(sym,F_LABEL,TEXT_SUB)); card.add(Box.createVerticalStrut(6));
        card.add(priceLabel); card.add(Box.createVerticalStrut(3)); card.add(changeLabel);
        return card;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  PORTFOLIO PANEL
    // ══════════════════════════════════════════════════════════════════════
    private static final int AUTO_REFRESH_SECS = 30;

    private JPanel buildPortfolioPanel() {
        JPanel p = bg(new BorderLayout());
        p.add(pageHeader("Portfolio", "Refreshes every " + AUTO_REFRESH_SECS + "s"), BorderLayout.NORTH);

        JPanel metricsRow = new JPanel(new GridLayout(1,4,14,0)); metricsRow.setBackground(BG); metricsRow.setBorder(new EmptyBorder(20,24,0,24));
        JLabel balVal=metricNum("$0.00"), holdVal=metricNum("$0.00"), totalVal=metricNum("$0.00"), plVal=metricNum("$0.00");
        metricsRow.add(metricCard("Cash balance",   balVal,  BLUE,   "$"));
        metricsRow.add(metricCard("Holdings value", holdVal, PURPLE, "Σ"));
        metricsRow.add(metricCard("Total value",    totalVal, GREEN, "◆"));
        metricsRow.add(metricCard("Total P&L",      plVal,   GOLD,   "Δ"));

        SparklinePanel sparkline = new SparklinePanel();
        sparkline.setPreferredSize(new Dimension(0,110)); sparkline.setBorder(new EmptyBorder(16,24,0,24));

        String[] portCols = {"Symbol","Shares","Avg Cost","Current","Mkt Value","Unrealized P&L","Weight"};
        DefaultTableModel model = new DefaultTableModel(portCols,0){ public boolean isCellEditable(int r,int c){return false;} };
        JTable table = styledTable(model); table.setAutoCreateRowSorter(true);
        table.getColumnModel().getColumn(0).setPreferredWidth(80); table.getColumnModel().getColumn(6).setPreferredWidth(70);
        JScrollPane scroll = styledScroll(table);

        JLabel countdown = lbl("Refresh 30s", F_SMALL, TEXT_MUTE);
        JButton refreshBtn = ghostBtn("Refresh");
        JPanel ctrlRow = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0)); ctrlRow.setBackground(BG); ctrlRow.add(countdown); ctrlRow.add(refreshBtn);
        JPanel tableHdr = new JPanel(new BorderLayout()); tableHdr.setBackground(BG);
        tableHdr.add(lbl("Open Positions",F_HEADING,TEXT),BorderLayout.WEST); tableHdr.add(ctrlRow,BorderLayout.EAST);
        JPanel tableWrap = bg(new BorderLayout(0,10)); tableWrap.setBorder(new EmptyBorder(16,24,8,24));
        tableWrap.add(tableHdr,BorderLayout.NORTH); tableWrap.add(scroll,BorderLayout.CENTER);

        JPanel breakdown = new JPanel(new GridLayout(1,2,14,0)); breakdown.setBackground(BG); breakdown.setBorder(new EmptyBorder(8,24,16,24));
        JLabel realLbl=metricNum("$0.00"), unrealLbl=metricNum("$0.00");
        breakdown.add(infoCard("Realized P&L", realLbl));
        breakdown.add(infoCard("Unrealized P&L", unrealLbl));

        List<Double> totalHistory = new ArrayList<>();
        Runnable doRefresh = () -> {
            refreshBtn.setEnabled(false);
            new SwingWorker<Void,Void>(){
                double bal,holdV,realPL,unrealPL; Object[][] rows;
                protected Void doInBackground(){
                    stockApi.clearCache();
                    PortfolioStore ps=new PortfolioStore(); HoldingStore hs=new HoldingStore();
                    bal=ps.getBalance(currentUser.getUsername()); realPL=ps.getRealizedPL(currentUser.getUsername());
                    List<Holding> holdings=hs.loadAll(currentUser.getUsername()); holdV=0; unrealPL=0;
                    double totalMv=0;
                    for(Holding h:holdings){ Stock s=stockApi.fetchStock(h.getSymbol()); if(s!=null) totalMv+=s.getCurrentPrice()*h.getQuantity(); }
                    rows=new Object[holdings.size()][7];
                    for(int i=0;i<holdings.size();i++){
                        Holding h=holdings.get(i); Stock s=stockApi.fetchStock(h.getSymbol());
                        double cur=s!=null?s.getCurrentPrice():0, mv=cur*h.getQuantity(), upl=h.getUnrealizedPL(cur);
                        holdV+=mv; unrealPL+=upl;
                        rows[i]=new Object[]{h.getSymbol(),h.getQuantity(),fmt(h.getAveragePrice()),s!=null?fmt(cur):"—",fmt(mv),fmtPL(upl),String.format("%.1f%%",totalMv>0?(mv/totalMv)*100:0)};
                    }
                    return null;
                }
                protected void done(){
                    double total=bal+holdV; totalHistory.add(total); if(totalHistory.size()>60) totalHistory.remove(0);
                    sparkline.setData(totalHistory);
                    balVal.setText(fmt(bal)); holdVal.setText(fmt(holdV)); totalVal.setText(fmt(total));
                    double tpl=realPL+unrealPL;
                    plVal.setText(fmtPL(tpl)); plVal.setForeground(tpl>=0?GREEN:RED);
                    realLbl.setText(fmtPL(realPL)); realLbl.setForeground(realPL>=0?GREEN:RED);
                    unrealLbl.setText(fmtPL(unrealPL)); unrealLbl.setForeground(unrealPL>=0?BLUE:RED);
                    model.setRowCount(0); for(Object[] row:rows) model.addRow(row);
                    colorColumn(table,5,s->s.startsWith("-")||s.startsWith("−")?RED:GREEN,SwingConstants.RIGHT);
                    showEmptyState(table,scroll,"No positions. Use Trade to buy.");
                    refreshBtn.setEnabled(true);
                }
            }.execute();
        };
        refreshBtn.addActionListener(e->doRefresh.run());
        int[] secsLeft={AUTO_REFRESH_SECS};
        javax.swing.Timer ticker = new javax.swing.Timer(1000, null);
        ticker.addActionListener(e -> {
            secsLeft[0]--;
            if(secsLeft[0]<=0){ secsLeft[0]=AUTO_REFRESH_SECS; countdown.setForeground(BLUE); doRefresh.run(); }
            else countdown.setForeground(secsLeft[0]<=8?GOLD:TEXT_MUTE);
            countdown.setText("Refresh " + secsLeft[0] + "s");
        });
        ticker.start();
        p.addAncestorListener(new javax.swing.event.AncestorListener(){
            public void ancestorRemoved(javax.swing.event.AncestorEvent e){ ticker.stop(); }
            public void ancestorAdded(javax.swing.event.AncestorEvent e)  { if(!ticker.isRunning()) ticker.start(); }
            public void ancestorMoved(javax.swing.event.AncestorEvent e)  {}
        });
        doRefresh.run();

        JPanel lower = bg(new BorderLayout());
        lower.add(sparkline,BorderLayout.NORTH); lower.add(tableWrap,BorderLayout.CENTER); lower.add(breakdown,BorderLayout.SOUTH);
        JPanel center = bg(new BorderLayout()); center.add(metricsRow,BorderLayout.NORTH); center.add(lower,BorderLayout.CENTER);
        p.add(center,BorderLayout.CENTER);
        return p;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  TRADE PANEL
    // ══════════════════════════════════════════════════════════════════════
    private JPanel buildTradePanel(JTextField[] tradeSymHolder) {
        JPanel outer = bg(new BorderLayout());
        outer.add(pageHeader("Trade", "Buy / sell"), BorderLayout.NORTH);

        // ── Order card (left) ─────────────────────────────────────────────
        JPanel orderCard = glassCard(440,570);
        orderCard.setLayout(new BoxLayout(orderCard,BoxLayout.Y_AXIS));
        orderCard.setBorder(new EmptyBorder(32,30,32,30));

        JLabel heading = lbl("Order", F_HEADING, TEXT); heading.setAlignmentX(CENTER_ALIGNMENT);
        JTextField symField = styledField("Ticker  e.g. AAPL", Component.CENTER_ALIGNMENT);
        symField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40)); applyUpperCase(symField);
        tradeSymHolder[0] = symField;

        JSpinner qtySpinner = new JSpinner(new SpinnerNumberModel(0, 0, 100000, 1));
        styleSpinner(qtySpinner, Component.CENTER_ALIGNMENT);
        qtySpinner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JPanel quoteArea = new JPanel(new BorderLayout(12,0)){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(SURFACE2); g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
                g2.setColor(BORDER); g2.setStroke(new BasicStroke(1f)); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,10,10); g2.dispose();
            }
        };
        quoteArea.setOpaque(false);
        quoteArea.setBorder(new EmptyBorder(12, 16, 12, 16));
        quoteArea.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));
        quoteArea.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel quoteSym=lbl("—",F_HEADING,TEXT), quotePrice=lbl("Enter symbol & press Quote",F_BODY,TEXT_MUTE), quoteStale=lbl("",F_SMALL,GOLD);
        JPanel quoteRight=bp(BoxLayout.Y_AXIS); quoteRight.add(quotePrice); quoteRight.add(quoteStale);
        quoteArea.add(quoteSym,BorderLayout.WEST); quoteArea.add(quoteRight,BorderLayout.EAST);

        JLabel holdPreview = lbl(" ", F_SMALL, TEXT_MUTE); holdPreview.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel costEst = lbl(" ", F_BODY, TEXT_SUB);       costEst.setAlignmentX(Component.CENTER_ALIGNMENT);
        JButton quoteBtn = ghostBtn("Get Quote"), buyBtn = primaryBtn(" BUY ", Color.WHITE, new Color(18, 120, 86)), sellBtn = primaryBtn("SELL", Color.WHITE, new Color(168, 42, 52));
        JPanel btnRow = new JPanel(new GridLayout(1, 3, 10, 0));
        btnRow.setOpaque(false);
        btnRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btnRow.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnRow.add(quoteBtn); btnRow.add(buyBtn); btnRow.add(sellBtn);
        JLabel status=lbl(" ",F_BODY,TEXT_MUTE); status.setAlignmentX(CENTER_ALIGNMENT);
        final double[] lastPrice={0};

        // ── Portfolio sidebar (right) ──────────────────────────────────────
        DefaultTableModel portModel = new DefaultTableModel(new String[]{"Symbol","Shares","Avg Cost","Curr Price","Mkt Value","P&L"},0){
            public boolean isCellEditable(int r,int c){return false;}
        };
        JTable portTable = styledTable(portModel); portTable.setAutoCreateRowSorter(true);
        portTable.setFont(F_MONO_SM); portTable.setRowHeight(28);
        portTable.getTableHeader().setFont(new Font("Dialog",Font.BOLD,10));
        int[] pw={60,50,75,75,80,75}; for(int i=0;i<pw.length;i++) portTable.getColumnModel().getColumn(i).setPreferredWidth(pw[i]);

        JLabel sideBalLbl=lbl("$—",F_NUM_MED,TEXT), sideTotalLbl=lbl("$—",F_BODY,TEXT_SUB), sidePLLbl=lbl("$—",F_BODY,GREEN);
        JButton sideRefresh = ghostBtn("Refresh");
        sideRefresh.setFont(F_SMALL);

        JPanel sideTitle=new JPanel(new BorderLayout()); sideTitle.setBackground(BG);
        sideTitle.add(lbl("My Portfolio",F_HEADING,TEXT),BorderLayout.WEST); sideTitle.add(sideRefresh,BorderLayout.EAST);
        JPanel miniMetrics=new JPanel(new GridLayout(1,3,8,0)); miniMetrics.setBackground(BG); miniMetrics.setBorder(new EmptyBorder(10,0,0,0));
        miniMetrics.add(miniTile("Cash",sideBalLbl)); miniMetrics.add(miniTile("Total",sideTotalLbl)); miniMetrics.add(miniTile("P&L",sidePLLbl));
        JPanel sideHdr=new JPanel(new BorderLayout(0,2)); sideHdr.setBackground(BG); sideHdr.setBorder(new EmptyBorder(20,18,10,18));
        sideHdr.add(sideTitle,BorderLayout.NORTH); sideHdr.add(miniMetrics,BorderLayout.SOUTH);
        JPanel sideTableWrap=bg(new BorderLayout()); sideTableWrap.setBorder(new EmptyBorder(0,18,18,18)); sideTableWrap.add(styledScroll(portTable));

        JPanel sidebar = new JPanel(new BorderLayout()){
            @Override protected void paintComponent(Graphics g){
                super.paintComponent(g); Graphics2D g2=(Graphics2D)g.create(); g2.setColor(BORDER); g2.drawLine(0,0,0,getHeight()); g2.dispose();
            }
        };
        sidebar.setBackground(BG); sidebar.setPreferredSize(new Dimension(430,0));
        sidebar.add(sideHdr,BorderLayout.NORTH); sidebar.add(sideTableWrap,BorderLayout.CENTER);

        Runnable refreshPortfolio = () -> {
            sideRefresh.setEnabled(false);
            new SwingWorker<Void,Void>(){
                double bal,holdV,tpl; Object[][] rows;
                protected Void doInBackground(){
                    PortfolioStore ps=new PortfolioStore(); HoldingStore hs=new HoldingStore();
                    bal=ps.getBalance(currentUser.getUsername());
                    double realPL=ps.getRealizedPL(currentUser.getUsername());
                    List<Holding> holdings=hs.loadAll(currentUser.getUsername()); holdV=0; double unrealPL=0;
                    rows=new Object[holdings.size()][6];
                    for(int i=0;i<holdings.size();i++){
                        Holding h=holdings.get(i); Stock s=stockApi.fetchStock(h.getSymbol());
                        double cur=s!=null?s.getCurrentPrice():0, mv=cur*h.getQuantity(), upl=h.getUnrealizedPL(cur);
                        holdV+=mv; unrealPL+=upl;
                        rows[i]=new Object[]{h.getSymbol(),h.getQuantity(),fmt(h.getAveragePrice()),s!=null?fmt(cur):"—",fmt(mv),fmtPL(upl)};
                    }
                    tpl=realPL+unrealPL; return null;
                }
                protected void done(){
                    sideBalLbl.setText(fmt(bal)); sideTotalLbl.setText(fmt(bal+holdV));
                    sidePLLbl.setText(fmtPL(tpl)); sidePLLbl.setForeground(tpl>=0?GREEN:RED);
                    portModel.setRowCount(0); for(Object[] row:rows) portModel.addRow(row);
                    colorColumn(portTable,5,s->s.startsWith("-")||s.startsWith("−")?RED:GREEN,SwingConstants.RIGHT);
                    sideRefresh.setEnabled(true);
                }
            }.execute();
        };
        sideRefresh.addActionListener(e->refreshPortfolio.run());
        qtySpinner.addChangeListener(e->{ if(lastPrice[0]>0) costEst.setText("Estimated total: "+fmt(lastPrice[0]*(int)qtySpinner.getValue())); });

        quoteBtn.addActionListener(e->{
            String sym=symField.getText().trim().toUpperCase();
            if(sym.isEmpty()){status.setText("Enter a symbol first");status.setForeground(RED);return;}
            status.setText("Fetching live price…"); status.setForeground(TEXT_MUTE);
            quoteSym.setText(sym); quotePrice.setText("…"); quoteStale.setText("");
            new SwingWorker<Stock,Void>(){
                protected Stock doInBackground(){return stockApi.fetchStock(sym);}
                protected void done(){
                    try{
                        Stock s=get();
                        if(s!=null){
                            lastPrice[0]=s.getCurrentPrice();
                            quotePrice.setText("$"+String.format("%.2f",s.getCurrentPrice())); quotePrice.setForeground(BLUE);
                            if(s.isStale()) quoteStale.setText("Stale quote (cached)");
                            costEst.setText("Estimated total: "+fmt(s.getCurrentPrice()*(int)qtySpinner.getValue()));
                            status.setText("");
                            new HoldingStore().loadAll(currentUser.getUsername()).stream()
                                    .filter(h->h.getSymbol().equals(sym)).findFirst()
                                    .ifPresentOrElse(
                                            h->holdPreview.setText("You own "+h.getQuantity()+" shares @ avg "+fmt(h.getAveragePrice())),
                                            ()->holdPreview.setText("You don't hold "+sym));
                        } else {
                            quotePrice.setText("Not found"); quotePrice.setForeground(RED);
                            status.setText("Invalid symbol or API error"); status.setForeground(RED); lastPrice[0]=0;
                        }
                    } catch(Exception ex){status.setText(ex.getMessage());status.setForeground(RED);}
                }
            }.execute();
        });

        ActionListener tradeAct = e -> {
            String sym=symField.getText().trim().toUpperCase(); int qty=(int)qtySpinner.getValue(); boolean isBuy=e.getSource()==buyBtn;
            if(sym.isEmpty()){status.setText("Enter a symbol");status.setForeground(RED);return;}
            status.setText("Executing order…"); status.setForeground(TEXT_MUTE);
            new SwingWorker<String,Void>(){
                boolean ok; String msg;
                protected String doInBackground(){
                    try{
                        if(isBuy) tradingService.buyStock(currentUser,sym,qty); else tradingService.sellStock(currentUser,sym,qty);
                        ok=true; Stock s=stockApi.fetchStock(sym); double price=s!=null?s.getCurrentPrice():lastPrice[0];
                        msg=String.format("%s %d x %s @ %s, total %s",isBuy?"Bought":"Sold",qty,sym,fmt(price),fmt(price*qty));
                    } catch(Exception ex){ok=false;msg=ex.getMessage();}
                    return msg;
                }
                protected void done(){
                    status.setText(msg); status.setForeground(ok?GREEN:RED);
                    showToast(outer,msg,ok); if(ok) refreshPortfolio.run();
                }
            }.execute();
        };
        buyBtn.addActionListener(tradeAct); sellBtn.addActionListener(tradeAct);

        orderCard.add(heading); orderCard.add(Box.createVerticalStrut(24));
        orderCard.add(fieldLbl("Ticker symbol", Component.CENTER_ALIGNMENT)); orderCard.add(Box.createVerticalStrut(5)); orderCard.add(symField);
        orderCard.add(Box.createVerticalStrut(14)); orderCard.add(fieldLbl("Quantity", Component.CENTER_ALIGNMENT)); orderCard.add(Box.createVerticalStrut(5)); orderCard.add(qtySpinner);
        orderCard.add(Box.createVerticalStrut(4));  orderCard.add(costEst);
        orderCard.add(Box.createVerticalStrut(14)); orderCard.add(fieldLbl("Live quote", Component.CENTER_ALIGNMENT)); orderCard.add(Box.createVerticalStrut(5)); orderCard.add(quoteArea);
        orderCard.add(Box.createVerticalStrut(4));  orderCard.add(holdPreview);
        orderCard.add(Box.createVerticalStrut(20)); orderCard.add(btnRow); orderCard.add(Box.createVerticalStrut(10)); orderCard.add(status);

        JPanel leftWrap=new JPanel(new GridBagLayout()); leftWrap.setBackground(BG);
        GridBagConstraints gbc=new GridBagConstraints(); gbc.insets=new Insets(0,24,0,24); gbc.anchor=GridBagConstraints.CENTER;
        leftWrap.add(orderCard,gbc);

        JPanel body=bg(new BorderLayout()); body.add(leftWrap,BorderLayout.CENTER); body.add(sidebar,BorderLayout.EAST);
        outer.add(body,BorderLayout.CENTER);
        refreshPortfolio.run();
        return outer;
    }

    private JPanel miniTile(String label, JLabel valueLabel) {
        JPanel tile=new JPanel(){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(SURFACE); g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
                g2.setColor(BORDER); g2.setStroke(new BasicStroke(1f)); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,8,8); g2.dispose();
            }
        };
        tile.setOpaque(false); tile.setLayout(new BoxLayout(tile,BoxLayout.Y_AXIS)); tile.setBorder(new EmptyBorder(8,10,8,10));
        tile.add(lbl(label,F_SMALL,TEXT_MUTE)); tile.add(Box.createVerticalStrut(3)); tile.add(valueLabel);
        return tile;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  HISTORY PANEL
    // ══════════════════════════════════════════════════════════════════════
    private JPanel buildHistoryPanel() {
        JPanel p = bg(new BorderLayout());
        p.add(pageHeader("History", "Old trades"), BorderLayout.NORTH);

        JPanel statsBar=new JPanel(new GridLayout(1,3,14,0)); statsBar.setBackground(BG); statsBar.setBorder(new EmptyBorder(20,24,12,24));
        JLabel totalTrades=metricNum("0"), buyCount=metricNum("0"), sellCount=metricNum("0");
        statsBar.add(infoCard("Total trades", totalTrades));
        statsBar.add(infoCard("Buy orders", buyCount));
        statsBar.add(infoCard("Sell orders", sellCount));

        JTextField filterField=styledField("Filter by symbol…"); filterField.setPreferredSize(new Dimension(180,34));
        JButton loadBtn=ghostBtn("Load");
        JToggleButton showAll=filterToggle("All"), showBuy=filterToggle("Buy"), showSell=filterToggle("Sell");
        new ButtonGroup(){{ add(showAll); add(showBuy); add(showSell); }}; showAll.setSelected(true);
        JPanel filterRow=new JPanel(new FlowLayout(FlowLayout.LEFT,8,0)); filterRow.setBackground(BG);
        filterRow.add(filterField); filterRow.add(loadBtn); filterRow.add(Box.createHorizontalStrut(12));
        filterRow.add(showAll); filterRow.add(showBuy); filterRow.add(showSell);
        JPanel filterWrap=bg(new BorderLayout()); filterWrap.setBorder(new EmptyBorder(8,24,8,24));
        filterWrap.add(lbl("Transaction Log",F_HEADING,TEXT),BorderLayout.WEST); filterWrap.add(filterRow,BorderLayout.EAST);

        DefaultTableModel model=new DefaultTableModel(new String[]{"Date & Time","Type","Symbol","Qty","Price / Share","Total Value"},0){
            public boolean isCellEditable(int r,int c){return false;}
        };
        JTable table=styledTable(model); table.setAutoCreateRowSorter(true);
        table.getColumnModel().getColumn(0).setPreferredWidth(160); table.getColumnModel().getColumn(1).setPreferredWidth(60);
        JPanel tableWrap=bg(new BorderLayout()); tableWrap.setBorder(new EmptyBorder(0,24,24,24)); tableWrap.add(styledScroll(table));

        Runnable load=()->{
            String sym=filterField.getText().trim().toUpperCase();
            new SwingWorker<List<Transaction>,Void>(){
                protected List<Transaction> doInBackground(){
                    return sym.isEmpty()?tradingService.getHistory(currentUser):tradingService.getHistoryBySymbol(currentUser,sym);
                }
                protected void done(){
                    try{
                        List<Transaction> all=get(), filtered=new ArrayList<>();
                        for(Transaction tx:all){
                            if(showBuy.isSelected()&&tx.getType()!=Transaction.Type.BUY)  continue;
                            if(showSell.isSelected()&&tx.getType()!=Transaction.Type.SELL) continue;
                            filtered.add(tx);
                        }
                        long buys=all.stream().filter(tx->tx.getType()==Transaction.Type.BUY).count();
                        totalTrades.setText(String.valueOf(all.size())); buyCount.setText(String.valueOf(buys)); sellCount.setText(String.valueOf(all.size()-buys));
                        model.setRowCount(0);
                        for(Transaction tx:filtered) model.addRow(new Object[]{
                                tx.getTimestamp().toString().replace("T","  ").substring(0,19),
                                tx.getType().name(),tx.getSymbol(),tx.getQuantity(),fmt(tx.getPrice()),fmt(tx.getPrice()*tx.getQuantity())});
                        colorColumn(table,1,s->"BUY".equals(s)?GREEN:RED,SwingConstants.CENTER);
                    } catch(Exception ex){ex.printStackTrace();}
                }
            }.execute();
        };
        loadBtn.addActionListener(e->load.run());
        showAll.addActionListener(e->load.run()); showBuy.addActionListener(e->load.run()); showSell.addActionListener(e->load.run());
        filterField.addActionListener(e->load.run()); load.run();

        JPanel bodyPanel=bg(new BorderLayout()); bodyPanel.add(statsBar,BorderLayout.NORTH);
        JPanel mid=bg(new BorderLayout()); mid.add(filterWrap,BorderLayout.NORTH); mid.add(tableWrap,BorderLayout.CENTER);
        bodyPanel.add(mid,BorderLayout.CENTER);
        p.add(bodyPanel,BorderLayout.CENTER);
        return p;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  BROWSE PANEL
    // ══════════════════════════════════════════════════════════════════════
    private JPanel buildBrowsePanel(CardLayout cc, JPanel content, JTextField[] tradeSymHolder) {
        JPanel p = bg(new BorderLayout());
        p.add(pageHeader("Browse", "Double-click = open Trade with that ticker"), BorderLayout.NORTH);

        JTextField searchField=styledField("Search by symbol or company name…"); searchField.setPreferredSize(new Dimension(280,36));
        String[] sectors={"All","Technology","Finance","Consumer","Healthcare","Entertainment","Energy","Retail","Other"};
        JPanel chipRow=new JPanel(new FlowLayout(FlowLayout.LEFT,6,0)); chipRow.setOpaque(false);
        ButtonGroup sg=new ButtonGroup(); JToggleButton[] sectorBtns=new JToggleButton[sectors.length];
        for(int i=0;i<sectors.length;i++){ sectorBtns[i]=filterToggle(sectors[i]); sg.add(sectorBtns[i]); chipRow.add(sectorBtns[i]); }
        sectorBtns[0].setSelected(true);
        JPanel filterBar=new JPanel(new BorderLayout(12,0)); filterBar.setBackground(BG); filterBar.setBorder(new EmptyBorder(16,24,8,24));
        filterBar.add(searchField,BorderLayout.WEST); filterBar.add(chipRow,BorderLayout.CENTER);

        JLabel totalLbl=lbl(ALL_STOCKS.length+" stocks available",F_SMALL,TEXT_MUTE);
        JLabel statusLbl=lbl("Ready",F_SMALL,GREEN);
        JButton refreshBtn = ghostBtn("Fetch prices");
        JPanel refreshWrap=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0)); refreshWrap.setOpaque(false); refreshWrap.add(refreshBtn); refreshWrap.add(statusLbl);
        JPanel statsRow=new JPanel(new BorderLayout()); statsRow.setBackground(BG); statsRow.setBorder(new EmptyBorder(0,24,8,24));
        statsRow.add(totalLbl,BorderLayout.WEST); statsRow.add(refreshWrap,BorderLayout.EAST);

        DefaultTableModel model=new DefaultTableModel(new String[]{"Symbol","Company Name","Sector","Live Price","Status"},0){
            public boolean isCellEditable(int r,int c){return false;}
        };
        JTable table=styledTable(model); table.setAutoCreateRowSorter(true); table.setRowHeight(38);
        int[] bw={80,220,110,110,80}; for(int i=0;i<bw.length;i++) table.getColumnModel().getColumn(i).setPreferredWidth(bw[i]);

        // Price column renderer
        table.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer(){
            public Component getTableCellRendererComponent(JTable t,Object val,boolean sel,boolean foc,int row,int c){
                super.getTableCellRendererComponent(t,val,sel,foc,row,c);
                setForeground(BLUE); setFont(F_MONO); setHorizontalAlignment(RIGHT);
                setBackground(sel ? SURFACE3 : (row % 2 == 0 ? SURFACE : ROW_ALT)); return this;
            }
        });
        // Status column renderer
        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer(){
            public Component getTableCellRendererComponent(JTable t,Object val,boolean sel,boolean foc,int row,int c){
                super.getTableCellRendererComponent(t,val,sel,foc,row,c);
                String s=val==null?"":val.toString();
                setForeground("Live".equals(s)?GREEN:"Stale".equals(s)?GOLD:TEXT_MUTE);
                setFont(F_SMALL); setHorizontalAlignment(CENTER);
                setBackground(sel ? SURFACE3 : (row % 2 == 0 ? SURFACE : ROW_ALT)); return this;
            }
        });

        // Double-click → pre-fill Trade and jump there
        table.addMouseListener(new MouseAdapter(){
            public void mouseClicked(MouseEvent e){
                if(e.getClickCount()==2){
                    int row=table.convertRowIndexToModel(table.rowAtPoint(e.getPoint()));
                    if(row>=0){ if(tradeSymHolder[0]!=null) tradeSymHolder[0].setText((String)model.getValueAt(row,0)); jumpToTrade.run(); }
                }
            }
        });

        JPanel tableWrap=bg(new BorderLayout()); tableWrap.setBorder(new EmptyBorder(0,24,24,24)); tableWrap.add(styledScroll(table));

        Runnable applyFilter=()->{
            String q=searchField.getText().trim().toLowerCase(); String sel="All";
            for(JToggleButton b:sectorBtns){ if(b.isSelected()){sel=b.getText();break;} }
            model.setRowCount(0); int count=0;
            final String fsel=sel;
            for(String[] s:ALL_STOCKS){
                boolean mq=q.isEmpty()||s[0].toLowerCase().contains(q)||s[1].toLowerCase().contains(q);
                boolean ms="All".equals(fsel)||s[2].equals(fsel)||("Other".equals(fsel)&&!"Technology".equals(s[2])&&!"Finance".equals(s[2])&&!"Consumer".equals(s[2])&&!"Healthcare".equals(s[2])&&!"Entertainment".equals(s[2])&&!"Energy".equals(s[2])&&!"Retail".equals(s[2]));
                if(mq&&ms){ model.addRow(new Object[]{s[0],s[1],s[2],"...","..."}); count++; }
            }
            totalLbl.setText(count+" stocks shown");
        };
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener(){
            public void insertUpdate(javax.swing.event.DocumentEvent e){ SwingUtilities.invokeLater(applyFilter); }
            public void removeUpdate(javax.swing.event.DocumentEvent e){ SwingUtilities.invokeLater(applyFilter); }
            public void changedUpdate(javax.swing.event.DocumentEvent e){ SwingUtilities.invokeLater(applyFilter); }
        });
        for(JToggleButton b:sectorBtns) b.addActionListener(e->applyFilter.run());

        Map<String,Object[]> priceCache=new LinkedHashMap<>();
        Runnable fetchPrices=()->{
            refreshBtn.setEnabled(false); statusLbl.setText("Fetching prices…"); statusLbl.setForeground(GOLD);
            new SwingWorker<Void,int[]>(){
                protected Void doInBackground(){
                    for(int i=0;i<ALL_STOCKS.length;i++){
                        String sym=ALL_STOCKS[i][0]; Stock s=stockApi.fetchStock(sym);
                        priceCache.put(sym,s!=null?new Object[]{"$"+String.format("%.2f",s.getCurrentPrice()),s.isStale()?"Stale":"Live"}:new Object[]{"N/A","Error"});
                        publish(new int[]{i+1,ALL_STOCKS.length});
                    }
                    return null;
                }
                @Override protected void process(List<int[]> chunks){
                    int[] l=chunks.get(chunks.size()-1); statusLbl.setText("Fetching… "+l[0]+"/"+l[1]);
                }
                protected void done(){
                    for(int row=0;row<model.getRowCount();row++){
                        String sym=(String)model.getValueAt(row,0); Object[] d=priceCache.get(sym);
                        if(d!=null){model.setValueAt(d[0],row,3);model.setValueAt(d[1],row,4);}
                    }
                    refreshBtn.setEnabled(true); statusLbl.setText("Updated "+java.time.LocalTime.now().toString().substring(0,8)); statusLbl.setForeground(GREEN);
                }
            }.execute();
        };
        refreshBtn.addActionListener(e->fetchPrices.run());

        // Re-fetch every time this panel becomes visible
        p.addAncestorListener(new javax.swing.event.AncestorListener(){
            public void ancestorAdded(javax.swing.event.AncestorEvent e){ SwingUtilities.invokeLater(fetchPrices); }
            public void ancestorRemoved(javax.swing.event.AncestorEvent e){}
            public void ancestorMoved(javax.swing.event.AncestorEvent e){}
        });

        JPanel tipPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        tipPanel.setBackground(SURFACE2);
        tipPanel.setBorder(new CompoundBorder(new LineBorder(BORDER, 1), new EmptyBorder(8, 12, 8, 12)));
        tipPanel.add(lbl("Tip: double-click a row to trade that symbol.", F_SMALL, TEXT_SUB));
        JPanel tipWrap=bg(new BorderLayout()); tipWrap.setBorder(new EmptyBorder(0,24,16,24)); tipWrap.add(tipPanel);

        JPanel topArea=bg(new BorderLayout()); topArea.add(filterBar,BorderLayout.NORTH); topArea.add(statsRow,BorderLayout.SOUTH);
        JPanel body=bg(new BorderLayout()); body.add(topArea,BorderLayout.NORTH); body.add(tableWrap,BorderLayout.CENTER); body.add(tipWrap,BorderLayout.SOUTH);
        p.add(body,BorderLayout.CENTER);
        applyFilter.run();
        return p;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  SPARKLINE COMPONENT
    // ══════════════════════════════════════════════════════════════════════
    private static class SparklinePanel extends JPanel {
        private List<Double> data = new ArrayList<>();
        void setData(List<Double> d) { this.data=new ArrayList<>(d); repaint(); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2=(Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(BG); g2.fillRect(0,0,getWidth(),getHeight());
            Insets ins=getInsets(); int W=getWidth()-ins.left-ins.right,H=getHeight()-ins.top-ins.bottom,ox=ins.left,oy=ins.top;
            if(data.size()<2){
                g2.setColor(TEXT_MUTE); g2.setFont(F_SMALL);
                g2.drawString("Chart fills in after a refresh.", ox + 10, oy + H / 2); g2.dispose(); return;
            }
            double minV=data.stream().mapToDouble(v->v).min().orElse(0), maxV=data.stream().mapToDouble(v->v).max().orElse(1), range=maxV-minV==0?1:maxV-minV;
            int n=data.size(); float stepX=(float)W/(n-1);
            GeneralPath line=new GeneralPath(), fill=new GeneralPath();
            for(int i=0;i<n;i++){
                float x=ox+i*stepX, y=(float)(oy+H-((data.get(i)-minV)/range)*(H-4)-2);
                if(i==0){line.moveTo(x,y);fill.moveTo(x,oy+H);fill.lineTo(x,y);}
                else{line.lineTo(x,y);fill.lineTo(x,y);}
            }
            float lastX=ox+(n-1)*stepX; fill.lineTo(lastX,oy+H); fill.closePath();
            g2.setPaint(new GradientPaint(0, oy, CHART_FILL, 0, oy + H, new Color(CHART_LINE.getRed(), CHART_LINE.getGreen(), CHART_LINE.getBlue(), 0)));
            g2.fill(fill);
            g2.setColor(CHART_LINE); g2.setStroke(new BasicStroke(2f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND)); g2.draw(line);
            float lastY=(float)(oy+H-((data.get(n-1)-minV)/range)*(H-4)-2);
            g2.setColor(CHART_LINE); g2.fillOval((int)lastX-3,(int)lastY-3,6,6);
            g2.setColor(TEXT_MUTE); g2.setFont(F_MONO_SM);
            NumberFormat nf=NumberFormat.getCurrencyInstance(Locale.US);
            g2.drawString(nf.format(maxV),ox+4,oy+10); g2.drawString(nf.format(minV),ox+4,oy+H-2);
            g2.dispose();
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  UI HELPERS
    // ══════════════════════════════════════════════════════════════════════

    // -- Panel factories --
    private JPanel bg(LayoutManager lm)         { JPanel p=new JPanel(lm); p.setBackground(BG);      return p; }
    private JPanel bp(int axis)                  { JPanel p=new JPanel(); p.setLayout(new BoxLayout(p,axis)); p.setOpaque(false); return p; }

    private JPanel pageHeader(String title, String subtitle) {
        JPanel hdr = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(BORDER);
                g2.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
                g2.dispose();
            }
        };
        hdr.setBackground(SURFACE);
        hdr.setBorder(new EmptyBorder(20, 28, 18, 28));
        JPanel col = bp(BoxLayout.Y_AXIS);
        col.setOpaque(false);
        col.add(lbl(title, F_TITLE, TEXT));
        col.add(Box.createVerticalStrut(4));
        col.add(lbl(subtitle, F_SMALL, TEXT_MUTE));
        hdr.add(col, BorderLayout.WEST);
        return hdr;
    }

    private JPanel glassCard(int w, int h) {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(SURFACE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(BORDER);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(w, h));
        return card;
    }

    private JPanel metricCard(String label, JLabel valueLabel, Color accent, String icon) {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(SURFACE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(BORDER);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(14, 14, 14, 14));
        card.add(valueLabel);
        card.add(Box.createVerticalStrut(6));
        card.add(lbl(label, F_SMALL, TEXT_MUTE));
        return card;
    }

    private JPanel infoCard(String label, JLabel valueLabel) {
        JPanel card = new JPanel(new BorderLayout(0, 6)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(SURFACE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(BORDER);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(14, 18, 14, 18));
        card.add(lbl(label, F_SMALL, TEXT_MUTE), BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    // -- Label factories --
    private JLabel lbl(String t,Font f,Color c)  { JLabel l=new JLabel(t); l.setFont(f); l.setForeground(c); return l; }
    private JLabel metricNum(String t)            { return lbl(t,F_NUM_MED,TEXT); }
    private JLabel fieldLbl(String t) {
        return fieldLbl(t, Component.LEFT_ALIGNMENT);
    }

    private JLabel fieldLbl(String t, float alignmentX) {
        JLabel l = lbl(t, F_LABEL, TEXT_MUTE);
        l.setAlignmentX(alignmentX);
        return l;
    }

    // -- Input fields --
    private JTextField styledField(String hint) {
        return styledField(hint, Component.LEFT_ALIGNMENT);
    }

    private JTextField styledField(String hint, float alignmentX) {
        JTextField f = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(SURFACE2);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        f.setOpaque(false);
        f.setForeground(TEXT);
        f.setCaretColor(BLUE);
        f.setFont(F_BODY);
        f.setBorder(new CompoundBorder(new LineBorder(BORDER, 1, true), new EmptyBorder(6, 12, 6, 12)));
        f.setBackground(SURFACE2);
        f.putClientProperty("JTextField.placeholderText", hint);
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        f.setAlignmentX(alignmentX);
        return f;
    }

    private JPasswordField styledPass() {
        return styledPass(Component.LEFT_ALIGNMENT);
    }

    private JPasswordField styledPass(float alignmentX) {
        JPasswordField f = new JPasswordField();
        f.setForeground(TEXT);
        f.setCaretColor(BLUE);
        f.setFont(F_BODY);
        f.setBorder(new CompoundBorder(new LineBorder(BORDER, 1, true), new EmptyBorder(6, 12, 6, 12)));
        f.setBackground(SURFACE2);
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        f.setAlignmentX(alignmentX);
        return f;
    }

    private void styleSpinner(JSpinner s) {
        styleSpinner(s, Component.LEFT_ALIGNMENT);
    }

    private void styleSpinner(JSpinner s, float alignmentX) {
        s.setBackground(SURFACE2);
        s.setForeground(TEXT);
        s.setFont(F_BODY);
        s.setBorder(new LineBorder(BORDER, 1, true));
        JSpinner.DefaultEditor ed = (JSpinner.DefaultEditor) s.getEditor();
        JTextField tf = ed.getTextField();
        tf.setBackground(SURFACE2);
        tf.setForeground(TEXT);
        tf.setFont(F_BODY);
        tf.setCaretColor(BLUE);
        tf.setHorizontalAlignment(JTextField.LEFT);
        tf.setColumns(8);
        // Breathing room so digits are not clipped against spinner buttons
        tf.setBorder(new EmptyBorder(6, 12, 6, 16));
        ed.setBorder(BorderFactory.createEmptyBorder());
        s.setAlignmentX(alignmentX);
    }

    // -- Buttons --
    private JButton primaryBtn(String text,Color fg,Color bgColor) {
        JButton b=new JButton(text){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed()?bgColor.darker():bgColor); g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
                g2.setColor(fg.darker()); g2.setStroke(new BasicStroke(1f)); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,10,10); g2.dispose(); super.paintComponent(g);
            }
        };
        b.setFont(F_HEADING); b.setForeground(fg); b.setOpaque(false); b.setContentAreaFilled(false); b.setBorderPainted(false); b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); return b;
    }

    private JButton ghostBtn(String text) {
        final float[] hov={0f};
        JButton b=new JButton(text){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed()?SURFACE3:interp(SURFACE2,SURFACE3,hov[0])); g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
                g2.setColor(interp(BORDER2,BLUE,hov[0]*0.5f)); g2.setStroke(new BasicStroke(1f)); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,8,8); g2.dispose(); super.paintComponent(g);
            }
        };
        b.setFont(F_BODY); b.setForeground(TEXT_SUB); b.setOpaque(false); b.setContentAreaFilled(false); b.setBorderPainted(false); b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        startHover(b,hov);
        b.addMouseListener(new MouseAdapter(){ public void mouseEntered(MouseEvent e){b.setForeground(TEXT);} public void mouseExited(MouseEvent e){b.setForeground(TEXT_SUB);} });
        return b;
    }

    private JToggleButton navButton(String text) {
        JToggleButton b = new JToggleButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                if (isSelected()) {
                    g2.setColor(SURFACE3);
                    g2.fillRect(0, 0, getWidth(), getHeight());
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(F_BODY);
        b.setForeground(TEXT_SUB);
        b.setBackground(SURFACE);
        b.setBorder(new EmptyBorder(11, 22, 11, 18));
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        b.setHorizontalAlignment(SwingConstants.LEFT);
        return b;
    }

    private JToggleButton authTab(String text,boolean selected) {
        JToggleButton b=new JToggleButton(text,selected){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isSelected() ? SURFACE3 : SURFACE2);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose(); super.paintComponent(g);
            }
        };
        b.setFont(F_BODY); b.setForeground(selected?TEXT:TEXT_MUTE); b.setOpaque(false); b.setContentAreaFilled(false); b.setBorderPainted(false); b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); b.addChangeListener(e->b.setForeground(b.isSelected()?TEXT:TEXT_MUTE)); return b;
    }

    private JToggleButton filterToggle(String text) {
        JToggleButton b=new JToggleButton(text){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isSelected()?BLUE_DIM:SURFACE2); g2.fillRoundRect(0,0,getWidth(),getHeight(),6,6);
                g2.setColor(isSelected()?BLUE:BORDER); g2.setStroke(new BasicStroke(1f)); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,6,6); g2.dispose(); super.paintComponent(g);
            }
        };
        b.setFont(F_SMALL); b.setForeground(TEXT_SUB); b.setOpaque(false); b.setContentAreaFilled(false); b.setBorderPainted(false); b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); b.addChangeListener(e->b.setForeground(b.isSelected()?BLUE:TEXT_SUB)); return b;
    }

    // -- Table helpers --
    private JTable styledTable(DefaultTableModel model) {
        JTable t = new JTable(model) {
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                c.setBackground(isRowSelected(row) ? SURFACE3 : (row % 2 == 0 ? SURFACE : ROW_ALT));
                return c;
            }
        };
        t.setBackground(SURFACE);
        t.setForeground(TEXT);
        t.setFont(F_MONO);
        t.setRowHeight(36);
        t.setShowHorizontalLines(true);
        t.setShowVerticalLines(false);
        t.setGridColor(new Color(42, 42, 48));
        t.setSelectionBackground(SURFACE3);
        t.setSelectionForeground(TEXT);
        t.setFillsViewportHeight(true);
        t.setIntercellSpacing(new Dimension(14, 0));
        JTableHeader hdr = t.getTableHeader();
        hdr.setBackground(new Color(12, 12, 14));
        hdr.setForeground(TEXT_MUTE);
        hdr.setFont(F_LABEL);
        hdr.setBorder(new MatteBorder(0, 0, 1, 0, BORDER));
        hdr.setReorderingAllowed(false);
        ((DefaultTableCellRenderer) hdr.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);
        return t;
    }

    private JScrollPane styledScroll(JTable t) {
        JScrollPane s=new JScrollPane(t);
        s.getViewport().setBackground(SURFACE); s.setBorder(new LineBorder(BORDER,1));
        s.getVerticalScrollBar().setBackground(SURFACE2); s.getHorizontalScrollBar().setBackground(SURFACE2); return s;
    }

    /**
     * Unified column colour renderer — replaces old colorPLColumn / colorTypeColumn / colorSignColumn.
     */
    private void colorColumn(JTable t, int col, java.util.function.Function<String,Color> colorFn, int align) {
        t.getColumnModel().getColumn(col).setCellRenderer(new DefaultTableCellRenderer(){
            public Component getTableCellRendererComponent(JTable tbl,Object val,boolean sel,boolean foc,int row,int c){
                super.getTableCellRendererComponent(tbl,val,sel,foc,row,c);
                setForeground(colorFn.apply(val==null?"":val.toString()));
                setBackground(sel ? SURFACE3 : (row % 2 == 0 ? SURFACE : ROW_ALT));
                setHorizontalAlignment(align); setFont(F_MONO); return this;
            }
        });
    }

    /** Replaces the table in the viewport with an empty-state message when there are no rows. */
    private void showEmptyState(JTable table, JScrollPane scroll, String message) {
        if (table.getRowCount()==0) {
            JPanel p=new JPanel(new GridBagLayout()); p.setBackground(SURFACE); p.add(lbl(message,F_BODY,TEXT_MUTE));
            scroll.setViewportView(p);
        } else {
            scroll.setViewportView(table);
        }
    }

    private void applyUpperCase(JTextField f) {
        f.getDocument().addDocumentListener(new javax.swing.event.DocumentListener(){
            boolean upd=false;
            void run(){ if(upd)return; upd=true; String t=f.getText().toUpperCase(); int pos=f.getCaretPosition(); f.setText(t); f.setCaretPosition(Math.min(pos,t.length())); upd=false; }
            public void insertUpdate(javax.swing.event.DocumentEvent e){ SwingUtilities.invokeLater(this::run); }
            public void removeUpdate(javax.swing.event.DocumentEvent e){}
            public void changedUpdate(javax.swing.event.DocumentEvent e){}
        });
    }

    // -- Misc utils --
    private String fmt(double v)  { return NumberFormat.getCurrencyInstance(Locale.US).format(v); }
    private String fmtPL(double v){ return (v>=0?"+":"−")+fmt(Math.abs(v)); }
    private static Color interp(Color a,Color b,float t){
        return new Color(a.getRed()+(int)((b.getRed()-a.getRed())*t), a.getGreen()+(int)((b.getGreen()-a.getGreen())*t), a.getBlue()+(int)((b.getBlue()-a.getBlue())*t));
    }


    private void startHover(JComponent comp, float[] alpha) {
        comp.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { alpha[0] = 1f; comp.repaint(); }
            public void mouseExited(MouseEvent e)  { alpha[0] = 0f; comp.repaint(); }
        });
    }

    // ══════════════════════════════════════════════════════════════════════
    //  TOAST NOTIFICATION
    // ══════════════════════════════════════════════════════════════════════
    private void showToast(JComponent parent, String message, boolean success) {
        JLayeredPane layered=getLayeredPane();
        final float[] progress={0f}; final int[] phase={0}, hold={0};
        Color tbg = success ? new Color(14, 52, 40, 238) : new Color(52, 16, 20, 238);
        Color tbrd=success?GREEN:RED;

        JPanel toast=new JPanel(new FlowLayout(FlowLayout.LEFT,10,0)){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                int a2=phase[0]==2?(int)(progress[0]*230):230;
                g2.setColor(new Color(tbg.getRed(),tbg.getGreen(),tbg.getBlue(),a2)); g2.fillRoundRect(0,0,getWidth(),getHeight(),12,12);
                g2.setColor(new Color(tbrd.getRed(),tbrd.getGreen(),tbrd.getBlue(),phase[0]==2?(int)(progress[0]*200):200));
                g2.setStroke(new BasicStroke(1.5f)); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,12,12); g2.dispose();
            }
        };
        toast.setOpaque(false); toast.setBorder(new EmptyBorder(10,14,10,20));
        JLabel iconLbl=lbl(success?"+":"x",F_TITLE,tbrd), msgLbl=lbl(message,F_BODY,TEXT);
        toast.add(iconLbl); toast.add(msgLbl);

        Dimension pref=toast.getPreferredSize();
        int tw=Math.min(pref.width+20,layered.getWidth()-60), th=pref.height;
        int tx=(layered.getWidth()-tw)/2, tyTarget=layered.getHeight()-th-50, tyStart=layered.getHeight()+10;
        toast.setBounds(tx,tyStart,tw,th); layered.add(toast,JLayeredPane.POPUP_LAYER);

        new javax.swing.Timer(16,null){{
            addActionListener(e -> {
                switch(phase[0]){
                    case 0:
                        progress[0]=Math.min(1f,progress[0]+0.08f);
                        float ease=1f-(1f-progress[0])*(1f-progress[0]);
                        toast.setLocation(tx,tyStart+(int)((tyTarget-tyStart)*ease));
                        if(progress[0]>=1f){phase[0]=1;progress[0]=0;} break;
                    case 1:
                        if(++hold[0]>120){phase[0]=2;progress[0]=1f;} break;
                    case 2:
                        progress[0]=Math.max(0f,progress[0]-0.04f); toast.repaint();
                        int ia=(int)(progress[0]*255);
                        iconLbl.setForeground(new Color(tbrd.getRed(),tbrd.getGreen(),tbrd.getBlue(),ia));
                        msgLbl.setForeground(new Color(TEXT.getRed(),TEXT.getGreen(),TEXT.getBlue(),ia));
                        if(progress[0]<=0f){stop();layered.remove(toast);layered.repaint();} break;
                }
            }); start();
        }};
    }
}
