package GUI;

import Models.User;
import Models.Device;
import Models.Department;
import database.Queries;
import utils.EnergyCalculator;
import utils.CarbonCalculator;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class DeviceEntryPanel extends JPanel {

    private final User        currentUser;
    private JTable            table;
    private DefaultTableModel model;
    private JComboBox<Department> deptCombo;
    private JTextField        nameField, wattField, qtyField, hrsField;
    private JButton           addBtn, updateBtn, deleteBtn, clearBtn;
    private JLabel            kwhLbl, costLbl, co2Lbl, treesLbl, formTitle;
    private int               editingId = -1;

    public DeviceEntryPanel(User user) {
        this.currentUser = user;
        setOpaque(false); setLayout(new BorderLayout());
        buildUI(); loadDepartments(); loadDevices();
    }

    private void buildUI() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildForm(), buildRight());
        split.setDividerLocation(300); split.setDividerSize(6);
        split.setOpaque(false); split.setBorder(null);
        add(split, BorderLayout.CENTER);
    }

    private JPanel buildForm() {
        JPanel outer = Theme.darkPanel(new BorderLayout());
        outer.setBorder(BorderFactory.createEmptyBorder(16,16,16,8));

        JPanel card = Theme.card(null);
        card.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill=GridBagConstraints.HORIZONTAL; c.weightx=1; c.gridx=0;

        formTitle = Theme.label("Add Device", Theme.FONT_HEADING, Theme.ACCENT);
        c.gridy=0; c.insets=new Insets(0,0,14,0); card.add(formTitle,c);

        // Department
        c.gridy=1; c.insets=new Insets(0,0,5,0);
        card.add(Theme.label("Department",Theme.FONT_BOLD,Theme.TEXT_SECONDARY),c);
        c.gridy=2; c.insets=new Insets(0,0,12,0);
        deptCombo=new JComboBox<>(); Theme.styleCombo(deptCombo);
        deptCombo.setPreferredSize(new Dimension(240,38));
        deptCombo.addActionListener(e -> loadDevices());
        if(!currentUser.isAdmin()) deptCombo.setEnabled(false);
        card.add(deptCombo,c);

        // Name
        c.gridy=3; c.insets=new Insets(0,0,5,0);
        card.add(Theme.label("Device Name",Theme.FONT_BOLD,Theme.TEXT_SECONDARY),c);
        c.gridy=4; c.insets=new Insets(0,0,12,0);
        nameField=Theme.field("e.g. Air Conditioner");
        nameField.setPreferredSize(new Dimension(240,38)); card.add(nameField,c);

        // Wattage
        c.gridy=5; c.insets=new Insets(0,0,5,0);
        card.add(Theme.label("Wattage (W)",Theme.FONT_BOLD,Theme.TEXT_SECONDARY),c);
        c.gridy=6; c.insets=new Insets(0,0,12,0);
        wattField=Theme.field("e.g. 1500");
        wattField.setPreferredSize(new Dimension(240,38)); card.add(wattField,c);

        // Quantity
        c.gridy=7; c.insets=new Insets(0,0,5,0);
        card.add(Theme.label("Quantity",Theme.FONT_BOLD,Theme.TEXT_SECONDARY),c);
        c.gridy=8; c.insets=new Insets(0,0,12,0);
        qtyField=Theme.field("e.g. 4");
        qtyField.setPreferredSize(new Dimension(240,38)); card.add(qtyField,c);

        // Hours
        c.gridy=9; c.insets=new Insets(0,0,5,0);
        card.add(Theme.label("Hours / Day",Theme.FONT_BOLD,Theme.TEXT_SECONDARY),c);
        c.gridy=10; c.insets=new Insets(0,0,18,0);
        hrsField=Theme.field("e.g. 8");
        hrsField.setPreferredSize(new Dimension(240,38)); card.add(hrsField,c);

        JPanel btns=new JPanel(new GridLayout(2,2,8,8)); btns.setOpaque(false);
        addBtn    = Theme.button("Add",    Theme.ACCENT_DARK);
        updateBtn = Theme.button("Update", new Color(160,100,0));
        deleteBtn = Theme.button("Delete", new Color(140,30,30));
        clearBtn  = Theme.button("Clear",  new Color(50,70,54));
        updateBtn.setEnabled(false); deleteBtn.setEnabled(false);
        btns.add(addBtn); btns.add(updateBtn); btns.add(deleteBtn); btns.add(clearBtn);

        addBtn.addActionListener(e    -> addDevice());
        updateBtn.addActionListener(e -> updateDevice());
        deleteBtn.addActionListener(e -> deleteDevice());
        clearBtn.addActionListener(e  -> clearForm());

        c.gridy=11; c.insets=new Insets(0,0,0,0); card.add(btns,c);
        outer.add(card,BorderLayout.CENTER);
        return outer;
    }

    private JPanel buildRight() {
        JPanel outer = Theme.darkPanel(new BorderLayout(0,14));
        outer.setBorder(BorderFactory.createEmptyBorder(16,8,16,16));

        // Summary cards row
        JPanel summary = new JPanel(new GridLayout(1,4,10,0));
        summary.setOpaque(false);
        summary.setPreferredSize(new Dimension(900,90));

        kwhLbl   = makeSummaryCard(summary,"Monthly kWh",   "0.0",   "kWh",   Theme.ACCENT);
        costLbl  = makeSummaryCard(summary,"Monthly Cost",  "Rs 0",  "PKR",   Theme.AMBER);
        co2Lbl   = makeSummaryCard(summary,"CO₂ Emission",  "0.0",   "kg CO₂",Theme.RED);
        treesLbl = makeSummaryCard(summary,"Trees to Offset","0",    "trees", new Color(50,180,100));

        // Table
        model = new DefaultTableModel(
            new String[]{"ID","Device","Watts","Qty","Hrs/Day","Daily kWh","Monthly kWh"},0){
            @Override public boolean isCellEditable(int r,int c){return false;}
        };
        table=new JTable(model); Theme.styleTable(table);
        table.getColumnModel().getColumn(0).setMaxWidth(40);
        table.getSelectionModel().addListSelectionListener(e->{
            if(!e.getValueIsAdjusting()&&table.getSelectedRow()>=0) loadSelectedDevice();
        });

        JPanel tableCard = Theme.card("Devices in Department");
        tableCard.add(Theme.scroll(table),BorderLayout.CENTER);

        outer.add(summary,   BorderLayout.NORTH);
        outer.add(tableCard, BorderLayout.CENTER);
        return outer;
    }

    private JLabel makeSummaryCard(JPanel parent, String title, String val, String unit, Color accent) {
        JPanel card = Theme.kpiCard(title, val, unit, accent);
        parent.add(card);
        return findValueLabel(card);
    }

    private JLabel findValueLabel(JPanel card) {
        for (Component c : card.getComponents())
            if (c instanceof JLabel l && l.getFont().getSize() >= 20) return l;
        return new JLabel("0");
    }

    private void loadDepartments() {
        deptCombo.removeAllItems();
        List<Department> depts = Queries.getAllDepartments();
        for (Department d : depts) deptCombo.addItem(d);
        if (!currentUser.isAdmin() && currentUser.getDepartmentId() != null) {
            for (Department d : depts)
                if (d.getDeptId() == currentUser.getDepartmentId()) { deptCombo.setSelectedItem(d); break; }
        }
    }

    private void loadDevices() {
        Department sel = (Department)deptCombo.getSelectedItem();
        if (sel==null) return;
        List<Device> devices = Queries.getDevicesByDepartment(sel.getDeptId());
        model.setRowCount(0);
        double total=0;
        for (Device d : devices) {
            double daily=d.calculateDailyKWh(), monthly=d.calculateMonthlyKWh();
            total+=monthly;
            model.addRow(new Object[]{
                d.getDeviceId(),d.getDeviceName(),d.getWattage(),d.getQuantity(),
                d.getHoursPerDay(),String.format("%.2f",daily),String.format("%.2f",monthly)
            });
        }
        updateSummary(total);
    }

    private void updateSummary(double kwh) {
        double cost=EnergyCalculator.calculateMonthlyCost(kwh);
        double co2 =CarbonCalculator.calculateCarbonFootprint(kwh);
        int    trees=CarbonCalculator.calculateTreesNeeded(co2);
        if(kwhLbl!=null)   kwhLbl.setText(String.format("%.1f",kwh));
        if(costLbl!=null)  costLbl.setText(String.format("Rs %.0f",cost));
        if(co2Lbl!=null)   co2Lbl.setText(String.format("%.1f",co2));
        if(treesLbl!=null) treesLbl.setText(String.valueOf(trees));
    }

    private void loadSelectedDevice() {
        int row=table.getSelectedRow(); if(row<0)return;
        editingId=(int)model.getValueAt(row,0);
        setF(nameField,model.getValueAt(row,1).toString());
        setF(wattField,model.getValueAt(row,2).toString());
        setF(qtyField, model.getValueAt(row,3).toString());
        setF(hrsField, model.getValueAt(row,4).toString());
        formTitle.setText("Edit Device");
        addBtn.setEnabled(false); updateBtn.setEnabled(true); deleteBtn.setEnabled(true);
    }

    private void setF(JTextField f,String v){f.setText(v);f.setForeground(Theme.TEXT_PRIMARY);}

    private void addDevice() {
        if(!validateForm())return;
        Department sel=(Department)deptCombo.getSelectedItem(); if(sel==null)return;
        Device d=new Device(sel.getDeptId(),nameField.getText().trim(),
            parseInt(wattField),parseInt(qtyField),parseDouble(hrsField));
        if(Queries.addDevice(d)){
            JOptionPane.showMessageDialog(this,"Device added!","Success",JOptionPane.INFORMATION_MESSAGE);
            clearForm(); loadDevices();
        }
    }

    private void updateDevice() {
        if(editingId<0||!validateForm())return;
        Department sel=(Department)deptCombo.getSelectedItem(); if(sel==null)return;
        Device d=new Device(sel.getDeptId(),nameField.getText().trim(),
            parseInt(wattField),parseInt(qtyField),parseDouble(hrsField));
        d.setDeviceId(editingId);
        if(Queries.updateDevice(d)){
            JOptionPane.showMessageDialog(this,"Updated!","Success",JOptionPane.INFORMATION_MESSAGE);
            clearForm(); loadDevices();
        }
    }

    private void deleteDevice() {
        if(editingId<0)return;
        int r=JOptionPane.showConfirmDialog(this,"Delete this device?","Confirm",
            JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
        if(r==JOptionPane.YES_OPTION&&Queries.deleteDevice(editingId)){
            JOptionPane.showMessageDialog(this,"Deleted!","Success",JOptionPane.INFORMATION_MESSAGE);
            clearForm(); loadDevices();
        }
    }

    private void clearForm() {
        editingId=-1;
        resetF(nameField,"e.g. Air Conditioner"); resetF(wattField,"e.g. 1500");
        resetF(qtyField,"e.g. 4"); resetF(hrsField,"e.g. 8");
        formTitle.setText("Add Device");
        addBtn.setEnabled(true); updateBtn.setEnabled(false); deleteBtn.setEnabled(false);
        table.clearSelection();
    }

    private void resetF(JTextField f,String ph){f.setText(ph);f.setForeground(Theme.TEXT_SECONDARY);}

    private boolean validateForm() {
        String n=nameField.getText().trim();
        if(n.isEmpty()||n.startsWith("e.g.")){
            JOptionPane.showMessageDialog(this,"Device name required","Validation",JOptionPane.ERROR_MESSAGE);return false;
        }
        try { int w=parseInt(wattField); if(w<=0)throw new NumberFormatException(); }
        catch(Exception e){ JOptionPane.showMessageDialog(this,"Valid wattage required","Validation",JOptionPane.ERROR_MESSAGE);return false; }
        try { int q=parseInt(qtyField); if(q<=0)throw new NumberFormatException(); }
        catch(Exception e){ JOptionPane.showMessageDialog(this,"Valid quantity required","Validation",JOptionPane.ERROR_MESSAGE);return false; }
        try { double h=parseDouble(hrsField); if(h<0||h>24)throw new NumberFormatException(); }
        catch(Exception e){ JOptionPane.showMessageDialog(this,"Valid hours (0–24) required","Validation",JOptionPane.ERROR_MESSAGE);return false; }
        return true;
    }

    private int    parseInt(JTextField f)    { try{return Integer.parseInt(f.getText().trim());}catch(Exception e){return 0;} }
    private double parseDouble(JTextField f) { try{return Double.parseDouble(f.getText().trim());}catch(Exception e){return 0;} }
}
