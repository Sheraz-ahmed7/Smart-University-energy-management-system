package GUI;

import Models.Department;
import Models.Device;
import Models.User;
import database.Queries;
import utils.EnergyCalculator;
import utils.CarbonCalculator;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReportsPanel extends JPanel {

    private final User          currentUser;
    private JComboBox<String>   typeCombo;
    private JComboBox<Department> deptCombo;
    private JButton             generateBtn, exportTxtBtn, exportCsvBtn;
    private JTextArea           reportArea;
    private JTable              summaryTable;
    private DefaultTableModel   tableModel;
    private JLabel              generatedLbl;

    public ReportsPanel(User user) {
        this.currentUser = user;
        setOpaque(false); setLayout(new BorderLayout());
        buildUI(); loadDepartments();
    }

    private void buildUI() {
        JPanel outer = Theme.darkPanel(new BorderLayout(0,12));
        outer.setBorder(BorderFactory.createEmptyBorder(16,16,16,16));

        outer.add(buildControls(), BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
            buildSummaryTable(), buildDetailArea());
        split.setResizeWeight(0.4); split.setDividerSize(6);
        split.setOpaque(false); split.setBorder(null);
        outer.add(split, BorderLayout.CENTER);
        outer.add(buildExportBar(), BorderLayout.SOUTH);

        add(outer, BorderLayout.CENTER);
    }

    private JPanel buildControls() {
        JPanel card = Theme.card("📊  Report Controls");
        card.setPreferredSize(new Dimension(900,110));
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT,14,6));
        row.setOpaque(false);

        row.add(Theme.label("Type:",Theme.FONT_BOLD,Theme.TEXT_SECONDARY));
        typeCombo=new JComboBox<>(new String[]{
            "Department Summary","Energy Details","Carbon Footprint","Cost Analysis","Efficiency Ranking"
        });
        Theme.styleCombo(typeCombo); typeCombo.setPreferredSize(new Dimension(220,36));
        row.add(typeCombo);

        row.add(Theme.label("Dept:",Theme.FONT_BOLD,Theme.TEXT_SECONDARY));
        deptCombo=new JComboBox<>();
        Theme.styleCombo(deptCombo); deptCombo.setPreferredSize(new Dimension(200,36));
        row.add(deptCombo);

        generateBtn=Theme.button("Generate",Theme.ACCENT_DARK);
        generateBtn.addActionListener(e->generateReport());
        row.add(generateBtn);

        generatedLbl=Theme.label("",Theme.FONT_SMALL,Theme.TEXT_MUTED);
        row.add(generatedLbl);

        card.add(row,BorderLayout.CENTER);
        return card;
    }

    private JPanel buildSummaryTable() {
        tableModel=new DefaultTableModel(
            new String[]{"Department","kWh/Month","Cost (Rs)","CO₂ (kg)","Score"},0){
            @Override public boolean isCellEditable(int r,int c){return false;}
        };
        summaryTable=new JTable(tableModel); Theme.styleTable(summaryTable);
        JPanel card=Theme.card("Summary View");
        card.add(Theme.scroll(summaryTable),BorderLayout.CENTER);
        return card;
    }

    private JPanel buildDetailArea() {
        reportArea=new JTextArea();
        reportArea.setEditable(false); reportArea.setFont(Theme.FONT_MONO);
        reportArea.setBackground(Theme.BG_FIELD); reportArea.setForeground(Theme.TEXT_PRIMARY);
        reportArea.setMargin(new Insets(12,14,12,14));
        JPanel card=Theme.card("Detailed Report");
        card.add(Theme.scroll(reportArea),BorderLayout.CENTER);
        return card;
    }

    private JPanel buildExportBar() {
        JPanel bar=new JPanel(new FlowLayout(FlowLayout.RIGHT,10,4));
        bar.setOpaque(false);
        exportTxtBtn=Theme.button("Export TXT", new Color(50,80,130));
        exportCsvBtn=Theme.button("Export CSV", new Color(30,110,60));
        exportTxtBtn.addActionListener(e->exportTxt());
        exportCsvBtn.addActionListener(e->exportCsv());
        bar.add(exportTxtBtn); bar.add(exportCsvBtn);
        return bar;
    }

    private void loadDepartments(){
        deptCombo.removeAllItems();
        deptCombo.addItem(new Department(0,"All Departments",0,""));
        for(Department d:Queries.getAllDepartments()) deptCombo.addItem(d);
        if(!currentUser.isAdmin()&&currentUser.getDepartmentId()!=null){
            deptCombo.removeAllItems();
            for(Department d:Queries.getAllDepartments())
                if(d.getDeptId()==currentUser.getDepartmentId()){ deptCombo.addItem(d); break; }
            deptCombo.setEnabled(false);
        }
    }

    private void generateReport(){
        loadSummaryTable();
        Department sel=(Department)deptCombo.getSelectedItem();
        String type=(String)typeCombo.getSelectedItem();
        String text=switch(type){
            case "Department Summary"   -> deptSummary(sel);
            case "Energy Details"       -> energyDetails(sel);
            case "Carbon Footprint"     -> carbonReport(sel);
            case "Cost Analysis"        -> costAnalysis(sel);
            case "Efficiency Ranking"   -> efficiencyRanking();
            default -> "";
        };
        reportArea.setText(text); reportArea.setCaretPosition(0);
        generatedLbl.setText("Generated: "+
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }

    private void loadSummaryTable(){
        tableModel.setRowCount(0);
        for(Department d:Queries.getAllDepartments()){
            double kwh =Queries.getTotalMonthlyKWhByDepartment(d.getDeptId());
            double cost=EnergyCalculator.calculateMonthlyCost(kwh);
            double co2 =CarbonCalculator.calculateCarbonFootprint(kwh);
            int    sc  =effScore(kwh);
            tableModel.addRow(new Object[]{
                d.getDeptName(),
                String.format("%.1f",kwh),
                String.format("%.0f",cost),
                String.format("%.1f",co2),
                sc+"/100"
            });
        }
    }

    private int effScore(double kwh){
        if(kwh<500)  return 95;
        if(kwh<1000) return 85;
        if(kwh<2000) return 72;
        if(kwh<3000) return 60;
        if(kwh<4000) return 48;
        return 35;
    }

    private String deptSummary(Department sel){
        StringBuilder sb=new StringBuilder();
        sb.append(h1("DEPARTMENT ENERGY SUMMARY REPORT"));
        List<Department> list=sel!=null&&sel.getDeptId()!=0?List.of(sel):Queries.getAllDepartments();
        for(Department d:list){ appendDept(sb,d); sb.append(sep(60)); }
        return sb.toString();
    }

    private void appendDept(StringBuilder sb, Department d){
        double kwh=Queries.getTotalMonthlyKWhByDepartment(d.getDeptId());
        double cost=EnergyCalculator.calculateMonthlyCost(kwh);
        double co2=CarbonCalculator.calculateCarbonFootprint(kwh);
        int trees=CarbonCalculator.calculateTreesNeeded(co2);
        sb.append("Department  : ").append(d.getDeptName()).append("\n")
          .append("Floor       : ").append(d.getFloorNumber()).append("\n")
          .append("Contact     : ").append(d.getContactNumber()!=null?d.getContactNumber():"N/A").append("\n")
          .append(String.format("Monthly kWh : %.2f kWh%n",kwh))
          .append(String.format("Monthly Cost: Rs. %.2f%n",cost))
          .append(String.format("CO₂ Emitted : %.2f kg%n",co2))
          .append(String.format("Trees Needed: %d%n%n",trees));
    }

    private String energyDetails(Department sel){
        StringBuilder sb=new StringBuilder();
        sb.append(h1("ENERGY CONSUMPTION DETAILS"));
        List<Department> list=sel!=null&&sel.getDeptId()!=0?List.of(sel):Queries.getAllDepartments();
        for(Department d:list){
            sb.append("\n").append(d.getDeptName()).append("\n").append(sep(40)).append("\n");
            List<Device> devs=Queries.getDevicesByDepartment(d.getDeptId());
            if(devs.isEmpty()){ sb.append("No devices registered.\n"); continue; }
            sb.append(String.format("%-22s %7s %5s %8s %12s%n","Device","Watts","Qty","Hrs/Day","Monthly kWh"));
            sb.append(sep(58)).append("\n");
            for(Device dv:devs)
                sb.append(String.format("%-22s %7d %5d %8.1f %12.2f%n",
                    trunc(dv.getDeviceName(),22),dv.getWattage(),dv.getQuantity(),
                    dv.getHoursPerDay(),dv.calculateMonthlyKWh()));
        }
        return sb.toString();
    }

    private String carbonReport(Department sel){
        StringBuilder sb=new StringBuilder();
        sb.append(h1("CARBON FOOTPRINT REPORT"));
        sb.append("Emission factor: 0.5 kg CO₂ per kWh\n");
        sb.append("Tree offset    : 1 tree absorbs ~22 kg CO₂/year\n\n");
        List<Department> list=sel!=null&&sel.getDeptId()!=0?List.of(sel):Queries.getAllDepartments();
        double total=0; int totalT=0;
        sb.append(String.format("%-28s %12s %12s %8s%n","Department","Monthly CO₂","Annual CO₂","Trees"));
        sb.append(sep(64)).append("\n");
        for(Department d:list){
            double kwh=Queries.getTotalMonthlyKWhByDepartment(d.getDeptId());
            double co2=CarbonCalculator.calculateCarbonFootprint(kwh);
            int t=CarbonCalculator.calculateTreesNeeded(co2);
            sb.append(String.format("%-28s %12.2f %12.2f %8d%n",d.getDeptName(),co2,co2*12,t));
            total+=co2; totalT+=t;
        }
        sb.append(sep(64)).append("\n");
        sb.append(String.format("%-28s %12.2f %12.2f %8d%n","TOTAL",total,total*12,totalT));
        return sb.toString();
    }

    private String costAnalysis(Department sel){
        StringBuilder sb=new StringBuilder();
        sb.append(h1("COST ANALYSIS REPORT"));
        sb.append("Rate: Rs. 25.00 per kWh\n\n");
        List<Department> list=sel!=null&&sel.getDeptId()!=0?List.of(sel):Queries.getAllDepartments();
        double total=0;
        sb.append(String.format("%-28s %14s %14s%n","Department","Monthly Cost","Annual Cost"));
        sb.append(sep(58)).append("\n");
        for(Department d:list){
            double kwh=Queries.getTotalMonthlyKWhByDepartment(d.getDeptId());
            double cost=EnergyCalculator.calculateMonthlyCost(kwh);
            sb.append(String.format("%-28s %14.2f %14.2f%n",d.getDeptName(),cost,cost*12));
            total+=cost;
        }
        sb.append(sep(58)).append("\n");
        sb.append(String.format("%-28s %14.2f %14.2f%n","TOTAL",total,total*12));
        return sb.toString();
    }

    private String efficiencyRanking(){
        StringBuilder sb=new StringBuilder();
        sb.append(h1("DEPARTMENT EFFICIENCY RANKING"));
        List<Department> list=Queries.getAllDepartments();
        record DS(Department d, int score){}
        List<DS> ranked=list.stream()
            .map(d->new DS(d,effScore(Queries.getTotalMonthlyKWhByDepartment(d.getDeptId()))))
            .sorted((a,b)->b.score()-a.score())
            .toList();
        sb.append(String.format("%-4s %-26s %8s %12s%n","Rank","Department","Score","Rating"));
        sb.append(sep(54)).append("\n");
        for(int i=0;i<ranked.size();i++)
            sb.append(String.format("%-4d %-26s %8d %12s%n",
                i+1,ranked.get(i).d().getDeptName(),ranked.get(i).score(),rating(ranked.get(i).score())));
        return sb.toString();
    }

    private String rating(int s){
        if(s>=90)return "Excellent ★★★★★";
        if(s>=75)return "Good      ★★★★";
        if(s>=60)return "Average   ★★★";
        if(s>=45)return "Below avg ★★";
        return "Poor      ★";
    }

    private String h1(String t){ return "=".repeat(70)+"\n"+t+"\n"+"=".repeat(70)+"\n\n"; }
    private String sep(int n){ return "-".repeat(n); }
    private String trunc(String s,int n){ return s.length()<=n?s:s.substring(0,n-2)+".."; }

    private void exportTxt(){
        if(reportArea.getText().isBlank()){ generateReport(); }
        JFileChooser fc=new JFileChooser();
        fc.setSelectedFile(new File("Energy_Report.txt"));
        if(fc.showSaveDialog(this)==JFileChooser.APPROVE_OPTION){
            try(PrintWriter pw=new PrintWriter(new FileWriter(fc.getSelectedFile()))){
                pw.print(reportArea.getText());
                JOptionPane.showMessageDialog(this,"Report saved!","Done",JOptionPane.INFORMATION_MESSAGE);
            }catch(Exception e){
                JOptionPane.showMessageDialog(this,"Error: "+e.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportCsv(){
        JFileChooser fc=new JFileChooser();
        fc.setSelectedFile(new File("Energy_Summary.csv"));
        if(fc.showSaveDialog(this)==JFileChooser.APPROVE_OPTION){
            try(PrintWriter pw=new PrintWriter(new FileWriter(fc.getSelectedFile()))){
                pw.println("Department,Monthly kWh,Monthly Cost (Rs),CO2 (kg),Efficiency Score");
                for(Department d:Queries.getAllDepartments()){
                    double kwh=Queries.getTotalMonthlyKWhByDepartment(d.getDeptId());
                    double cost=EnergyCalculator.calculateMonthlyCost(kwh);
                    double co2=CarbonCalculator.calculateCarbonFootprint(kwh);
                    int sc=effScore(kwh);
                    pw.printf("\"%s\",%.2f,%.2f,%.2f,%d%n",
                        d.getDeptName(),kwh,cost,co2,sc);
                }
                JOptionPane.showMessageDialog(this,"CSV saved!","Done",JOptionPane.INFORMATION_MESSAGE);
            }catch(Exception e){
                JOptionPane.showMessageDialog(this,"Error: "+e.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
