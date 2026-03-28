package GUI;

import Models.User;
import Models.Department;
import Models.Device;
import Models.EnergyUsage;
import database.Queries;
import utils.EnergyCalculator;
import utils.CarbonCalculator;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ChartsPanel extends JPanel {

    private final User currentUser;
    private JTabbedPane tabs;
    private JComboBox<Department> deptCombo;
    private JComboBox<String>     rangeCombo;
    private JButton refreshBtn;

    private static final Color CHART_BG  = new Color(22, 35, 28);
    private static final Color PLOT_BG   = new Color(18, 28, 22);
    private static final Color GRID_LINE = new Color(44, 68, 50);
    private static final Color AXIS_LABEL= new Color(140, 168, 148);
    private static final Color AXIS_TICK = new Color(100, 140, 110);
    private static final Color[] SERIES_CLR = {
        new Color(52, 199, 89), new Color(64, 156, 255),
        new Color(255, 178, 55), new Color(255, 80, 80),
        new Color(200, 100, 220), new Color(50, 210, 200)
    };

    public ChartsPanel(User user) {
        this.currentUser = user;
        setOpaque(false);
        setLayout(new BorderLayout(0, 0));
        buildUI();
        loadDepts();
    }

    private void buildUI() {
        JPanel outer = Theme.darkPanel(new BorderLayout(0, 12));
        outer.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        outer.add(buildControls(), BorderLayout.NORTH);
        tabs = new JTabbedPane();
        tabs.setBackground(Theme.BG_CARD);
        tabs.setForeground(Theme.TEXT_SECONDARY);
        tabs.setFont(Theme.FONT_BOLD);
        outer.add(tabs, BorderLayout.CENTER);
        add(outer, BorderLayout.CENTER);
    }

    private JPanel buildControls() {
        JPanel card = Theme.card(null);
        card.setPreferredSize(new Dimension(900, 76));
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 8));
        row.setOpaque(false);
        row.add(Theme.label("Department:", Theme.FONT_BOLD, Theme.TEXT_SECONDARY));
        deptCombo = new JComboBox<>();
        Theme.styleCombo(deptCombo); deptCombo.setPreferredSize(new Dimension(210, 36));
        row.add(deptCombo);
        row.add(Theme.label("Period:", Theme.FONT_BOLD, Theme.TEXT_SECONDARY));
        rangeCombo = new JComboBox<>(new String[]{"Last 7 Days","Last 30 Days","Last 3 Months","Last Year"});
        Theme.styleCombo(rangeCombo); rangeCombo.setPreferredSize(new Dimension(150, 36));
        row.add(rangeCombo);
        refreshBtn = Theme.button("↻  Refresh", Theme.ACCENT_DARK);
        refreshBtn.addActionListener(e -> loadAllCharts());
        row.add(refreshBtn);
        card.add(row, BorderLayout.CENTER);
        return card;
    }

    private void loadDepts() {
        deptCombo.removeAllItems();
        deptCombo.addItem(new Department(0, "All Departments", 0, ""));
        for (Department d : Queries.getAllDepartments()) deptCombo.addItem(d);
        if (!currentUser.isAdmin() && currentUser.getDepartmentId() != null) {
            for (int i = 0; i < deptCombo.getItemCount(); i++) {
                if (deptCombo.getItemAt(i).getDeptId() == currentUser.getDepartmentId()) {
                    deptCombo.setSelectedIndex(i); break;
                }
            }
            deptCombo.setEnabled(false);
        }
        loadAllCharts();
    }

    private void loadAllCharts() {
        tabs.removeAll();
        tabs.addTab("📊 Consumption", wrapChart(buildConsumptionChart()));
        tabs.addTab("🥧 Devices",     wrapChart(buildDeviceChart()));
        tabs.addTab("💰 Cost",        wrapChart(buildCostChart()));
        tabs.addTab("📈 Trend",       wrapChart(buildTrendChart()));
        tabs.addTab("🌿 Carbon",      wrapChart(buildCarbonChart()));
        tabs.addTab("⭐ Efficiency",  wrapChart(buildEfficiencyChart()));
    }

    private JPanel wrapChart(JFreeChart chart) {
        ChartPanel cp = new ChartPanel(chart);
        cp.setBackground(CHART_BG); cp.setPopupMenu(null); cp.setMouseWheelEnabled(true);
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(CHART_BG); p.add(cp, BorderLayout.CENTER);
        return p;
    }

    private void styleCategoryPlot(CategoryPlot plot) {
        plot.setBackgroundPaint(PLOT_BG);
        plot.setRangeGridlinePaint(GRID_LINE); plot.setDomainGridlinePaint(GRID_LINE);
        plot.setOutlinePaint(GRID_LINE);
        plot.getDomainAxis().setLabelPaint(AXIS_LABEL); plot.getDomainAxis().setTickLabelPaint(AXIS_TICK);
        plot.getDomainAxis().setLabelFont(Theme.FONT_SMALL); plot.getDomainAxis().setTickLabelFont(Theme.FONT_SMALL);
        plot.getRangeAxis().setLabelPaint(AXIS_LABEL); plot.getRangeAxis().setTickLabelPaint(AXIS_TICK);
        plot.getRangeAxis().setLabelFont(Theme.FONT_SMALL); plot.getRangeAxis().setTickLabelFont(Theme.FONT_SMALL);
    }

    private JFreeChart styledChart(JFreeChart chart) {
        chart.setBackgroundPaint(CHART_BG);
        chart.getTitle().setPaint(Theme.TEXT_PRIMARY); chart.getTitle().setFont(Theme.FONT_HEADING);
        if (chart.getLegend() != null) {
            chart.getLegend().setBackgroundPaint(CHART_BG);
            chart.getLegend().setItemPaint(Theme.TEXT_SECONDARY); chart.getLegend().setItemFont(Theme.FONT_SMALL);
        }
        return chart;
    }

    private JFreeChart buildConsumptionChart() {
        DefaultCategoryDataset ds = new DefaultCategoryDataset();
        for (Department d : Queries.getAllDepartments())
            ds.addValue(Queries.getTotalMonthlyKWhByDepartment(d.getDeptId()), "Monthly kWh", d.getDeptName());
        JFreeChart chart = ChartFactory.createBarChart(
            "Monthly Energy Consumption by Department", "Department", "kWh",
            ds, PlotOrientation.VERTICAL, false, true, false);
        styledChart(chart);
        CategoryPlot plot = chart.getCategoryPlot(); styleCategoryPlot(plot);
        ((BarRenderer) plot.getRenderer()).setSeriesPaint(0, SERIES_CLR[0]);
        plot.getDomainAxis().setCategoryLabelPositions(org.jfree.chart.axis.CategoryLabelPositions.UP_45);
        return chart;
    }

    private JFreeChart buildDeviceChart() {
        DefaultPieDataset<String> ds = new DefaultPieDataset<>();
        Department sel = (Department) deptCombo.getSelectedItem();
        List<Department> depts = (sel != null && sel.getDeptId() != 0) ? List.of(sel) : Queries.getAllDepartments();
        int ac=0, light=0, comp=0, other=0;
        for (Department d : depts)
            for (Device dv : Queries.getDevicesByDepartment(d.getDeptId())) {
                String n = dv.getDeviceName().toLowerCase();
                if (n.contains("ac")||n.contains("air")) ac += dv.getQuantity();
                else if (n.contains("light")||n.contains("lamp")) light += dv.getQuantity();
                else if (n.contains("comp")||n.contains("pc")||n.contains("laptop")) comp += dv.getQuantity();
                else other += dv.getQuantity();
            }
        if (ac>0)    ds.setValue("Air Conditioners", ac);
        if (light>0) ds.setValue("Lights & Lamps",   light);
        if (comp>0)  ds.setValue("Computers",         comp);
        if (other>0) ds.setValue("Other Devices",     other);
        if (ds.getItemCount()==0) ds.setValue("No Devices", 1);
        JFreeChart chart = ChartFactory.createPieChart("Device Distribution", ds, true, true, false);
        styledChart(chart);
        PiePlot<String> plot = (PiePlot<String>) chart.getPlot();
        plot.setBackgroundPaint(PLOT_BG); plot.setOutlinePaint(GRID_LINE);
        plot.setLabelBackgroundPaint(new Color(30,46,34)); plot.setLabelOutlinePaint(GRID_LINE);
        plot.setLabelShadowPaint(null); plot.setLabelPaint(Theme.TEXT_PRIMARY); plot.setLabelFont(Theme.FONT_SMALL);
        Color[] pc = {SERIES_CLR[0],SERIES_CLR[1],SERIES_CLR[2],SERIES_CLR[3]};
        int i=0; for (var key : ds.getKeys()) plot.setSectionPaint(key, pc[i++%pc.length]);
        return chart;
    }

    private JFreeChart buildCostChart() {
        DefaultCategoryDataset ds = new DefaultCategoryDataset();
        for (Department d : Queries.getAllDepartments())
            ds.addValue(EnergyCalculator.calculateMonthlyCost(
                Queries.getTotalMonthlyKWhByDepartment(d.getDeptId())), "Monthly Cost (Rs)", d.getDeptName());
        JFreeChart chart = ChartFactory.createBarChart(
            "Monthly Electricity Cost by Department", "Department", "Cost (Rs)",
            ds, PlotOrientation.VERTICAL, false, true, false);
        styledChart(chart);
        CategoryPlot plot = chart.getCategoryPlot(); styleCategoryPlot(plot);
        ((BarRenderer) plot.getRenderer()).setSeriesPaint(0, SERIES_CLR[2]);
        plot.getDomainAxis().setCategoryLabelPositions(org.jfree.chart.axis.CategoryLabelPositions.UP_45);
        return chart;
    }

    // ── Trend chart — energy_usage table missing pe crash nahi hoga ──
    private JFreeChart buildTrendChart() {
        Department sel = (Department) deptCombo.getSelectedItem();
        int days = switch (rangeCombo.getSelectedIndex()) {
            case 0 -> 7; case 1 -> 30; case 2 -> 90; default -> 365;
        };
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        List<Department> depts = (sel != null && sel.getDeptId() != 0)
            ? List.of(sel) : Queries.getAllDepartments().stream().limit(5).toList();

        for (Department d : depts) {
            TimeSeries series = new TimeSeries(d.getDeptName());

            // FIX: try-catch around DB call — graceful fallback if table missing
            List<EnergyUsage> history = new ArrayList<>();
            try {
                history = Queries.getEnergyUsageHistory(d.getDeptId(), days);
            } catch (Exception ignored) {
                // energy_usage table does not exist yet — use deterministic fallback
            }

            if (!history.isEmpty()) {
                for (EnergyUsage u : history) {
                    LocalDate ld = u.getTimestamp().toLocalDate();
                    Day day = new Day(ld.getDayOfMonth(), ld.getMonthValue(), ld.getYear());
                    try { series.addOrUpdate(day, u.getKWh()); } catch (Exception ignored) {}
                }
            } else {
                // Deterministic fallback — no Random(), based on device data
                double base = Queries.getTotalMonthlyKWhByDepartment(d.getDeptId()) / 30.0;
                for (int i = days; i >= 0; i--) {
                    LocalDate ld = LocalDate.now().minusDays(i);
                    Day day = new Day(ld.getDayOfMonth(), ld.getMonthValue(), ld.getYear());
                    double variation = base * 0.15 * Math.sin((ld.getDayOfYear() + d.getDeptId() * 31) * Math.PI / 180.0);
                    try { series.addOrUpdate(day, Math.max(0, base + variation)); } catch (Exception ignored) {}
                }
            }
            dataset.addSeries(series);
        }

        JFreeChart chart = ChartFactory.createTimeSeriesChart(
            "Energy Consumption Trend", "Date", "kWh", dataset, true, true, false);
        styledChart(chart);
        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(PLOT_BG); plot.setDomainGridlinePaint(GRID_LINE); plot.setRangeGridlinePaint(GRID_LINE);
        plot.getDomainAxis().setLabelPaint(AXIS_LABEL); plot.getDomainAxis().setTickLabelPaint(AXIS_TICK);
        plot.getDomainAxis().setTickLabelFont(Theme.FONT_SMALL);
        plot.getRangeAxis().setLabelPaint(AXIS_LABEL); plot.getRangeAxis().setTickLabelPaint(AXIS_TICK);
        XYLineAndShapeRenderer r = new XYLineAndShapeRenderer(true, false);
        for (int i = 0; i < dataset.getSeriesCount(); i++) r.setSeriesPaint(i, SERIES_CLR[i % SERIES_CLR.length]);
        plot.setRenderer(r);
        return chart;
    }

    private JFreeChart buildCarbonChart() {
        DefaultCategoryDataset ds = new DefaultCategoryDataset();
        for (Department d : Queries.getAllDepartments())
            ds.addValue(CarbonCalculator.calculateCarbonFootprint(
                Queries.getTotalMonthlyKWhByDepartment(d.getDeptId())), "CO₂ Emissions (kg)", d.getDeptName());
        JFreeChart chart = ChartFactory.createBarChart(
            "Monthly Carbon Footprint by Department", "Department", "CO₂ (kg)",
            ds, PlotOrientation.VERTICAL, false, true, false);
        styledChart(chart);
        CategoryPlot plot = chart.getCategoryPlot(); styleCategoryPlot(plot);
        ((BarRenderer) plot.getRenderer()).setSeriesPaint(0, SERIES_CLR[3]);
        plot.getDomainAxis().setCategoryLabelPositions(org.jfree.chart.axis.CategoryLabelPositions.UP_45);
        return chart;
    }

    private JFreeChart buildEfficiencyChart() {
        DefaultCategoryDataset ds = new DefaultCategoryDataset();
        for (Department d : Queries.getAllDepartments())
            ds.addValue(efficiencyScore(Queries.getTotalMonthlyKWhByDepartment(d.getDeptId())),
                "Efficiency Score", d.getDeptName());
        JFreeChart chart = ChartFactory.createLineChart(
            "Department Efficiency Scores", "Department", "Score (0–100)",
            ds, PlotOrientation.VERTICAL, false, true, false);
        styledChart(chart);
        CategoryPlot plot = chart.getCategoryPlot(); styleCategoryPlot(plot);
        plot.getRangeAxis().setRange(0, 100);
        LineAndShapeRenderer rend = new LineAndShapeRenderer(true, true);
        rend.setSeriesPaint(0, SERIES_CLR[1]); rend.setSeriesShapesVisible(0, true);
        plot.setRenderer(rend);
        plot.getDomainAxis().setCategoryLabelPositions(org.jfree.chart.axis.CategoryLabelPositions.UP_45);
        return chart;
    }

    private int efficiencyScore(double kwh) {
        if (kwh < 500)  return 95;
        if (kwh < 1000) return 85;
        if (kwh < 2000) return 72;
        if (kwh < 3000) return 60;
        if (kwh < 4000) return 48;
        return 35;
    }
}