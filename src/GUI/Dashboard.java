package GUI;

import Models.User;
import Models.Department;
import database.Queries;
import utils.EnergyCalculator;
import utils.CarbonCalculator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class Dashboard extends JFrame {

    private final User currentUser;
    private JPanel     contentArea;
    private JLabel[]   kpiValues;          // 0=kWh 1=cost 2=co2 3=devices
    private JLabel     statusDot, statusTxt;

    // Panels (lazy-init)
    private DepartmentPanel    deptPanel;
    private DeviceEntryPanel   devicePanel;
    private SolarSimulationPanel solarPanel;
    private ReportsPanel       reportsPanel;
    private ChartsPanel        chartsPanel;
    private UsersPanel         usersPanel;

    // Sidebar buttons
    private JButton[] navBtns;
    private int        activeNav = 0;

    private static final String[] NAV_ICONS  = {"⊞","🏢","🔌","☀","📊","📈","👥"};
    private static final String[] NAV_LABELS = {"Home","Departments","Devices","Solar","Reports","Charts","Users"};

    public Dashboard(User user) {
        this.currentUser = user;
        setupWindow();
        buildUI();
        refreshKPIs();
        // Auto-refresh KPIs every 60s
        new Timer(60_000, e -> refreshKPIs()).start();
    }

    // ── Window ─────────────────────────────────────────────────
    private void setupWindow() {
        setTitle("SEUS — Smart Energy Optimization System");
        setSize(1280, 760);
        setMinimumSize(new Dimension(960, 600));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        try { setIconImage(Toolkit.getDefaultToolkit().createImage(getClass().getResource("/icon.png"))); } catch(Exception ignored){}
    }

    // ── Root layout ────────────────────────────────────────────
    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(0,0));
        root.setBackground(Theme.BG_DARK);

        root.add(buildSidebar(),    BorderLayout.WEST);
        root.add(buildMainArea(),   BorderLayout.CENTER);

        setContentPane(root);
    }

    // ── Sidebar ────────────────────────────────────────────────
    private JPanel buildSidebar() {
        JPanel sb = new JPanel(new BorderLayout(0,0));
        sb.setBackground(Theme.BG_SIDEBAR);
        sb.setPreferredSize(new Dimension(200, 760));
        sb.setBorder(BorderFactory.createMatteBorder(0,0,0,1,Theme.BORDER));

        // Logo area
        JPanel logo = new JPanel(new BorderLayout());
        logo.setOpaque(false);
        logo.setBorder(BorderFactory.createEmptyBorder(18,18,18,18));
        JLabel logoTxt = Theme.label("⚡ SEUS", new Font("Segoe UI",Font.BOLD,20), Theme.ACCENT);
        JLabel logoSub = Theme.label("Energy System", Theme.FONT_SMALL, Theme.TEXT_MUTED);
        JPanel logoInner = new JPanel(new GridLayout(2,1,0,0));
        logoInner.setOpaque(false);
        logoInner.add(logoTxt); logoInner.add(logoSub);
        logo.add(logoInner, BorderLayout.CENTER);
        logo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0,0,1,0,Theme.BORDER),
            BorderFactory.createEmptyBorder(16,18,16,18)));

        // Nav buttons
        JPanel nav = new JPanel(new GridLayout(NAV_LABELS.length, 1, 0, 2));
        nav.setOpaque(false);
        nav.setBorder(BorderFactory.createEmptyBorder(12,8,12,8));
        navBtns = new JButton[NAV_LABELS.length];
        // Hide Users tab for non-admins
        int total = currentUser.isAdmin() ? NAV_LABELS.length : NAV_LABELS.length - 1;
        for (int i = 0; i < total; i++) {
            final int idx = i;
            navBtns[i] = makeNavBtn(NAV_ICONS[i], NAV_LABELS[i], i == 0);
            navBtns[i].addActionListener(e -> navigateTo(idx));
            nav.add(navBtns[i]);
        }

        // Bottom: user info + logout
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1,0,0,0,Theme.BORDER),
            BorderFactory.createEmptyBorder(10,12,12,12)));

        String role = currentUser.isAdmin() ? "Administrator" : "Staff";
        JLabel uname = Theme.label(currentUser.getUsername(), Theme.FONT_BOLD, Theme.TEXT_PRIMARY);
        JLabel urole = Theme.label(role, Theme.FONT_SMALL, Theme.TEXT_MUTED);
        JPanel uinfo = new JPanel(new GridLayout(2,1,0,2));
        uinfo.setOpaque(false); uinfo.add(uname); uinfo.add(urole);

        JButton logout = Theme.button("Logout", new Color(140,30,30));
        logout.setPreferredSize(new Dimension(160,32));
        logout.addActionListener(e -> confirmLogout());

        bottom.add(uinfo,  BorderLayout.CENTER);
        bottom.add(logout, BorderLayout.SOUTH);

        sb.add(logo,        BorderLayout.NORTH);
        sb.add(new JScrollPane(nav){{setOpaque(false);getViewport().setOpaque(false);setBorder(null);}}, BorderLayout.CENTER);
        sb.add(bottom,      BorderLayout.SOUTH);
        return sb;
    }

    private JButton makeNavBtn(String icon, String label, boolean active) {
        JButton b = new JButton(icon + "  " + label) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                boolean sel = Boolean.TRUE.equals(getClientProperty("selected"));
                if (sel) {
                    g2.setColor(new Color(52,199,89,30));
                    g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
                    g2.setColor(Theme.ACCENT); g2.setStroke(new BasicStroke(2f));
                    g2.drawLine(0,8,0,getHeight()-8);
                } else if (getModel().isRollover()) {
                    g2.setColor(Theme.BORDER); g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
                }
                g2.setFont(Theme.FONT_BODY);
                boolean sel2 = Boolean.TRUE.equals(getClientProperty("selected"));
                g2.setColor(sel2 ? Theme.ACCENT : (getModel().isRollover() ? Theme.TEXT_PRIMARY : Theme.TEXT_SECONDARY));
                g2.drawString(getText(), 14, getHeight()/2 + g2.getFontMetrics().getAscent()/2 - 2);
                g2.dispose();
            }
        };
        b.putClientProperty("selected", active);
        b.setFont(Theme.FONT_BODY);
        b.setPreferredSize(new Dimension(184,40));
        b.setContentAreaFilled(false); b.setBorderPainted(false); b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    // ── Main area ──────────────────────────────────────────────
    private JPanel buildMainArea() {
        JPanel main = new JPanel(new BorderLayout(0,0));
        main.setBackground(Theme.BG_DARK);

        main.add(buildTopBar(),  BorderLayout.NORTH);

        contentArea = new JPanel(new CardLayout());
        contentArea.setBackground(Theme.BG_DARK);

        // Add all panels
        deptPanel    = new DepartmentPanel(currentUser);
        devicePanel  = new DeviceEntryPanel(currentUser);
        solarPanel   = new SolarSimulationPanel(currentUser);
        reportsPanel = new ReportsPanel(currentUser);
        chartsPanel  = new ChartsPanel(currentUser);
        usersPanel   = new UsersPanel(currentUser);

        contentArea.add(buildHomePanel(), "Home");
        contentArea.add(deptPanel,        "Departments");
        contentArea.add(devicePanel,      "Devices");
        contentArea.add(solarPanel,       "Solar");
        contentArea.add(reportsPanel,     "Reports");
        contentArea.add(chartsPanel,      "Charts");
        contentArea.add(usersPanel,       "Users");

        main.add(contentArea,    BorderLayout.CENTER);
        main.add(buildStatusBar(),BorderLayout.SOUTH);
        return main;
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Theme.BG_SIDEBAR);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0,0,1,0,Theme.BORDER),
            BorderFactory.createEmptyBorder(10,20,10,20)));

        JLabel page = Theme.label("Dashboard", Theme.FONT_HEADING, Theme.TEXT_PRIMARY);
        page.setName("pageTitle");

        JLabel time = Theme.label(java.time.LocalDateTime.now()
            .format(java.time.format.DateTimeFormatter.ofPattern("EEE, dd MMM yyyy  HH:mm")),
            Theme.FONT_SMALL, Theme.TEXT_MUTED);

        // Clock tick
        new Timer(30_000, e -> time.setText(java.time.LocalDateTime.now()
            .format(java.time.format.DateTimeFormatter.ofPattern("EEE, dd MMM yyyy  HH:mm")))).start();

        bar.add(page, BorderLayout.WEST);
        bar.add(time, BorderLayout.EAST);
        return bar;
    }

    // ── Home panel ─────────────────────────────────────────────
    private JPanel buildHomePanel() {
        JPanel p = Theme.darkPanel(new BorderLayout(0,16));
        p.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));

        // KPI row
        JPanel kpiRow = new JPanel(new GridLayout(1,4,14,0));
        kpiRow.setOpaque(false);
        kpiValues = new JLabel[4];

        String[][] kpis = {
            {"Total Monthly kWh",   "—",    "kilowatt-hours",   String.valueOf(Theme.ACCENT.getRGB())},
            {"Monthly Cost",        "—",    "Pakistani Rupees", String.valueOf(Theme.AMBER.getRGB())},
            {"CO₂ Footprint",       "—",    "kg CO₂ emitted",   String.valueOf(Theme.RED.getRGB())},
            {"Registered Devices",  "—",    "across all depts", String.valueOf(Theme.BLUE.getRGB())}
        };
        for (int i=0; i<4; i++) {
            Color ac = new Color(Integer.parseInt(kpis[i][3]));
            JPanel card = Theme.kpiCard(kpis[i][0], kpis[i][1], kpis[i][2], ac);
            // grab value label (2nd label in card)
            kpiValues[i] = (JLabel)((JPanel)card.getComponent(0)).getComponent(1);
            // Actually kpiCard returns a JPanel with GridBagLayout — get labels differently
            kpiValues[i] = findValueLabel(card);
            kpiRow.add(card);
        }

        // Bottom 2 columns
        JPanel bottom = new JPanel(new GridLayout(1,2,14,0));
        bottom.setOpaque(false);
        bottom.add(buildTipsCard());
        bottom.add(buildQuickActionsCard());

        p.add(kpiRow,  BorderLayout.NORTH);
        p.add(bottom,  BorderLayout.CENTER);
        return p;
    }

    /** Walk card to find the big-number JLabel */
    private JLabel findValueLabel(JPanel card) {
        for (Component c : card.getComponents()) {
            if (c instanceof JLabel l && l.getFont().getSize() >= 20) return l;
        }
        return new JLabel("—");
    }

    private void refreshKPIs() {
        new SwingWorker<double[],Void>() {
            @Override protected double[] doInBackground() {
                List<Department> depts = Queries.getAllDepartments();
                double kwh=0;
                for (Department d : depts) kwh += Queries.getTotalMonthlyKWhByDepartment(d.getDeptId());
                double cost = EnergyCalculator.calculateMonthlyCost(kwh);
                double co2  = CarbonCalculator.calculateCarbonFootprint(kwh);
                int    devs = 0;
                for (Department d : depts) devs += Queries.getDevicesByDepartment(d.getDeptId()).size();
                return new double[]{kwh, cost, co2, devs};
            }
            @Override protected void done() {
                try {
                    double[] v = get();
                    if (kpiValues[0] != null) kpiValues[0].setText(String.format("%.0f", v[0]));
                    if (kpiValues[1] != null) kpiValues[1].setText(String.format("Rs %.0f", v[1]));
                    if (kpiValues[2] != null) kpiValues[2].setText(String.format("%.0f", v[2]));
                    if (kpiValues[3] != null) kpiValues[3].setText(String.valueOf((int)v[3]));
                } catch(Exception ex){ ex.printStackTrace(); }
            }
        }.execute();
    }

    private JPanel buildTipsCard() {
        JPanel card = Theme.card("💡 Energy Saving Tips");
        String[] tips = {
            "• Set AC to 24°C — saves up to 18% electricity",
            "• Turn off lights when leaving classrooms",
            "• Use LED bulbs — 75% less energy than incandescent",
            "• Unplug chargers and standby devices overnight",
            "• Use natural light — open blinds during daytime",
            "• Regular HVAC maintenance improves efficiency by 15%"
        };
        JPanel list = new JPanel(new GridLayout(tips.length,1,0,4));
        list.setOpaque(false);
        for (String t : tips) {
            JLabel l = Theme.label(t, Theme.FONT_SMALL, Theme.TEXT_SECONDARY);
            list.add(l);
        }
        card.add(list, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildQuickActionsCard() {
        JPanel card = Theme.card("⚡ Quick Actions");
        JPanel btns = new JPanel(new GridLayout(4,1,0,10));
        btns.setOpaque(false);
        String[][] actions = {
            {"＋  Add New Device",    "Devices"},
            {"🏢  Manage Departments","Departments"},
            {"☀  Solar Calculator",  "Solar"},
            {"📊  View Reports",      "Reports"}
        };
        for (String[] a : actions) {
            JButton b = Theme.button(a[0], Theme.ACCENT_DARK);
            b.setPreferredSize(new Dimension(200,38));
            b.addActionListener(e -> {
                for(int i=0;i<NAV_LABELS.length;i++)
                    if(NAV_LABELS[i].equals(a[1])) { navigateTo(i); break; }
            });
            btns.add(b);
        }
        card.add(btns, BorderLayout.CENTER);
        return card;
    }

    // ── Status bar ─────────────────────────────────────────────
    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT,12,5));
        bar.setBackground(Theme.BG_SIDEBAR);
        bar.setBorder(BorderFactory.createMatteBorder(1,0,0,0,Theme.BORDER));

        statusDot = new JLabel("●");
        statusDot.setFont(Theme.FONT_SMALL);
        statusTxt = Theme.label("Checking database…", Theme.FONT_SMALL, Theme.TEXT_MUTED);

        bar.add(statusDot); bar.add(statusTxt);

        JLabel user = Theme.label("User: " + currentUser.getUsername() + "  |  Role: " +
            (currentUser.isAdmin() ? "Admin" : "Staff"), Theme.FONT_SMALL, Theme.TEXT_MUTED);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        right.setOpaque(false); right.add(user);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false); wrap.add(bar, BorderLayout.WEST); wrap.add(right, BorderLayout.EAST);
        wrap.setBackground(Theme.BG_SIDEBAR);

        // Check DB
        new SwingWorker<Boolean,Void>(){
            @Override protected Boolean doInBackground(){
                try { var c=database.DBConnection.getConnection(); return c!=null&&!c.isClosed(); }
                catch(Exception e){ return false; }
            }
            @Override protected void done(){
                try {
                    boolean ok=get();
                    statusDot.setForeground(ok?Theme.ACCENT:Theme.RED);
                    statusTxt.setText(ok?"Database connected":"Database disconnected");
                } catch(Exception e){ statusDot.setForeground(Theme.RED); }
            }
        }.execute();

        return wrap;
    }

    // ── Navigation ─────────────────────────────────────────────
    private void navigateTo(int idx) {
        activeNav = idx;
        for (int i=0; i<navBtns.length; i++) {
            if (navBtns[i] != null) navBtns[i].putClientProperty("selected", i == idx);
            if (navBtns[i] != null) navBtns[i].repaint();
        }
        String name = NAV_LABELS[idx];
        ((CardLayout)contentArea.getLayout()).show(contentArea, name);
    }

    // ── Logout ─────────────────────────────────────────────────
    private void confirmLogout() {
        int r = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to logout?", "Logout", JOptionPane.YES_NO_OPTION);
        if (r == JOptionPane.YES_OPTION) { dispose(); new LoginScreen().setVisible(true); }
    }
}