package GUI;

import Models.Department;
import Models.User;
import database.Queries;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import utils.CarbonCalculator;
import utils.EnergyCalculator;

public class Dashboard extends JFrame {

    private final User currentUser;
    private JPanel     contentArea;
    private JLabel     statusDot, statusTxt;

    // KPI labels stored directly — no casting, no findValueLabel() bug
    private JLabel kpiKwh, kpiCost, kpiCo2, kpiDevices;

    private DepartmentPanel      deptPanel;
    private DeviceEntryPanel     devicePanel;
    private SolarSimulationPanel solarPanel;
    private ReportsPanel         reportsPanel;
    private ChartsPanel          chartsPanel;
    private UsersPanel           usersPanel;

    private JButton[] navBtns;
    private static final String[] NAV_ICONS  = {"⊞","🏢","🔌","☀","📊","📈","👥"};
    private static final String[] NAV_LABELS = {"Home","Departments","Devices","Solar","Reports","Charts","Users"};

    public Dashboard(User user) {
        this.currentUser = user;
        setupWindow();
        buildUI();
        refreshKPIs();
        new Timer(60_000, e -> refreshKPIs()).start();
    }

    private void setupWindow() {
        setTitle("SEUS — Smart Energy Optimization System");
        setSize(1280, 760);
        setMinimumSize(new Dimension(960, 600));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(Theme.BG_DARK);
        root.add(buildSidebar(),  BorderLayout.WEST);
        root.add(buildMainArea(), BorderLayout.CENTER);
        setContentPane(root);
    }

    private JPanel buildSidebar() {
        JPanel sb = new JPanel(new BorderLayout(0, 0));
        sb.setBackground(Theme.BG_SIDEBAR);
        sb.setPreferredSize(new Dimension(200, 760));
        sb.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Theme.BORDER));

        JPanel logo = new JPanel(new GridLayout(2, 1, 0, 2));
        logo.setOpaque(false);
        logo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER),
            BorderFactory.createEmptyBorder(16, 18, 16, 18)));
        logo.add(Theme.label("⚡ SEUS", new Font("Segoe UI", Font.BOLD, 20), Theme.ACCENT));
        logo.add(Theme.label("Energy System", Theme.FONT_SMALL, Theme.TEXT_MUTED));

        int count = currentUser.isAdmin() ? NAV_LABELS.length : NAV_LABELS.length - 1;
        JPanel nav = new JPanel(new GridLayout(count, 1, 0, 2));
        nav.setOpaque(false);
        nav.setBorder(BorderFactory.createEmptyBorder(12, 8, 12, 8));
        navBtns = new JButton[NAV_LABELS.length];
        for (int i = 0; i < count; i++) {
            final int idx = i;
            navBtns[i] = makeNavBtn(NAV_ICONS[i], NAV_LABELS[i], i == 0);
            navBtns[i].addActionListener(e -> navigateTo(idx));
            nav.add(navBtns[i]);
        }

        JPanel bottom = new JPanel(new BorderLayout(0, 8));
        bottom.setOpaque(false);
        bottom.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER),
            BorderFactory.createEmptyBorder(10, 12, 12, 12)));
        JPanel uinfo = new JPanel(new GridLayout(2, 1, 0, 2));
        uinfo.setOpaque(false);
        uinfo.add(Theme.label(currentUser.getUsername(), Theme.FONT_BOLD, Theme.TEXT_PRIMARY));
        uinfo.add(Theme.label(currentUser.isAdmin() ? "Administrator" : "Staff", Theme.FONT_SMALL, Theme.TEXT_MUTED));
        JButton logout = Theme.button("Logout", new Color(140, 30, 30));
        logout.setPreferredSize(new Dimension(160, 32));
        logout.addActionListener(e -> confirmLogout());
        bottom.add(uinfo, BorderLayout.CENTER);
        bottom.add(logout, BorderLayout.SOUTH);

        JScrollPane navScroll = new JScrollPane(nav);
        navScroll.setOpaque(false); navScroll.getViewport().setOpaque(false); navScroll.setBorder(null);

        sb.add(logo, BorderLayout.NORTH);
        sb.add(navScroll, BorderLayout.CENTER);
        sb.add(bottom, BorderLayout.SOUTH);
        return sb;
    }

    private JButton makeNavBtn(String icon, String label, boolean selected) {
        JButton b = new JButton(icon + "  " + label) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean sel = Boolean.TRUE.equals(getClientProperty("selected"));
                if (sel) {
                    g2.setColor(new Color(52, 199, 89, 30));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    g2.setColor(Theme.ACCENT); g2.setStroke(new BasicStroke(2f));
                    g2.drawLine(0, 8, 0, getHeight() - 8);
                } else if (getModel().isRollover()) {
                    g2.setColor(Theme.BORDER);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                }
                g2.setFont(Theme.FONT_BODY);
                g2.setColor(sel ? Theme.ACCENT : (getModel().isRollover() ? Theme.TEXT_PRIMARY : Theme.TEXT_SECONDARY));
                g2.drawString(getText(), 14, getHeight() / 2 + g2.getFontMetrics().getAscent() / 2 - 2);
                g2.dispose();
            }
        };
        b.putClientProperty("selected", selected);
        b.setFont(Theme.FONT_BODY);
        b.setPreferredSize(new Dimension(184, 40));
        b.setContentAreaFilled(false); b.setBorderPainted(false); b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JPanel buildMainArea() {
        JPanel main = new JPanel(new BorderLayout(0, 0));
        main.setBackground(Theme.BG_DARK);
        main.add(buildTopBar(), BorderLayout.NORTH);

        contentArea = new JPanel(new CardLayout());
        contentArea.setBackground(Theme.BG_DARK);

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

        main.add(contentArea,      BorderLayout.CENTER);
        main.add(buildStatusBar(), BorderLayout.SOUTH);
        return main;
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Theme.BG_SIDEBAR);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)));
        JLabel page = Theme.label("Dashboard", Theme.FONT_HEADING, Theme.TEXT_PRIMARY);
        JLabel time = Theme.label(
            java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("EEE, dd MMM yyyy  HH:mm")),
            Theme.FONT_SMALL, Theme.TEXT_MUTED);
        new Timer(30_000, e -> time.setText(
            java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("EEE, dd MMM yyyy  HH:mm")))).start();
        bar.add(page, BorderLayout.WEST);
        bar.add(time, BorderLayout.EAST);
        return bar;
    }

    // ── Home panel — KPI bug FIXED ─────────────────────────────
    private JPanel buildHomePanel() {
        JPanel p = Theme.darkPanel(new BorderLayout(0, 14));
        p.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JPanel kpiRow = new JPanel(new GridLayout(1, 4, 12, 0));
        kpiRow.setOpaque(false);
        kpiRow.setPreferredSize(new Dimension(900, 100));

        // addKpiCard returns the value JLabel directly — no guessing, no casting
        kpiKwh     = addKpiCard(kpiRow, "Total Monthly kWh",  "—", "kilowatt-hours",   Theme.ACCENT);
        kpiCost    = addKpiCard(kpiRow, "Monthly Cost",        "—", "Pakistani Rupees", Theme.AMBER);
        kpiCo2     = addKpiCard(kpiRow, "CO₂ Footprint",       "—", "kg CO₂ emitted",   Theme.RED);
        kpiDevices = addKpiCard(kpiRow, "Registered Devices",  "—", "across all depts", Theme.BLUE);

        JPanel bottom = new JPanel(new GridLayout(1, 2, 14, 0));
        bottom.setOpaque(false);
        bottom.add(buildTipsCard());
        bottom.add(buildQuickActionsCard());

        p.add(kpiRow,  BorderLayout.NORTH);
        p.add(bottom,  BorderLayout.CENTER);
        return p;
    }

    private JLabel addKpiCard(JPanel parent, String title, String value, String unit, Color accent) {
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(Theme.FONT_SMALL);
        titleLbl.setForeground(Theme.TEXT_SECONDARY);

        JLabel valLbl = new JLabel(value);
        valLbl.setFont(new Font("Segoe UI", Font.BOLD, 26));
        valLbl.setForeground(accent);

        JLabel unitLbl = new JLabel(unit);
        unitLbl.setFont(Theme.FONT_SMALL);
        unitLbl.setForeground(Theme.TEXT_MUTED);

        JPanel card = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(accent); g2.setStroke(new BasicStroke(2f));
                g2.drawLine(16, getHeight()-3, getWidth()-16, getHeight()-3);
                g2.setColor(Theme.BORDER); g2.setStroke(new BasicStroke(0.8f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 14, 14);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1; c.gridx = 0;
        c.gridy = 0; c.insets = new Insets(10, 14, 2, 14); card.add(titleLbl, c);
        c.gridy = 1; c.insets = new Insets(0,  14, 2, 14); card.add(valLbl,   c);
        c.gridy = 2; c.insets = new Insets(0,  14, 10,14); card.add(unitLbl,  c);

        parent.add(card);
        return valLbl;
    }

    private void refreshKPIs() {
        new SwingWorker<double[], Void>() {
            @Override protected double[] doInBackground() {
                try {
                    List<Department> depts = Queries.getAllDepartments();
                    double kwh = 0; int devs = 0;
                    for (Department d : depts) {
                        kwh  += Queries.getTotalMonthlyKWhByDepartment(d.getDeptId());
                        devs += Queries.getDevicesByDepartment(d.getDeptId()).size();
                    }
                    return new double[]{kwh, EnergyCalculator.calculateMonthlyCost(kwh),
                        CarbonCalculator.calculateCarbonFootprint(kwh), devs};
                } catch (Exception e) { return new double[]{0, 0, 0, 0}; }
            }
            @Override protected void done() {
                try {
                    double[] v = get();
                    kpiKwh.setText(String.format("%.0f",     v[0]));
                    kpiCost.setText(String.format("Rs %.0f", v[1]));
                    kpiCo2.setText(String.format("%.0f",     v[2]));
                    kpiDevices.setText(String.valueOf((int)  v[3]));
                } catch (Exception ex) { ex.printStackTrace(); }
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
        JPanel list = new JPanel(new GridLayout(tips.length, 1, 0, 4));
        list.setOpaque(false);
        for (String t : tips) list.add(Theme.label(t, Theme.FONT_SMALL, Theme.TEXT_SECONDARY));
        card.add(list, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildQuickActionsCard() {
        JPanel card = Theme.card("⚡ Quick Actions");
        JPanel btns = new JPanel(new GridLayout(4, 1, 0, 10));
        btns.setOpaque(false);
        String[][] actions = {
            {"＋  Add New Device",     "Devices"},
            {"🏢  Manage Departments", "Departments"},
            {"☀  Solar Calculator",   "Solar"},
            {"📊  View Reports",       "Reports"}
        };
        for (String[] a : actions) {
            JButton b = Theme.button(a[0], Theme.ACCENT_DARK);
            b.setPreferredSize(new Dimension(200, 38));
            b.addActionListener(e -> {
                for (int i = 0; i < NAV_LABELS.length; i++)
                    if (NAV_LABELS[i].equals(a[1])) { navigateTo(i); break; }
            });
            btns.add(b);
        }
        card.add(btns, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Theme.BG_SIDEBAR);
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER));
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 5));
        left.setOpaque(false);
        statusDot = Theme.label("●", Theme.FONT_SMALL, Theme.TEXT_MUTED);
        statusTxt = Theme.label("Connecting…", Theme.FONT_SMALL, Theme.TEXT_MUTED);
        left.add(statusDot); left.add(statusTxt);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 5));
        right.setOpaque(false);
        right.add(Theme.label("User: " + currentUser.getUsername() + "  |  Role: " +
            (currentUser.isAdmin() ? "Admin" : "Staff"), Theme.FONT_SMALL, Theme.TEXT_MUTED));
        bar.add(left, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);

        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() {
                try { var c = database.DBConnection.getConnection(); return c != null && !c.isClosed(); }
                catch (Exception e) { return false; }
            }
            @Override protected void done() {
                try {
                    boolean ok = get();
                    statusDot.setForeground(ok ? Theme.ACCENT : Theme.RED);
                    statusTxt.setText(ok ? "Database connected" : "Database disconnected");
                } catch (Exception e) { statusDot.setForeground(Theme.RED); }
            }
        }.execute();
        return bar;
    }

    private void navigateTo(int idx) {
        for (int i = 0; i < navBtns.length; i++) {
            if (navBtns[i] != null) {
                navBtns[i].putClientProperty("selected", i == idx);
                navBtns[i].repaint();
            }
        }
        ((CardLayout) contentArea.getLayout()).show(contentArea, NAV_LABELS[idx]);
    }

    private void confirmLogout() {
        int r = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to logout?", "Logout", JOptionPane.YES_NO_OPTION);
        if (r == JOptionPane.YES_OPTION) { dispose(); new LoginScreen().setVisible(true); }
    }
}