package GUI;

import Models.User;
import Models.Department;
import database.Queries;
import utils.EnergyCalculator;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class DepartmentPanel extends JPanel {

    private final User          currentUser;
    private JTable              table;
    private DefaultTableModel   model;
    private JTextField          nameField, floorField, contactField, searchField;
    private JButton             addBtn, updateBtn, deleteBtn, clearBtn;
    private JLabel              formTitle;
    private int                 editingId = -1;

    public DepartmentPanel(User user) {
        this.currentUser = user;
        setOpaque(false);
        setLayout(new BorderLayout());
        buildUI();
        loadData();
        if (!user.isAdmin()) disableForm();
    }

    private void buildUI() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildForm(), buildTable());
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

        formTitle = Theme.label("Add Department", Theme.FONT_HEADING, Theme.ACCENT);
        c.gridy=0; c.insets=new Insets(0,0,16,0); card.add(formTitle,c);

        c.gridy=1; c.insets=new Insets(0,0,5,0);
        card.add(Theme.label("Department Name",Theme.FONT_BOLD,Theme.TEXT_SECONDARY),c);
        c.gridy=2; c.insets=new Insets(0,0,12,0);
        nameField=Theme.field("e.g. Computer Science");
        nameField.setPreferredSize(new Dimension(240,38)); card.add(nameField,c);

        c.gridy=3; c.insets=new Insets(0,0,5,0);
        card.add(Theme.label("Floor Number",Theme.FONT_BOLD,Theme.TEXT_SECONDARY),c);
        c.gridy=4; c.insets=new Insets(0,0,12,0);
        floorField=Theme.field("e.g. 3");
        floorField.setPreferredSize(new Dimension(240,38)); card.add(floorField,c);

        c.gridy=5; c.insets=new Insets(0,0,5,0);
        card.add(Theme.label("Contact Number",Theme.FONT_BOLD,Theme.TEXT_SECONDARY),c);
        c.gridy=6; c.insets=new Insets(0,0,20,0);
        contactField=Theme.field("e.g. 0300-1234567");
        contactField.setPreferredSize(new Dimension(240,38)); card.add(contactField,c);

        JPanel btns=new JPanel(new GridLayout(2,2,8,8)); btns.setOpaque(false);
        addBtn    = Theme.button("Add",    Theme.ACCENT_DARK);
        updateBtn = Theme.button("Update", new Color(160,100,0));
        deleteBtn = Theme.button("Delete", new Color(140,30,30));
        clearBtn  = Theme.button("Clear",  new Color(50,70,54));
        updateBtn.setEnabled(false); deleteBtn.setEnabled(false);
        btns.add(addBtn); btns.add(updateBtn); btns.add(deleteBtn); btns.add(clearBtn);

        addBtn.addActionListener(e    -> addDept());
        updateBtn.addActionListener(e -> updateDept());
        deleteBtn.addActionListener(e -> deleteDept());
        clearBtn.addActionListener(e  -> clearForm());

        c.gridy=7; c.insets=new Insets(0,0,0,0); card.add(btns,c);

        outer.add(card,BorderLayout.CENTER);
        return outer;
    }

    private JPanel buildTable() {
        JPanel outer = Theme.darkPanel(new BorderLayout());
        outer.setBorder(BorderFactory.createEmptyBorder(16,8,16,16));

        JPanel topBar = new JPanel(new BorderLayout(10,0));
        topBar.setOpaque(false); topBar.setBorder(BorderFactory.createEmptyBorder(0,0,10,0));
        JLabel title = Theme.label("All Departments", Theme.FONT_HEADING, Theme.TEXT_PRIMARY);
        searchField = Theme.field("🔍 Search…");
        searchField.setPreferredSize(new Dimension(200,36));
        searchField.addActionListener(e -> filterTable(searchField.getText()));
        topBar.add(title,BorderLayout.WEST); topBar.add(searchField,BorderLayout.EAST);

        model = new DefaultTableModel(
            new String[]{"ID","Name","Floor","Contact","kWh/Month","Cost/Month"},0){
            @Override public boolean isCellEditable(int r,int c){return false;}
        };
        table = new JTable(model); Theme.styleTable(table);
        table.getColumnModel().getColumn(0).setMaxWidth(40);
        table.getSelectionModel().addListSelectionListener(e->{
            if(!e.getValueIsAdjusting()&&table.getSelectedRow()>=0) loadSelected();
        });

        JPanel card = Theme.card(null);
        card.setLayout(new BorderLayout(0,10));
        card.add(topBar,BorderLayout.NORTH);
        card.add(Theme.scroll(table),BorderLayout.CENTER);
        outer.add(card,BorderLayout.CENTER);
        return outer;
    }

    private void loadData() {
        model.setRowCount(0);
        for (Department d : Queries.getAllDepartments()) {
            double kwh  = Queries.getTotalMonthlyKWhByDepartment(d.getDeptId());
            double cost = EnergyCalculator.calculateMonthlyCost(kwh);
            model.addRow(new Object[]{
                d.getDeptId(), d.getDeptName(), d.getFloorNumber(),
                d.getContactNumber()!=null?d.getContactNumber():"—",
                String.format("%.1f",kwh), String.format("Rs %.0f",cost)
            });
        }
    }

    private void filterTable(String q) {
        model.setRowCount(0);
        for (Department d : Queries.getAllDepartments()) {
            if (q.isEmpty()||q.startsWith("🔍")||
                d.getDeptName().toLowerCase().contains(q.toLowerCase())) {
                double kwh=Queries.getTotalMonthlyKWhByDepartment(d.getDeptId());
                double cost=EnergyCalculator.calculateMonthlyCost(kwh);
                model.addRow(new Object[]{
                    d.getDeptId(),d.getDeptName(),d.getFloorNumber(),
                    d.getContactNumber()!=null?d.getContactNumber():"—",
                    String.format("%.1f",kwh),String.format("Rs %.0f",cost)
                });
            }
        }
    }

    private void loadSelected() {
        int row=table.getSelectedRow(); if(row<0)return;
        editingId=(int)model.getValueAt(row,0);
        setFieldText(nameField,    model.getValueAt(row,1).toString());
        setFieldText(floorField,   model.getValueAt(row,2).toString());
        setFieldText(contactField, model.getValueAt(row,3).toString());
        formTitle.setText("Edit: "+model.getValueAt(row,1));
        addBtn.setEnabled(false); updateBtn.setEnabled(true); deleteBtn.setEnabled(currentUser.isAdmin());
    }

    private void setFieldText(JTextField f, String v) {
        f.setText(v); f.setForeground(Theme.TEXT_PRIMARY);
    }

    private void addDept() {
        if(!validateForm())return;
        Department d=new Department();
        d.setDeptName(nameField.getText().trim());
        d.setFloorNumber(parseIntSafe(floorField.getText()));
        d.setContactNumber(contactField.getText().trim());
        if(Queries.addDepartment(d)){
            JOptionPane.showMessageDialog(this,"Department added!","Success",JOptionPane.INFORMATION_MESSAGE);
            loadData(); clearForm();
        }
    }

    private void updateDept() {
        if(editingId<0||!validateForm())return;
        Department d=new Department();
        d.setDeptId(editingId);
        d.setDeptName(nameField.getText().trim());
        d.setFloorNumber(parseIntSafe(floorField.getText()));
        d.setContactNumber(contactField.getText().trim());
        if(Queries.updateDepartment(d)){
            JOptionPane.showMessageDialog(this,"Updated!","Success",JOptionPane.INFORMATION_MESSAGE);
            loadData(); clearForm();
        }
    }

    private void deleteDept() {
        if(editingId<0)return;
        int r=JOptionPane.showConfirmDialog(this,"Delete this department and all its devices?",
            "Confirm",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
        if(r==JOptionPane.YES_OPTION&&Queries.deleteDepartment(editingId)){
            JOptionPane.showMessageDialog(this,"Deleted!","Success",JOptionPane.INFORMATION_MESSAGE);
            loadData(); clearForm();
        }
    }

    private void clearForm() {
        editingId=-1;
        resetField(nameField,"e.g. Computer Science");
        resetField(floorField,"e.g. 3");
        resetField(contactField,"e.g. 0300-1234567");
        formTitle.setText("Add Department");
        addBtn.setEnabled(true); updateBtn.setEnabled(false); deleteBtn.setEnabled(false);
        table.clearSelection();
    }

    private void resetField(JTextField f, String ph){f.setText(ph);f.setForeground(Theme.TEXT_SECONDARY);}

    private boolean validateForm(){
        if(nameField.getText().trim().isEmpty()||nameField.getText().equals("e.g. Computer Science")){
            JOptionPane.showMessageDialog(this,"Department name required","Validation",JOptionPane.ERROR_MESSAGE);
            nameField.requestFocus(); return false;
        }
        return true;
    }

    private int parseIntSafe(String s){try{return Integer.parseInt(s.trim());}catch(Exception e){return 0;}}

    private void disableForm(){
        nameField.setEnabled(false); floorField.setEnabled(false); contactField.setEnabled(false);
        addBtn.setEnabled(false); updateBtn.setEnabled(false); deleteBtn.setEnabled(false);
    }
}
