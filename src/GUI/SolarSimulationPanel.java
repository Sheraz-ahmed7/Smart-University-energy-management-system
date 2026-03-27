package GUI;

import Models.User;
import Models.Department;
import database.Queries;
import utils.SolarSimulator;
import utils.EnergyCalculator;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class SolarSimulationPanel extends JPanel {

    private final User          currentUser;
    private JComboBox<Department> deptCombo;
    private JTextField          kwhField, panelWatt, peakHrs, panelCost;
    private JButton             calcBtn;
    private JLabel              panelsLbl, installLbl, genLbl, savingsLbl, paybackLbl, recLbl;

    public SolarSimulationPanel(User user) {
        this.currentUser = user;
        setOpaque(false); setLayout(new BorderLayout(0,0));
        buildUI(); loadDepartments();
    }

    private void buildUI() {
        JPanel outer = Theme.darkPanel(new BorderLayout(16,16));
        outer.setBorder(BorderFactory.createEmptyBorder(16,16,16,16));

        outer.add(buildInputCard(),   BorderLayout.WEST);
        outer.add(buildResultCard(),  BorderLayout.CENTER);
        outer.add(buildFactsPanel(),  BorderLayout.SOUTH);

        add(outer, BorderLayout.CENTER);
    }

    private JPanel buildInputCard() {
        JPanel card = Theme.card("☀  Solar ROI Calculator");
        card.setLayout(new GridBagLayout());
        card.setPreferredSize(new Dimension(320, 500));

        GridBagConstraints c = new GridBagConstraints();
        c.fill=GridBagConstraints.HORIZONTAL; c.weightx=1; c.gridx=0;

        // Dept
        c.gridy=0; c.insets=new Insets(12,0,5,0);
        card.add(Theme.label("Department",Theme.FONT_BOLD,Theme.TEXT_SECONDARY),c);
        c.gridy=1; c.insets=new Insets(0,0,14,0);
        deptCombo=new JComboBox<>(); Theme.styleCombo(deptCombo);
        deptCombo.setPreferredSize(new Dimension(270,38));
        deptCombo.addActionListener(e -> autoFillKwh());
        card.add(deptCombo,c);

        // kWh
        c.gridy=2; c.insets=new Insets(0,0,5,0);
        card.add(Theme.label("Monthly Consumption (kWh)",Theme.FONT_BOLD,Theme.TEXT_SECONDARY),c);
        c.gridy=3; c.insets=new Insets(0,0,14,0);
        kwhField=Theme.field("Auto-filled from department");
        kwhField.setPreferredSize(new Dimension(270,38)); card.add(kwhField,c);

        // Panel watt
        c.gridy=4; c.insets=new Insets(0,0,5,0);
        card.add(Theme.label("Panel Wattage (W)  [default 400]",Theme.FONT_BOLD,Theme.TEXT_SECONDARY),c);
        c.gridy=5; c.insets=new Insets(0,0,14,0);
        panelWatt=Theme.field("400"); panelWatt.setPreferredSize(new Dimension(270,38)); card.add(panelWatt,c);

        // Peak hours
        c.gridy=6; c.insets=new Insets(0,0,5,0);
        card.add(Theme.label("Peak Sun Hours/Day  [default 5]",Theme.FONT_BOLD,Theme.TEXT_SECONDARY),c);
        c.gridy=7; c.insets=new Insets(0,0,14,0);
        peakHrs=Theme.field("5"); peakHrs.setPreferredSize(new Dimension(270,38)); card.add(peakHrs,c);

        // Panel cost
        c.gridy=8; c.insets=new Insets(0,0,5,0);
        card.add(Theme.label("Cost per Panel (Rs)  [default 50000]",Theme.FONT_BOLD,Theme.TEXT_SECONDARY),c);
        c.gridy=9; c.insets=new Insets(0,0,20,0);
        panelCost=Theme.field("50000"); panelCost.setPreferredSize(new Dimension(270,38)); card.add(panelCost,c);

        calcBtn=Theme.button("Calculate Solar ROI", Theme.ACCENT_DARK);
        calcBtn.setPreferredSize(new Dimension(270,42));
        calcBtn.addActionListener(e->calculate());
        c.gridy=10; c.insets=new Insets(0,0,0,0); card.add(calcBtn,c);

        return card;
    }

    private JPanel buildResultCard() {
        JPanel card = Theme.card("📊  Simulation Results");
        card.setLayout(new GridBagLayout());

        // KPI grid
        JPanel grid = new JPanel(new GridLayout(2,3,12,12));
        grid.setOpaque(false);

        panelsLbl  = makeResultKpi(grid,"Panels Needed",  "—", "units",          Theme.ACCENT);
        installLbl = makeResultKpi(grid,"Installation Cost","—","PKR",            Theme.RED);
        genLbl     = makeResultKpi(grid,"Monthly Generation","—","kWh",           Theme.BLUE);
        savingsLbl = makeResultKpi(grid,"Annual Savings",  "—","PKR/year",       Theme.AMBER);
        paybackLbl = makeResultKpi(grid,"Payback Period",  "—","years",          new Color(200,100,220));
        makeResultKpi(grid,"Area Required",   "—","sq ft", Theme.TEXT_SECONDARY); // placeholder

        // Recommendation
        recLbl = Theme.label("Enter data and click Calculate to see solar recommendation.",
            Theme.FONT_BODY, Theme.TEXT_SECONDARY);
        recLbl.setBorder(BorderFactory.createEmptyBorder(14,0,0,0));

        GridBagConstraints c=new GridBagConstraints();
        c.fill=GridBagConstraints.BOTH; c.weightx=1; c.weighty=1; c.gridx=0; c.gridy=0;
        card.add(grid,c);
        c.gridy=1; c.weighty=0;
        card.add(recLbl,c);

        return card;
    }

    private JLabel makeResultKpi(JPanel parent, String title, String val, String unit, Color accent) {
        JPanel kpi = Theme.kpiCard(title, val, unit, accent);
        parent.add(kpi);
        return findLbl(kpi);
    }

    private JLabel findLbl(JPanel p){
        for(Component c:p.getComponents()) if(c instanceof JLabel l&&l.getFont().getSize()>=20) return l;
        return new JLabel("—");
    }

    private JPanel buildFactsPanel() {
        JPanel card = Theme.card("🌍  Solar Energy Facts — Pakistan");
        card.setPreferredSize(new Dimension(900,100));
        JPanel row = new JPanel(new GridLayout(1,3,14,0));
        row.setOpaque(false);
        String[] facts={
            "🌞  Pakistan receives 5–7 peak sun hours daily — ideal for solar",
            "💰  Solar panels typically pay for themselves in 3–7 years (ROI)",
            "♻  Modern panels last 25–30 years with minimal maintenance"
        };
        for(String f:facts){
            JLabel l=Theme.label(f,Theme.FONT_SMALL,Theme.TEXT_SECONDARY);
            l.setBorder(BorderFactory.createEmptyBorder(4,8,4,8));
            row.add(l);
        }
        card.add(row,BorderLayout.CENTER);
        return card;
    }

    private void loadDepartments(){
        deptCombo.removeAllItems();
        for(Department d:Queries.getAllDepartments()) deptCombo.addItem(d);
        if(!currentUser.isAdmin()&&currentUser.getDepartmentId()!=null)
            for(int i=0;i<deptCombo.getItemCount();i++)
                if(deptCombo.getItemAt(i).getDeptId()==currentUser.getDepartmentId())
                    { deptCombo.setSelectedIndex(i); break; }
    }

    private void autoFillKwh(){
        Department sel=(Department)deptCombo.getSelectedItem();
        if(sel!=null){
            double kwh=Queries.getTotalMonthlyKWhByDepartment(sel.getDeptId());
            kwhField.setText(String.format("%.2f",kwh));
            kwhField.setForeground(Theme.TEXT_PRIMARY);
        }
    }

    private void calculate(){
        try{
            double kwh   = Double.parseDouble(kwhField.getText().trim());
            double pw    = parseD(panelWatt,"400");
            double ph    = parseD(peakHrs,"5");
            double pc    = parseD(panelCost,"50000");
            if(kwh<=0){ JOptionPane.showMessageDialog(this,"Enter valid kWh","Error",JOptionPane.ERROR_MESSAGE); return; }

            SolarSimulator.SolarResult r = SolarSimulator.simulate(kwh, pw, ph, pc);

            panelsLbl.setText(String.valueOf(r.panelsNeeded));
            installLbl.setText(String.format("Rs %.0f",r.installationCost));
            genLbl.setText(String.format("%.0f",r.monthlyGeneration));
            savingsLbl.setText(String.format("Rs %.0f",r.annualSavings));
            paybackLbl.setText(String.format("%.1f",r.paybackYears));

            String rec = SolarSimulator.getRecommendation(r);
            recLbl.setText(rec);
            recLbl.setForeground(r.paybackYears<5 ? Theme.ACCENT : r.paybackYears<7 ? Theme.AMBER : Theme.RED);

        }catch(NumberFormatException e){
            JOptionPane.showMessageDialog(this,"Please enter valid numbers","Error",JOptionPane.ERROR_MESSAGE);
        }
    }

    private double parseD(JTextField f,String def){
        try{ return Double.parseDouble(f.getText().trim()); }
        catch(Exception e){ return Double.parseDouble(def); }
    }
}
